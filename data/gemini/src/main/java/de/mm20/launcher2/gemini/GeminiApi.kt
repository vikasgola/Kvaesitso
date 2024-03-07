package de.mm20.launcher2.gemini

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

data class GeminiSearchResult(
    val candidates: List<GeminiSearchResultContent>,
)

data class GeminiSearchResultContent(
    val content: GeminiSearchResultParts,
)

data class GeminiSearchResultParts(
    val parts: List<GeminiSearchResultText>
)

data class GeminiSearchResultText(
    val text: String
)


data class QueryText(
    @SerializedName("text") val text: String,
)

data class QueryPart(
    @SerializedName("parts") val parts: List<QueryText>,
)

data class QueryContent(
    @SerializedName("contents") val contents: List<QueryPart>
)

interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-pro:generateContent?key=GEMINI_KEY")
    suspend fun search(@Body query_content: QueryContent): GeminiSearchResult
}