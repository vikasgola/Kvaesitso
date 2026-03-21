package de.mm20.launcher2.ui.settings.rss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.preferences.rss.RssSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RssSettingsScreenVM : ViewModel(), KoinComponent {

    private val rssSettings: RssSettings by inject()

    val feedUrls = rssSettings.feedUrls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFeedUrl(url: String) {
        viewModelScope.launch {
            rssSettings.addFeedUrl(url)
        }
    }

    fun removeFeedUrl(url: String) {
        viewModelScope.launch {
            rssSettings.removeFeedUrl(url)
        }
    }
}
