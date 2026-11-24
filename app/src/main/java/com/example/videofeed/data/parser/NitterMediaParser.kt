package com.justtech45.videofeed.data.parser

import com.justtech45.videofeed.data.model.VideoPost
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI

data class ParseResult(
    val videos: List<VideoPost>,
    val nextPageUrl: String?,
    val profileAvatarUrl: String?   // NEW: Extracted from the top of the page
)

object NitterMediaParser {
    fun parse(html: String, pageUrl: String): ParseResult {
        val doc = Jsoup.parse(html, pageUrl)
        val realOrigin: String = runCatching {
            val u = URI(pageUrl)
            "${u.scheme}://${u.authority}"
        }.getOrDefault("")

        fun absolute(path: String?): String? {
            if (path.isNullOrBlank()) return null
            return when {
                path.startsWith("http://") || path.startsWith("https://") -> path
                path.startsWith("//") -> "https:$path"
                path.startsWith("/") -> realOrigin + path
                else -> "$realOrigin/$path"
            }
        }

        // NEW: Extract main profile avatar
        val profileAvatar = doc.selectFirst(".profile-card-avatar img")?.attr("src")
            ?.takeIf { it.isNotBlank() }?.let(::absolute)
            ?: doc.selectFirst("meta[property='og:image']")?.attr("content")?.let(::absolute)

        val videos = doc.select(".timeline-item").mapNotNull { item: Element ->
            val video = item.selectFirst(".attachments video") ?: item.selectFirst("video") ?: return@mapNotNull null
            val src = video.selectFirst("source[src]")?.attr("src")
                ?: video.attr("src").takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            val videoUrl = absolute(src) ?: return@mapNotNull null
            val thumb = video.attr("poster").takeIf { it.isNotBlank() }?.let(::absolute)

            val tweetLink = item.selectFirst("a.tweet-link")
            val href = tweetLink?.attr("href").orEmpty()
            val tweetId = href.substringAfter("/status/", "").substringBefore("#").substringBefore("?")

            val handle = item.attr("data-username").ifBlank {
                item.selectFirst("a.username")?.text()?.removePrefix("@").orEmpty()
            }.ifBlank {
                href.trimStart('/').substringBefore('/')
            }

            // NEW: Extract avatar for this specific tweet's author
            val authorAvatar = item.selectFirst(".tweet-avatar img")?.attr("src")
                ?.takeIf { it.isNotBlank() }?.let(::absolute)

            val retweetedBy = item.selectFirst(".retweet-header")
                ?.text()?.trim()
                ?.removeSuffix(" retweeted")?.trim()
                ?.takeIf { it.isNotBlank() }

            val displayName: String = run {
                val el = item.selectFirst(".fullname")
                val own = el?.ownText()?.trim()
                if (!own.isNullOrBlank()) own
                else el?.text()?.trim()?.takeIf { it.isNotBlank() } ?: handle
            }
            val text = item.selectFirst(".tweet-content")?.text()?.trim().orEmpty()
            val dateTitle = item.selectFirst(".tweet-date a")?.attr("title")

            VideoPost(
                tweetId = tweetId,
                authorHandle = handle,
                authorDisplayName = displayName,
                authorAvatarUrl = authorAvatar,
                retweetedBy = retweetedBy,
                text = text,
                timestampTitle = dateTitle,
                videoUrl = videoUrl,
                thumbnailUrl = thumb
            )
        }

        val nextHref = doc.selectFirst(".show-more a, a.more")?.attr("href")?.takeIf { it.isNotBlank() }
        val nextPageUrl = nextHref?.let(::absolute)
        
        return ParseResult(videos, nextPageUrl, profileAvatar)
    }
}
