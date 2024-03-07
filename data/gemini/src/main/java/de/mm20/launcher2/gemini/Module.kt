package de.mm20.launcher2.gemini

import de.mm20.launcher2.search.GeminiResponse
import de.mm20.launcher2.search.SearchableDeserializer
import de.mm20.launcher2.search.SearchableRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val geminiModule = module {
    single<SearchableRepository<GeminiResponse>>(named<GeminiResponse>()) { GeminiRepository(androidContext(), get()) }
    factory<SearchableDeserializer>(named(Gemini.Domain)) { GeminiDeserializer(androidContext()) }
}