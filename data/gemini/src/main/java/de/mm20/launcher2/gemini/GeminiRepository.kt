package de.mm20.launcher2.gemini

import android.content.Context
import android.util.Log
import de.mm20.launcher2.crashreporter.CrashReporter
import de.mm20.launcher2.preferences.search.GeminiSearchSettings
import de.mm20.launcher2.search.GeminiResponse
import de.mm20.launcher2.search.SearchableRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


internal class GeminiRepository(
    private val context: Context,
    private val settings: GeminiSearchSettings,
) : SearchableRepository<GeminiResponse> {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val httpClient = OkHttpClient
        .Builder()
        .connectTimeout(10000, TimeUnit.MILLISECONDS)
        .readTimeout(10000, TimeUnit.MILLISECONDS)
        .writeTimeout(10000, TimeUnit.MILLISECONDS)
        .build()

    private lateinit var retrofit: Retrofit

    init {
        scope.launch {
            settings.customUrl
                .distinctUntilChanged()
                .collectLatest {
                    try { retrofit = Retrofit.Builder()
                            .client(httpClient)
                            .baseUrl(it.takeIf { !it.isNullOrBlank() }
                                ?: context.getString(R.string.gemini_url))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                        geminiService = retrofit.create(GeminiApi::class.java)
                    } catch (e: IllegalArgumentException) {
                        CrashReporter.logException(e)
                    }
                }
        }
    }

    private lateinit var geminiService: GeminiApi


    override fun search(query: String, allowNetwork: Boolean): Flow<ImmutableList<Gemini>> {
        if (query.length < 4 || !allowNetwork) return flowOf(persistentListOf())

        return settings.enabled.transformLatest {
            emit(persistentListOf())
            withContext(Dispatchers.IO) {
                httpClient.dispatcher.cancelAll()
            }

            if (!it || !::geminiService.isInitialized) return@transformLatest
            if (query.isBlank()) return@transformLatest

            val results = queryGemini(query)
            Log.i("MM20", results.toString())
            if (results != null) {
                emit(persistentListOf(results))
            }
        }

    }

    private suspend fun queryGemini(query: String): Gemini? {

        val geminiService = geminiService
        val geminiUrl = retrofit.baseUrl().toString()

        val result = try {
            val querycontent = QueryContent(
                contents = listOf(QueryPart(listOf(QueryText(text = query))))
            )
            geminiService.search(querycontent)
        } catch (e: Exception) {
            CrashReporter.logException(e)
            return null
        }

        val text = result.candidates.get(0).content.parts.get(0).text

        return Gemini(
            label = "Google Gemini Response",
            id = 1,
            text = text,
            imageUrl = null,
            sourceUrl = "https://gemini.google.com/",
            geminiUrl = geminiUrl,
            sourceName = context.getString(R.string.gemini_source),
        )
    }

}