package de.mm20.launcher2.gemini

import android.content.Context
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.search.SearchableDeserializer
import de.mm20.launcher2.search.SearchableSerializer
import org.json.JSONObject

class GeminiSerializer : SearchableSerializer {
    override fun serialize(searchable: SavableSearchable): String {
        searchable as Gemini
        val json = JSONObject()
        json.put("label", searchable.label)
        json.put("text", searchable.text)
        json.put("id", searchable.id)
        json.put("image", searchable.imageUrl)
        json.put("gemini_url", searchable.geminiUrl)
        json.put("url", searchable.sourceUrl)
        return json.toString()
    }

    override val typePrefix: String
        get() = "gemini"
}

class GeminiDeserializer(val context: Context) : SearchableDeserializer {
    override suspend fun deserialize(serialized: String): SavableSearchable? {
        val json = JSONObject(serialized)
        val geminiUrl = json.optString("gemini_url").takeIf { !it.isNullOrBlank() } ?: return null
        val id = json.getLong("id")
        return Gemini(
            label = json.getString("label"),
            text = json.getString("text"),
            id = id,
            imageUrl = json.optString("image"),
            sourceUrl = json.optString("url").takeIf { !it.isNullOrBlank() } ?: "${geminiUrl.padEnd(1, '/')}gemini",
            geminiUrl = geminiUrl,
            sourceName = context.getString(R.string.gemini_source),
        )
    }
}