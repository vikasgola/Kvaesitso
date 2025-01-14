package de.mm20.launcher2.gemini

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import de.mm20.launcher2.icons.ColorLayer
import de.mm20.launcher2.icons.StaticLauncherIcon
import de.mm20.launcher2.icons.TintedIconLayer
import de.mm20.launcher2.ktx.tryStartActivity
import de.mm20.launcher2.search.GeminiResponse
import de.mm20.launcher2.search.SearchableSerializer

internal data class Gemini(
    override val label: String,
    val id: Long,
    override val text: String,
    override val imageUrl: String?,
    override val sourceUrl: String,
    override val sourceName: String,
    val geminiUrl: String,
    override val labelOverride: String? = null,
) : GeminiResponse {

    override val domain: String = Domain

    override fun overrideLabel(label: String): Gemini {
        return this.copy(labelOverride = label)
    }

    override val key = "$domain://$geminiUrl:$id"

    override fun getPlaceholderIcon(context: Context): StaticLauncherIcon {
        return StaticLauncherIcon(
            foregroundLayer = TintedIconLayer(
                icon = ContextCompat.getDrawable(context, R.drawable.ic_file_markup)!!,
                scale = 1f,
                color = 0xFFC1C2C4.toInt(),
            ),
            backgroundLayer = ColorLayer(0xFFC1C2C4.toInt())
        )
    }

    private fun getLaunchIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
    }

    override fun launch(context: Context, options: Bundle?): Boolean {
        return context.tryStartActivity(getLaunchIntent(), options)
    }

    override val canShare: Boolean = true
    override fun share(context: Context) {
        val text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, "${label}\n\n" +
                    "${text.substring(0, 200.coerceAtMost(text.length))}…\n\n" +
                    sourceUrl
        )
        shareIntent.type = "text/plain"
        context.startActivity(Intent.createChooser(shareIntent, null))
    }

    override fun getSerializer(): SearchableSerializer {
        return GeminiSerializer()
    }

    companion object {
        const val Domain = "gemini"
    }
}