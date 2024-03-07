package de.mm20.launcher2.ui.launcher.search.gemini

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import coil.compose.AsyncImage
import de.mm20.launcher2.search.GeminiResponse
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.DefaultToolbarAction
import de.mm20.launcher2.ui.component.Toolbar
import de.mm20.launcher2.ui.component.ToolbarAction
import de.mm20.launcher2.ui.component.markdown.MarkdownText
import de.mm20.launcher2.ui.ktx.toPixels
import de.mm20.launcher2.ui.launcher.search.common.SearchableItemVM
import de.mm20.launcher2.ui.launcher.search.listItemViewModel
import de.mm20.launcher2.ui.launcher.sheets.LocalBottomSheetManager
import de.mm20.launcher2.ui.launcher.widgets.notes.LinkedFileConflictStrategy
import de.mm20.launcher2.ui.locals.LocalFavoritesEnabled
import de.mm20.launcher2.ui.locals.LocalGridSettings

@Composable
fun GeminiItem(
    modifier: Modifier = Modifier,
    geminiResponse: GeminiResponse,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val viewModel: SearchableItemVM = listItemViewModel(key = "search-${geminiResponse.key}")
    val iconSize = LocalGridSettings.current.iconSize.dp.toPixels()

    LaunchedEffect(geminiResponse, iconSize) {
        viewModel.init(geminiResponse, iconSize.toInt())
    }

    Column(
        modifier = modifier.clickable {
            viewModel.launch(context)
        }
    ) {
        if (!geminiResponse.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                model = geminiResponse.imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            val tags by viewModel.tags.collectAsState(emptyList())
            var selectedStrategy by remember { mutableStateOf<LinkedFileConflictStrategy?>(null) }
            if (tags.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 1.dp, bottom = 4.dp),
                    text = tags.joinToString(separator = " #", prefix = "#"),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(id = R.string.gemini_source),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            MarkdownText(
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Top, unbounded = true)
                    .padding(4.dp),
                text = geminiResponse.text, onTextChange = {}
            )
        }
        val toolbarActions = mutableListOf<ToolbarAction>()

        if (LocalFavoritesEnabled.current) {
            val isPinned by viewModel.isPinned.collectAsState(false)
            val favAction = if (isPinned) {
                DefaultToolbarAction(
                    label = stringResource(R.string.menu_favorites_unpin),
                    icon = Icons.Rounded.Star,
                    action = {
                        viewModel.unpin()
                        onBack?.invoke()
                    }
                )
            } else {
                DefaultToolbarAction(
                    label = stringResource(R.string.menu_favorites_pin),
                    icon = Icons.Rounded.StarOutline,
                    action = {
                        viewModel.pin()
                        onBack?.invoke()
                    })
            }
            toolbarActions.add(favAction)
        }

        toolbarActions.add(
            DefaultToolbarAction(
                label = stringResource(R.string.menu_share),
                icon = Icons.Rounded.Share,
                action = {
                    geminiResponse.share(context)
                }
            )
        )

        val sheetManager = LocalBottomSheetManager.current
        toolbarActions.add(DefaultToolbarAction(
            label = stringResource(R.string.menu_customize),
            icon = Icons.Rounded.Edit,
            action = { sheetManager.showCustomizeSearchableModal(geminiResponse) }
        ))

        Toolbar(
            leftActions = if (onBack != null) listOf(
                DefaultToolbarAction(
                    stringResource(id = R.string.menu_back),
                    icon = Icons.Rounded.ArrowBack,
                    action = onBack
                )
            ) else emptyList(),
            rightActions = toolbarActions
        )
    }
}

@Composable
fun GeminiItemGridPopup(
    geminiResponse: GeminiResponse,
    show: MutableTransitionState<Boolean>,
    animationProgress: Float,
    origin: Rect,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        show,
        enter = expandIn(
            animationSpec = tween(300),
            expandFrom = Alignment.Center,
        ) { origin.roundToIntRect().size },
        exit = shrinkOut(
            animationSpec = tween(300),
            shrinkTowards = Alignment.Center,
        ) { origin.roundToIntRect().size },
    ) {
        GeminiItem(
            modifier = Modifier
                .fillMaxWidth(),
            geminiResponse = geminiResponse,
            onBack = onDismiss
        )
    }
}