package de.mm20.launcher2.preferences.rss

import de.mm20.launcher2.preferences.LauncherDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RssSettings internal constructor(
    private val dataStore: LauncherDataStore,
) {
    val feedUrls: Flow<List<String>> = dataStore.data
        .map { it.rssFeedUrls }
        .distinctUntilChanged()

    fun setFeedUrls(urls: List<String>) {
        val normalized = urls.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        dataStore.update { it.copy(rssFeedUrls = normalized) }
    }

    fun addFeedUrl(url: String) {
        val t = url.trim()
        if (t.isEmpty()) return
        dataStore.update { data ->
            val next = data.rssFeedUrls + t
            data.copy(rssFeedUrls = next.distinct())
        }
    }

    fun removeFeedUrl(url: String) {
        dataStore.update { data ->
            data.copy(rssFeedUrls = data.rssFeedUrls.filterNot { it == url })
        }
    }
}
