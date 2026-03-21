package de.mm20.launcher2.ui.launcher.scaffold

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.launcher.rss.ArticleReaderActivity
import de.mm20.launcher2.ui.launcher.rss.RssReaderArticle
import de.mm20.launcher2.ui.launcher.rss.RssReaderViewModel
import de.mm20.launcher2.ui.settings.SettingsActivity
import org.koin.androidx.compose.koinViewModel

internal object RssReaderComponent : ScaffoldComponent() {

    private val listState = LazyListState()

    override val isAtTop: State<Boolean?> = derivedStateOf {
        !listState.canScrollBackward
    }

    override val isAtBottom: State<Boolean?> = derivedStateOf {
        !listState.canScrollForward
    }

    override val survivesPause: Boolean = true

    override val showSearchBar: Boolean = false

    override val drawBackground: Boolean = false

    @Composable
    override fun Component(
        modifier: Modifier,
        insets: PaddingValues,
        state: LauncherScaffoldState,
    ) {
        val viewModel: RssReaderViewModel = koinViewModel()
        val ui by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val layoutDirection = LocalLayoutDirection.current

        val nestedScrollConnection = androidx.compose.runtime.remember(state) {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    state.onComponentScroll(-consumed.y)
                    return Offset.Zero
                }
            }
        }

        val scheme = MaterialTheme.colorScheme
        // Avoid double status-bar padding: scaffold insets include status bar; TopAppBar applies it again.
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    start = insets.calculateStartPadding(layoutDirection),
                    top = 0.dp,
                    end = insets.calculateEndPadding(layoutDirection),
                    bottom = insets.calculateBottomPadding(),
                ),
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.rss_reader_title),
                        color = scheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            context.startActivity(
                                Intent(context, SettingsActivity::class.java).apply {
                                    putExtra(
                                        SettingsActivity.EXTRA_ROUTE,
                                        SettingsActivity.ROUTE_RSS_FEEDS,
                                    )
                                },
                            )
                        },
                    ) {
                        Icon(
                            painterResource(R.drawable.settings_24px),
                            contentDescription = stringResource(R.string.preference_rss_feeds),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !ui.isLoading,
                    ) {
                        Icon(
                            painterResource(R.drawable.autorenew_24px),
                            contentDescription = stringResource(R.string.rss_refresh),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = scheme.onSurface,
                    navigationIconContentColor = scheme.onSurface,
                    actionIconContentColor = scheme.onSurface,
                ),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    ui.isLoading && ui.articles.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = scheme.primary)
                        }
                    }
                    ui.feedUrls.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.rss_empty_state),
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onSurface,
                            )
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(context, SettingsActivity::class.java).apply {
                                            putExtra(
                                                SettingsActivity.EXTRA_ROUTE,
                                                SettingsActivity.ROUTE_RSS_FEEDS,
                                            )
                                        },
                                    )
                                },
                            ) {
                                Text(stringResource(R.string.rss_configure_feeds))
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 4.dp,
                                bottom = 28.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (ui.errorMessage != null && ui.articles.isEmpty()) {
                                item {
                                    Text(
                                        stringResource(R.string.rss_error_load),
                                        color = scheme.error,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                            }
                            items(ui.articles, key = { it.link }) { article ->
                                RssArticleRow(
                                    article = article,
                                    onOpen = {
                                        context.startActivity(
                                            Intent(context, ArticleReaderActivity::class.java).apply {
                                                putExtra(ArticleReaderActivity.EXTRA_URL, article.link)
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RssArticleRow(
        article: RssReaderArticle,
        onOpen: () -> Unit,
    ) {
        val scheme = MaterialTheme.colorScheme
        val context = LocalContext.current
        val summaryPlain = article.summary?.let { html ->
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
                .trim()
                .lines()
                .joinToString(" ")
                .take(220)
                .let { if (it.length == 220) "$it…" else it }
        }
        val showSummary = summaryPlain != null &&
            summaryPlain.length > 12 &&
            !summaryPlain.equals("comments", ignoreCase = true) &&
            !summaryPlain.startsWith("comments\n", ignoreCase = true)

        val time = article.publishedMillis?.let { ms ->
            DateUtils.getRelativeTimeSpanString(
                ms,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
            ).toString()
        }

        val cardShape = RoundedCornerShape(16.dp)
        val imageClip = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = scheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!article.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(article.imageUrl)
                            .crossfade(180)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .clip(imageClip),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = article.sourceLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    if (time != null) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    if (showSummary) {
                        Text(
                            text = summaryPlain!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
