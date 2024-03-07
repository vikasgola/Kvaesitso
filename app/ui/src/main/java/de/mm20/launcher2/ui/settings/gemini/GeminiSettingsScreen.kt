package de.mm20.launcher2.ui.settings.gemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import de.mm20.launcher2.ui.component.preferences.TextPreference

@Composable
fun GeminiSettingsScreen() {
    val viewModel: GeminiSettingsScreenVM = viewModel()
    PreferenceScreen(title = stringResource(R.string.preference_search_gemini)) {
        item {
            val gemini by viewModel.gemini.collectAsState()
            SwitchPreference(
                title = stringResource(R.string.preference_search_gemini),
                summary = stringResource(R.string.preference_search_gemini_summary),
                value = gemini == true,
                onValueChanged = {
                    viewModel.setGemini(it)
                }
            )
            val customUrl by viewModel.customUrl.collectAsState()
            TextPreference(
                title = stringResource(R.string.preference_gemini_customurl),
                value = customUrl ?: "",
                placeholder = stringResource(id = R.string.gemini_url),
                summary = customUrl.takeIf { !it.isNullOrBlank() }
                    ?: stringResource(id = R.string.gemini_url),
                onValueChanged = {
                    viewModel.setCustomUrl(it)
                })
        }
    }
}