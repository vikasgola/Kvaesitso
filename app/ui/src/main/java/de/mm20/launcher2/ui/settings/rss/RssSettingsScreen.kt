package de.mm20.launcher2.ui.settings.rss

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import kotlinx.serialization.Serializable

@Serializable
data object RssSettingsRoute : NavKey

@Composable
fun RssSettingsScreen() {
    val viewModel: RssSettingsScreenVM = viewModel()
    val urls by viewModel.feedUrls.collectAsStateWithLifecycle()

    PreferenceScreen(title = stringResource(R.string.preference_rss_feeds)) {
        item {
            PreferenceCategory {
                var newUrl by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = newUrl,
                    onValueChange = { newUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = { Text(stringResource(R.string.rss_feed_url_hint)) },
                    singleLine = true,
                )
                Preference(
                    title = stringResource(R.string.rss_add_feed),
                    onClick = {
                        if (newUrl.isNotBlank()) {
                            viewModel.addFeedUrl(newUrl.trim())
                            newUrl = ""
                        }
                    },
                    summary = stringResource(R.string.rss_add_feed_summary),
                )
            }
        }
        item {
            PreferenceCategory(title = stringResource(R.string.rss_configured_feeds)) {
                if (urls.isEmpty()) {
                    Text(
                        stringResource(R.string.rss_empty_feeds_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                } else {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        for (url in urls) {
                            Preference(
                                title = url,
                                onClick = {},
                                controls = {
                                    TextButton(onClick = { viewModel.removeFeedUrl(url) }) {
                                        Text(stringResource(R.string.rss_remove_feed))
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
