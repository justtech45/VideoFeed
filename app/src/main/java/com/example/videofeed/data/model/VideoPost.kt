package com.example.videofeed.data.model

data class VideoPost(
    val tweetId: String,
    val authorHandle: String,
    val authorDisplayName: String,
    val authorAvatarUrl: String?,   // NEW: Avatar of the tweet author
    val retweetedBy: String?,
    val text: String,
    val timestampTitle: String?,
    val videoUrl: String,
    val thumbnailUrl: String?
)
