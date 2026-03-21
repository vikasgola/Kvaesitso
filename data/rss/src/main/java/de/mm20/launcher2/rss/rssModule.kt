package de.mm20.launcher2.rss

import org.koin.dsl.module

val rssModule = module {
    single { RssRepository() }
}
