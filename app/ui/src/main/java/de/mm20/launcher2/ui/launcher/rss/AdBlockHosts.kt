package de.mm20.launcher2.ui.launcher.rss

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.Charsets.UTF_8

/**
 * Blocks third-party ad/tracker hosts in the article WebView (subresources only).
 * Uses a small built-in suffix list immediately; merges in
 * [StevenBlack hosts](https://github.com/StevenBlack/hosts) after load from cache or network.
 */
object AdBlockHosts {

    private const val HOSTS_URL =
        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts"
    private const val CACHE_FILE = "stevenblack_hosts_domains.txt"
    private const val CACHE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val extendedHosts = AtomicReference<Set<String>?>(null)

    private val builtinSuffixes = arrayOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "googletagservices.com",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "adsafeprotected.com",
        "advertising.com",
        "adnxs.com",
        "adsrvr.org",
        "amazon-adsystem.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
        "quantserve.com",
        "moatads.com",
        "chartbeat.com",
        "chartbeat.net",
        "facebook.net",
        "connect.facebook.net",
        "fbcdn.net",
        "analytics.twitter.com",
        "ads-twitter.com",
        "ads.linkedin.com",
        "adform.net",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "contextweb.com",
        "bluekai.com",
        "demdex.net",
        "2mdn.net",
        "3lift.com",
        "adsystem.amazon.com",
        "serving-sys.com",
        "adtechus.com",
        "revcontent.com",
        "media.net",
        "zedo.com",
        "exelator.com",
        "mathtag.com",
        "spotxchange.com",
        "yieldmo.com",
        "indexww.com",
        "casalemedia.com",
        "sitescout.com",
        "turn.com",
        "adsymptotic.com",
        "rlcdn.com",
        "krxd.net",
        "tapad.com",
        "agkn.com",
        "adroll.com",
        "dyntrk.com",
        "smartadserver.com",
        "adition.com",
        "adnxs-simple.com",
        "stickyadstv.com",
        "liverail.com",
        "bidswitch.net",
    )

    private val skipHosts = setOf(
        "localhost",
        "local",
        "broadcasthost",
        "ip6-localhost",
        "ip6-loopback",
        "ipv6-localhost",
    )

    fun warmUp(context: Context) {
        val app = context.applicationContext
        scope.launch { loadAndMaybeRefresh(app) }
    }

    private fun loadAndMaybeRefresh(app: Context) {
        val cache = app.filesDir.resolve(CACHE_FILE)
        if (cache.isFile && cache.length() > 0L) {
            runCatching { readHostLines(cache.inputStream().buffered()) }
                .getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?.let { extendedHosts.set(it) }
        }
        val stale = !cache.isFile ||
            cache.length() == 0L ||
            System.currentTimeMillis() - cache.lastModified() > CACHE_MAX_AGE_MS
        if (!stale) return
        runCatching {
            val conn = (URL(HOSTS_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Kvaesitso-RssReader/1")
            }
            conn.inputStream.use { ins ->
                val set = parseStevenBlackHosts(ins)
                if (set.isNotEmpty()) {
                    extendedHosts.set(set)
                    cache.outputStream().bufferedWriter(UTF_8).use { w ->
                        for (h in set) {
                            w.write(h)
                            w.newLine()
                        }
                    }
                }
            }
        }
    }

    private fun readHostLines(input: java.io.BufferedInputStream): Set<String> {
        val set = HashSet<String>(90_000)
        BufferedReader(InputStreamReader(input, UTF_8)).useLines { lines ->
            lines.forEach { line ->
                val h = line.trim().lowercase()
                if (h.isNotEmpty() && !h.startsWith("#")) set.add(h)
            }
        }
        return set
    }

    private fun parseStevenBlackHosts(ins: java.io.InputStream): Set<String> {
        val set = HashSet<String>(90_000)
        BufferedReader(InputStreamReader(ins, UTF_8)).useLines { lines ->
            lines.forEach { line ->
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) return@forEach
                val tokens = t.split(Regex("\\s+")).filter { it.isNotEmpty() && !it.startsWith("#") }
                if (tokens.size < 2) return@forEach
                val rest = tokens.dropWhile { token -> isHostsFileIpOrPlaceholder(token) }
                for (raw in rest) {
                    val host = raw.lowercase().substringBefore('#').trim()
                    if (host.isEmpty()) continue
                    if (host in skipHosts || host.endsWith(".local")) continue
                    if (host.contains("/") || host.contains(":")) continue
                    if (!host.contains(".")) continue
                    set.add(host)
                }
            }
        }
        return set
    }

    private fun isHostsFileIpOrPlaceholder(token: String): Boolean {
        if (token == "0.0.0.0" || token == "127.0.0.1" || token == "::1") return true
        return token.matches(IP_LIKE_REGEX)
    }

    private val IP_LIKE_REGEX = Regex("^[0-9a-fA-F:.]+$")

    fun shouldBlock(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        if (builtinMatches(host)) return true
        val ext = extendedHosts.get() ?: return false
        return domainOrParentBlocked(host, ext)
    }

    private fun builtinMatches(host: String): Boolean {
        for (s in builtinSuffixes) {
            if (host == s || host.endsWith(".$s")) return true
        }
        return false
    }

    /**
     * True if [host] or any parent domain (label-stripped) is in the block set.
     */
    private fun domainOrParentBlocked(host: String, blocked: Set<String>): Boolean {
        var h = host
        while (true) {
            if (h in blocked) return true
            val dot = h.indexOf('.')
            if (dot < 0) return false
            h = h.substring(dot + 1)
        }
    }
}
