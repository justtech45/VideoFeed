package com.justtech45.videofeed.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.justtech45.videofeed.data.model.VideoPost
import com.justtech45.videofeed.ui.theme.*

/* ── Avatar ──────────────────────────────────────────────────────────── */

@Composable
fun AvatarImage(url: String?, handle: String, size: Dp = 36.dp, showMintRing: Boolean = false) {
    var hasError by remember(url) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (showMintRing) {
                    Modifier.background(MintGradient, CircleShape)
                } else Modifier
            )
            .padding(if (showMintRing) 2.dp else 0.dp)
            .clip(CircleShape)
            .background(DarkElevatedHigh)
            .border(1.dp, Hairline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (url != null && !hasError) {
            AsyncImage(
                model = url,
                contentDescription = "Avatar for $handle",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { hasError = true }
            )
        } else {
            Text(
                text = handle.take(1).uppercase().ifBlank { "?" },
                style = MaterialTheme.typography.titleMedium,
                color = Mint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ── Thumbnail ───────────────────────────────────────────────────────── */

@Composable
fun ThumbnailBox(video: VideoPost, modifier: Modifier = Modifier, corner: Dp = 18.dp, playSize: Dp = 56.dp) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(DarkCard)
            .border(1.dp, Hairline, shape)
    ) {
        if (video.thumbnailUrl != null) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(DarkElevated, DarkCard))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    null,
                    tint = TextTertiary.copy(alpha = 0.7f),
                    modifier = Modifier.size(playSize)
                )
            }
        }

        // Cinematic scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Glass play button
        Box(
            Modifier
                .align(Alignment.Center)
                .size(playSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.42f))
                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                null,
                tint = Mint,
                modifier = Modifier.size(playSize / 2)
            )
        }

        if (video.retweetedBy != null) {
            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Repeat, null, tint = Mint, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        video.retweetedBy.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* ── Feed card ───────────────────────────────────────────────────────── */

@Composable
fun VideoCard(video: VideoPost, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = tween(140),
        label = "pressScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Mint.copy(alpha = 0.10f)),
                onClick = onClick
            )
            .padding(vertical = 8.dp)
    ) {
        ThumbnailBox(video, Modifier.padding(horizontal = 16.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AvatarImage(video.authorAvatarUrl, video.authorHandle, 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    video.text.ifBlank { " " },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "@${video.authorHandle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(3.dp).background(TextTertiary, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text("3d ago", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                }
            }
        }
    }
}

/* ── Related card ────────────────────────────────────────────────────── */

@Composable
fun RelatedCard(video: VideoPost, isActive: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(shape)
            .background(if (isActive) Mint.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                1.dp,
                if (isActive) Mint.copy(alpha = 0.45f) else Color.Transparent,
                shape
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ThumbnailBox(
            video,
            Modifier.width(128.dp).height(72.dp),
            corner = 10.dp,
            playSize = 28.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                video.text.ifBlank { "Video by @${video.authorHandle}" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "@${video.authorHandle} · 3d ago",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ── Empty state ─────────────────────────────────────────────────────── */

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Mint.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(78.dp),
                shape = CircleShape,
                color = DarkCard,
                border = BorderStroke(1.dp, MintBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(36.dp), tint = Mint)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.92f)
        )
        if (action != null) {
            Spacer(Modifier.height(26.dp))
            action()
        }
    }
}

/* ── Footer / pagination ─────────────────────────────────────────────── */

@Composable
fun FeedFooter(isLoadingMore: Boolean, error: String?, exhausted: Boolean, videoCount: Int, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoadingMore -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Mint,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Loading more videos…",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
            error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = onLoadMore) {
                    Text("Try again", color = Mint, fontWeight = FontWeight.SemiBold)
                }
            }
            exhausted -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, null, tint = Mint, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "You're all caught up · $videoCount videos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            else -> TextButton(onClick = onLoadMore) {
                Text("Load more videos", color = Mint, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/* ── Skeleton loading ────────────────────────────────────────────────── */

@Composable
fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerShift"
    )
    val brush = Brush.linearGradient(
        colors = listOf(DarkElevated, DarkElevatedHigh, DarkElevated),
        start = Offset(shift * 900f - 450f, 0f),
        end = Offset(shift * 900f, 420f)
    )

    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(18.dp))
                .background(brush)
        )
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(brush))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .height(15.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(9.dp))
                Box(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(brush)
                )
            }
        }
    }
}
