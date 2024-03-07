package de.mm20.launcher2.preferences.search

import de.mm20.launcher2.preferences.LauncherDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class GeminiSearchSettings internal constructor(
    private val dataStore: LauncherDataStore
) {
    val enabled
        get() = dataStore.data.map { it.geminiSearchEnabled }.distinctUntilChanged()

    fun setEnabled(enabled: Boolean) {
        dataStore.update {
            it.copy(geminiSearchEnabled = enabled)
        }
    }

    val customUrl
        get() = dataStore.data.map { it.geminiCustomUrl }.distinctUntilChanged()

    fun setCustomUrl(customUrl: String?) {
        dataStore.update {
            it.copy(geminiCustomUrl = customUrl?.takeIf { it.isNotBlank() })
        }
    }
}