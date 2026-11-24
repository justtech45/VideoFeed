package com.example.videofeed.data

import com.example.videofeed.data.network.NitterClient

class ProfileVideoRepository(private val client: NitterClient) {
    fun firstUrl(username: String): String = client.firstPageUrl(username, mediaOnly = true)
    fun fallbackUrl(username: String): String = client.firstPageUrl(username, mediaOnly = false)
    suspend fun page(url: String) = client.fetchPage(url)
}
