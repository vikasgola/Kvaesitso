package de.mm20.launcher2.rss

data class RssParsedFeed(
    val feedTitle: String?,
    val items: List<RssParsedItem>,
)

data class RssParsedItem(
    val title: String,
    val link: String,
    val summary: String?,
    val publishedMillis: Long?,
    val imageUrl: String? = null,
)
