package de.mm20.launcher2.ui.settings.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.preferences.search.GeminiSearchSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeminiSettingsScreenVM: ViewModel(), KoinComponent {
    private val geminiSearchSettings: GeminiSearchSettings by inject()

    val gemini = geminiSearchSettings.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    fun setGemini(gemini: Boolean) {
        geminiSearchSettings.setEnabled(gemini)
    }

    val customUrl = geminiSearchSettings.customUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    fun setCustomUrl(customUrl: String) {
        geminiSearchSettings.setCustomUrl(customUrl)
    }
}