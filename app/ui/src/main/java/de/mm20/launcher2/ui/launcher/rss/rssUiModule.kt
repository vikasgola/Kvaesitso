package de.mm20.launcher2.ui.launcher.rss

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val rssUiModule = module {
    single(createdAtStart = true) { RssReaderStartup(androidContext()) }
    viewModel { RssReaderViewModel(get(), get()) }
}

internal class RssReaderStartup(context: Context) {
    init {
        AdBlockHosts.warmUp(context.applicationContext)
    }
}
