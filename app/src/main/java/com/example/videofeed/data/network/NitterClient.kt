package com.justtech45.videofeed.data.network

import android.util.Log
import com.justtech45.videofeed.data.parser.NitterMediaParser
import com.justtech45.videofeed.data.parser.ParseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NitterClient(
    private val instanceUrl: String,
    private val http: OkHttpClient = defaultClient()
) {
    companion object {
        private const val TAG = "NitterClient"
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    fun firstPageUrl(username: String, mediaOnly: Boolean = true): String {
        val clean = username.trim().trimStart('@')
        require(clean.matches(Regex("[A-Za-z0-9_]{1,15}"))) { "Invalid username" }
        val suffix = if (mediaOnly) "/media" else ""
        return "${instanceUrl.trimEnd('/')}/$clean$suffix"
    }

    suspend fun fetchPage(url: String): ParseResult = withContext(Dispatchers.IO) {
        // CRITICAL: Nitter only honors the cursor when scroll=true is present.
        // Without it, most instances return page 1 again, so pagination appears
        // to "stop" after the first batch.
        val requestUrl = if (url.contains("cursor=") && !url.contains("scroll=")) {
            url + (if (url.contains('?')) "&" else "?") + "scroll=true"
        } else {
            url
        }

        Log.d(TAG, "GET $requestUrl")

        val request = Request.Builder()
            .url(requestUrl)
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
            )
            .header("Accept", "text/html")
            .build()

        http.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) error("Nitter returned HTTP ${resp.code}")
            val html = resp.body?.string() ?: error("Empty response")
            val result = NitterMediaParser.parse(html, requestUrl)
            Log.d(TAG, "Parsed ${result.videos.size} videos, next=${result.nextPageUrl}")
            result
        }
    }
}
