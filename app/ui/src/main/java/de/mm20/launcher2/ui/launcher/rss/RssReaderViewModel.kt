package de.mm20.launcher2.ui.launcher.rss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.preferences.rss.RssSettings
import de.mm20.launcher2.rss.RssRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RssReaderArticle(
    val title: String,
    val link: String,
    val summary: String?,
    val publishedMillis: Long?,
    val sourceLabel: String,
    val imageUrl: String? = null,
)

data class RssReaderUiState(
    val articles: List<RssReaderArticle> = emptyList(),
    val feedUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class RssReaderViewModel(
    private val rssRepository: RssRepository,
    private val rssSettings: RssSettings,
) : ViewModel() {

    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val mergedArticles = MutableStateFlow<List<RssReaderArticle>>(emptyList())

    val uiState: StateFlow<RssReaderUiState> = combine(
        mergedArticles,
        rssSettings.feedUrls,
        loading,
        error,
    ) { articles, urls, isLoading, err ->
        RssReaderUiState(
            articles = articles,
            feedUrls = urls,
            isLoading = isLoading,
            errorMessage = err,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RssReaderUiState())

    init {
        viewModelScope.launch {
            rssSettings.feedUrls.collectLatest { urls ->
                loadFeeds(urls)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadFeeds(rssSettings.feedUrls.first())
        }
    }

    private suspend fun loadFeeds(urls: List<String>) {
        if (urls.isEmpty()) {
            mergedArticles.value = emptyList()
            loading.value = false
            error.value = null
            return
        }
        loading.value = true
        error.value = null
        val collected = mutableListOf<RssReaderArticle>()
        val failures = mutableListOf<String>()
        for (url in urls) {
            rssRepository.fetchFeed(url).fold(
                onSuccess = { feed ->
                    val source = feed.feedTitle?.takeIf { it.isNotBlank() } ?: url
                    for (item in feed.items) {
                        collected += RssReaderArticle(
                            title = item.title,
                            link = item.link,
                            summary = item.summary,
                            publishedMillis = item.publishedMillis,
                            sourceLabel = source,
                            imageUrl = item.imageUrl,
                        )
                    }
                },
                onFailure = {
                    failures += url
                },
            )
        }
        val distinct = collected
            .distinctBy { it.link }
            .sortedByDescending { it.publishedMillis ?: 0L }
        mergedArticles.value = distinct
        loading.value = false
        error.value = when {
            distinct.isEmpty() && failures.isNotEmpty() ->
                failures.firstOrNull()
            else -> null
        }
    }
}
