package de.mm20.launcher2.rss

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal object RssParser {

    private const val NS_MEDIA = "http://search.yahoo.com/mrss/"

    fun parse(xml: String): RssParsedFeed? {
        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            parser.setInput(StringReader(xml))
            var event = parser.eventType
            while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
                event = parser.next()
            }
            if (event == XmlPullParser.END_DOCUMENT) return null
            when (parser.name.lowercase()) {
                "rss" -> parseRss(parser)
                "feed" -> parseAtom(parser)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRss(parser: XmlPullParser): RssParsedFeed {
        val items = mutableListOf<RssParsedItem>()
        var feedTitle: String? = null
        var inChannel = false
        var inItem = false
        var currentTitle = ""
        var currentLink = ""
        var currentSummary: String? = null
        var currentPub: Long? = null
        var currentImageUrl: String? = null
        var textTag: String? = null
        val textBuf = StringBuilder()

        fun flushText() {
            val tag = textTag ?: return
            val text = textBuf.toString().trim()
            textBuf.clear()
            textTag = null
            if (text.isEmpty()) return
            when {
                inItem -> when (tag) {
                    "title" -> currentTitle += text
                    "link" -> currentLink += text
                    "guid" -> if (currentLink.isBlank()) currentLink = text
                    "pubdate" -> currentPub = parseDate(text) ?: currentPub
                    "description" -> currentSummary = (currentSummary ?: "") + text
                }
                inChannel && !inItem && tag == "title" -> feedTitle = text
            }
        }

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.lowercase()
                    when {
                        tag == "channel" -> inChannel = true
                        tag == "item" && inChannel -> {
                            flushText()
                            inItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentSummary = null
                            currentPub = null
                            currentImageUrl = null
                        }
                        inItem && tag == "enclosure" -> {
                            val encType = parser.getAttributeValue(null, "type") ?: ""
                            val encUrl = parser.getAttributeValue(null, "url")
                            if (!encUrl.isNullOrBlank() && encType.startsWith("image/", ignoreCase = true)) {
                                currentImageUrl = encUrl.trim()
                            }
                        }
                        inItem && parser.namespace == NS_MEDIA &&
                            (tag == "content" || tag == "thumbnail") -> {
                            val mUrl = parser.getAttributeValue(null, "url")
                                ?: parser.getAttributeValue(NS_MEDIA, "url")
                            val mType = parser.getAttributeValue(null, "type") ?: ""
                            val medium = parser.getAttributeValue(null, "medium")
                            val useUrl = when {
                                tag == "thumbnail" && !mUrl.isNullOrBlank() -> true
                                medium == "image" && !mUrl.isNullOrBlank() -> true
                                mType.startsWith("image/", ignoreCase = true) && !mUrl.isNullOrBlank() -> true
                                else -> false
                            }
                            if (useUrl && currentImageUrl.isNullOrBlank()) {
                                currentImageUrl = mUrl!!.trim()
                            }
                        }
                        inItem && tag in RSS_ITEM_TEXT_TAGS -> {
                            flushText()
                            textTag = tag
                            if (tag == "link") {
                                val href = parser.getAttributeValue(null, "href")
                                if (!href.isNullOrBlank()) {
                                    currentLink = href
                                    textTag = null
                                }
                            }
                        }
                        inChannel && !inItem && tag == "title" -> {
                            flushText()
                            textTag = "title"
                        }
                    }
                }
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    if (textTag != null) textBuf.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    flushText()
                    when (parser.name.lowercase()) {
                        "item" -> {
                            if (inItem && currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                                val summary = currentSummary?.trim()?.takeIf { it.isNotEmpty() }
                                val img = currentImageUrl?.takeIf { it.isNotBlank() }
                                    ?: extractFirstImageUrlFromHtml(summary)
                                items += RssParsedItem(
                                    title = currentTitle.trim(),
                                    link = currentLink.trim(),
                                    summary = summary,
                                    publishedMillis = currentPub,
                                    imageUrl = img,
                                )
                            }
                            inItem = false
                        }
                        "channel" -> inChannel = false
                    }
                }
            }
            parser.next()
        }

        return RssParsedFeed(
            feedTitle = feedTitle?.takeIf { it.isNotBlank() },
            items = items,
        )
    }

    private fun parseAtom(parser: XmlPullParser): RssParsedFeed {
        val items = mutableListOf<RssParsedItem>()
        var feedTitle: String? = null
        var inEntry = false
        var textTag: String? = null
        val textBuf = StringBuilder()

        var currentTitle = ""
        var currentLink = ""
        var currentSummary: String? = null
        var currentPub: Long? = null
        var currentImageUrl: String? = null

        fun flushText() {
            val tag = textTag ?: return
            val text = textBuf.toString().trim()
            textBuf.clear()
            textTag = null
            if (text.isEmpty()) return
            when {
                inEntry -> when (tag) {
                    "title" -> currentTitle += text
                    "summary", "content" -> currentSummary = (currentSummary ?: "") + text
                    "updated", "published" -> currentPub = parseDate(text) ?: currentPub
                }
                tag == "title" -> feedTitle = text
            }
        }

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name.lowercase()
                    when {
                        tag == "entry" -> {
                            flushText()
                            inEntry = true
                            currentTitle = ""
                            currentLink = ""
                            currentSummary = null
                            currentPub = null
                            currentImageUrl = null
                        }
                        tag == "link" && inEntry -> {
                            val href = parser.getAttributeValue(null, "href")
                            val rel = parser.getAttributeValue(null, "rel")
                            val type = parser.getAttributeValue(null, "type") ?: ""
                            if (!href.isNullOrBlank() && rel == "enclosure" &&
                                type.startsWith("image/", ignoreCase = true)
                            ) {
                                currentImageUrl = href.trim()
                            } else if (!href.isNullOrBlank() &&
                                (rel == null || rel == "alternate" || rel == "related")
                            ) {
                                if (currentLink.isBlank()) currentLink = href
                            }
                        }
                        inEntry && parser.namespace == NS_MEDIA &&
                            (tag == "content" || tag == "thumbnail") -> {
                            val mUrl = parser.getAttributeValue(null, "url")
                                ?: parser.getAttributeValue(NS_MEDIA, "url")
                            val mType = parser.getAttributeValue(null, "type") ?: ""
                            val medium = parser.getAttributeValue(null, "medium")
                            val ok = when {
                                tag == "thumbnail" && !mUrl.isNullOrBlank() -> true
                                medium == "image" && !mUrl.isNullOrBlank() -> true
                                mType.startsWith("image/", ignoreCase = true) && !mUrl.isNullOrBlank() -> true
                                else -> false
                            }
                            if (ok && currentImageUrl.isNullOrBlank()) {
                                currentImageUrl = mUrl!!.trim()
                            }
                        }
                        !inEntry && tag == "title" -> {
                            flushText()
                            textTag = "title"
                        }
                        inEntry && tag in ATOM_TEXT_TAGS -> {
                            flushText()
                            textTag = tag
                        }
                    }
                }
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    if (textTag != null) textBuf.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    flushText()
                    when (parser.name.lowercase()) {
                        "entry" -> {
                            if (inEntry && currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                                val summary = currentSummary?.trim()?.takeIf { it.isNotEmpty() }
                                val img = currentImageUrl?.takeIf { it.isNotBlank() }
                                    ?: extractFirstImageUrlFromHtml(summary)
                                items += RssParsedItem(
                                    title = currentTitle.trim(),
                                    link = currentLink.trim(),
                                    summary = summary,
                                    publishedMillis = currentPub,
                                    imageUrl = img,
                                )
                            }
                            inEntry = false
                        }
                    }
                }
            }
            parser.next()
        }

        return RssParsedFeed(
            feedTitle = feedTitle?.takeIf { it.isNotBlank() },
            items = items,
        )
    }

    private fun parseDate(raw: String): Long? {
        val s = raw.trim()
        if (s.isEmpty()) return null
        return try {
            Instant.parse(s).toEpochMilli()
        } catch (_: Exception) {
            try {
                ZonedDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
            } catch (_: Exception) {
                try {
                    ZonedDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
                        .toEpochMilli()
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    private fun extractFirstImageUrlFromHtml(html: String?): String? {
        if (html.isNullOrBlank()) return null
        val m = """(?i)<img[^>]+src\s*=\s*["']([^"']+)["']""".toRegex().find(html)
        val u = m?.groupValues?.getOrNull(1)?.trim() ?: return null
        return u.takeIf {
            it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true)
        }
    }

    private val RSS_ITEM_TEXT_TAGS = setOf("title", "link", "guid", "pubdate", "description")
    private val ATOM_TEXT_TAGS = setOf("title", "summary", "content", "updated", "published")
}
