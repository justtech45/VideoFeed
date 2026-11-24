package com.justtech45.videofeed.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.justtech45.videofeed.data.model.VideoPost
import com.justtech45.videofeed.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.delay

private const val SEEK_STEP_MS = 15_000L
private val PlaybackSpeeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

/* ───────────────────────────── Screen ───────────────────────────── */

@Composable
fun VideoPlayerScreen(vm: FeedViewModel, initialIndex: Int, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val state by vm.state.collectAsStateWithLifecycle()
    var currentIndex by remember(initialIndex) { mutableIntStateOf(initialIndex) }
    var isFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Back press exits fullscreen first, then leaves the screen
    BackHandler(enabled = isFullscreen) { isFullscreen = false }

    if (state.videos.isEmpty() || currentIndex !in state.videos.indices) {
        Box(Modifier.fillMaxSize().background(TrueBlack), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Mint, strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Loading video…",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
        return
    }

    val current = state.videos[currentIndex]
    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(Unit) {
        onDispose { player.pause(); player.release() }
    }

    LaunchedEffect(current.videoUrl) {
        player.setMediaItem(MediaItem.fromUri(current.videoUrl))
        player.prepare()
        player.play()
    }

    DisposableEffect(current.videoUrl) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    val next = currentIndex + 1
                    if (next < state.videos.size) currentIndex = next
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> player.pause()
                Lifecycle.Event.ON_START -> if (!isFullscreen) player.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(currentIndex) { vm.loadMoreIfNearEnd(currentIndex) }

    // Landscape fullscreen: rotate when entering, portrait when exiting,
    // and restore the device's normal orientation when leaving the player.
    val activity = context.findActivity()
    LaunchedEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(TrueBlack)) {
        if (isFullscreen) {
            // FULLSCREEN MODE
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { it.player = null }
                )
                CustomPlayerControls(
                    player = player,
                    title = current.text.ifBlank { "Video by @${current.authorHandle}" },
                    videoUrl = current.videoUrl,
                    isFullscreen = isFullscreen,
                    onFullscreenToggle = { isFullscreen = false },
                    onBack = onBack
                )
            }
        } else {
            // NORMAL MODE
            Column(
                Modifier
                    .fillMaxSize()
                    .background(TrueBlack)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        onRelease = { it.player = null }
                    )
                    CustomPlayerControls(
                        player = player,
                        title = current.text.ifBlank { "Video by @${current.authorHandle}" },
                        videoUrl = current.videoUrl,
                        isFullscreen = isFullscreen,
                        onFullscreenToggle = { isFullscreen = true },
                        onBack = onBack
                    )
                }

                // Soft hairline under the player
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Hairline, Color.Transparent)
                            )
                        )
                )

                val infoListState = rememberLazyListState()
                LaunchedEffect(currentIndex) { infoListState.scrollToItem(0) }

                LazyColumn(
                    state = infoListState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item { VideoInfoBlock(current) }
                    if (state.videos.size > 1) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, Hairline, Color.Transparent)
                                        )
                                    )
                            )
                            Spacer(Modifier.height(16.dp))
                            RelatedSectionHeader(
                                username = state.username,
                                count = state.videos.size - 1
                            )
                        }
                    }
                    itemsIndexed(
                        items = state.videos,
                        key = { _, v -> v.tweetId + "|" + v.videoUrl },
                    ) { idx, video ->
                        if (idx != currentIndex) {
                            RelatedCard(video = video, isActive = false, onClick = { currentIndex = idx })
                        }
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ───────────────────────────── Controls ───────────────────────────── */

@Composable
private fun CustomPlayerControls(
    player: ExoPlayer,
    title: String,
    videoUrl: String,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var isBuffering by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }

    var playbackSpeed by remember { mutableFloatStateOf(player.playbackParameters.speed) }
    var speedMenuOpen by remember { mutableStateOf(false) }

    val playInteraction = remember { MutableInteractionSource() }
    val playPressed by playInteraction.collectIsPressedAsState()
    val playScale by animateFloatAsState(
        targetValue = if (playPressed) 0.92f else 1f,
        animationSpec = tween(120),
        label = "playScale"
    )

    // Wider side margins in fullscreen (camera cutouts / rounded corners)
    val sideInset = if (isFullscreen) 28.dp else 16.dp

    // Immersive mode for fullscreen. Re-applied after rotation and restored when leaving.
    val configuration = LocalConfiguration.current
    DisposableEffect(isFullscreen, configuration.orientation) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (isFullscreen) {
            controller?.hide(WindowInsetsCompat.Type.systemBars())
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(controlsVisible, isPlaying, isDragging, speedMenuOpen) {
        if (controlsVisible && isPlaying && !isDragging && !speedMenuOpen) {
            delay(3000)
            controlsVisible = false
        }
    }

    LaunchedEffect(player) {
        while (true) {
            isBuffering = player.playbackState == Player.STATE_BUFFERING
            if (!isDragging) {
                isPlaying = player.isPlaying
                position = player.currentPosition
                bufferedPosition = player.bufferedPosition
                duration = player.duration.coerceAtLeast(0L)
            }
            delay(250)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { controlsVisible = !controlsVisible }
            )
    ) {
        // Fading controls overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Black.copy(alpha = 0.74f),
                                0.30f to Color.Transparent,
                                0.62f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.84f)
                            )
                        )
                    )
            ) {
                // ZONE 1: TOP BAR
                Row(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        // In normal mode the screen already pads for the status bar
                        .then(if (isFullscreen) Modifier.statusBarsPadding() else Modifier)
                        .padding(horizontal = sideInset - 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassIconButton(
                            icon = Icons.Rounded.Download,
                            contentDescription = "Download",
                            onClick = { downloadVideo(context, videoUrl, title) }
                        )
                        GlassIconButton(
                            icon = if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit fullscreen" else "Fullscreen",
                            onClick = onFullscreenToggle
                        )
                    }
                }

                // ZONE 2: CENTER ROW
                Row(
                    Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeekButton(Icons.Rounded.FastRewind, "15s", "Rewind 15 seconds") {
                        player.seekTo((player.currentPosition - SEEK_STEP_MS).coerceAtLeast(0))
                    }

                    Box(
                        Modifier
                            .size(68.dp)
                            .graphicsLayer { scaleX = playScale; scaleY = playScale }
                            .shadow(
                                elevation = 18.dp,
                                shape = CircleShape,
                                ambientColor = Mint.copy(alpha = 0.50f),
                                spotColor = Mint.copy(alpha = 0.50f)
                            )
                            .background(MintGradient, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = playInteraction,
                                indication = null
                            ) {
                                if (isPlaying) {
                                    player.pause()
                                } else {
                                    // Replay from the start if the video already ended
                                    if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
                                    player.play()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            if (isPlaying) "Pause" else "Play",
                            tint = TextOnMint,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    SeekButton(Icons.Rounded.FastForward, "15s", "Forward 15 seconds") {
                        val target = player.currentPosition + SEEK_STEP_MS
                        player.seekTo(if (duration > 0) target.coerceAtMost(duration) else target)
                    }
                }

                // ZONE 3: BOTTOM GROUP
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        // In normal mode the player sits at the top, so no nav-bar padding here
                        .then(if (isFullscreen) Modifier.navigationBarsPadding().padding(bottom = 8.dp) else Modifier)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = sideInset),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val timeStyle = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontFeatureSettings = "tnum"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatTime(if (isDragging) dragPosition else position),
                                color = Color.White,
                                style = timeStyle,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "  /  ${formatTime(duration)}",
                                color = Color.White.copy(alpha = 0.7f),
                                style = timeStyle
                            )
                        }

                        // Playback speed
                        Box {
                            Row(
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.38f))
                                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                                    .clickable { speedMenuOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Speed,
                                    "Playback speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    formatSpeed(playbackSpeed),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            DropdownMenu(
                                expanded = speedMenuOpen,
                                onDismissRequest = { speedMenuOpen = false },
                                modifier = Modifier.background(DarkElevated)
                            ) {
                                PlaybackSpeeds.forEach { speed ->
                                    val selected = speed == playbackSpeed
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                formatSpeed(speed),
                                                color = if (selected) Mint else TextPrimary,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (selected) Icon(
                                                Icons.Rounded.Check,
                                                null,
                                                tint = Mint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        onClick = {
                                            player.setPlaybackSpeed(speed)
                                            playbackSpeed = speed
                                            speedMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    CustomSeekBar(
                        position = position,
                        bufferedPosition = bufferedPosition,
                        duration = duration,
                        onSeek = { player.seekTo(it) },
                        onDraggingChanged = { isDragging = it; controlsVisible = true },
                        onDragPositionChanged = { dragPosition = it },
                        modifier = Modifier.padding(horizontal = sideInset)
                    )
                }
            }
        }

        // Buffering indicator (behind the play button, or alone when controls are hidden)
        if (isBuffering) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(84.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(46.dp),
                    color = Mint,
                    strokeWidth = 3.dp
                )
            }
        }

        // Thin progress line (when controls are hidden)
        if (!controlsVisible && duration > 0) {
            val progress = (position.toFloat() / duration).coerceIn(0f, 1f)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White.copy(alpha = 0.18f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Mint)
                )
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.40f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun SeekButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.32f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CustomSeekBar(
    position: Long,
    bufferedPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onDraggingChanged: (Boolean) -> Unit,
    onDragPositionChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = (if (isDragging) dragProgress else if (duration > 0) position.toFloat() / duration else 0f).coerceIn(0f, 1f)
    val bufferedProgress = (if (duration > 0) bufferedPosition.toFloat() / duration else 0f).coerceIn(0f, 1f)
    val thumbSize by animateDpAsState(if (isDragging) 18.dp else 13.dp, animationSpec = tween(120), label = "thumb")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(duration) {
                if (duration <= 0) return@pointerInput
                detectTapGestures { offset ->
                    val tapProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek((tapProgress * duration).toLong())
                }
            }
            .pointerInput(duration) {
                if (duration <= 0) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        onDraggingChanged(true)
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onDragPositionChanged((dragProgress * duration).toLong())
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onDragPositionChanged((dragProgress * duration).toLong())
                    },
                    onDragEnd = {
                        isDragging = false
                        onDraggingChanged(false)
                        onSeek((dragProgress * duration).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                        onDraggingChanged(false)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            val trackHeight = (if (isDragging) 6.dp else 4.dp).toPx()
            val thumbCenter = Offset(size.width * currentProgress, centerY)

            // Inactive track
            drawLine(
                color = Color.White.copy(alpha = 0.22f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Buffered track
            drawLine(
                color = Color.White.copy(alpha = 0.38f),
                start = Offset(0f, centerY),
                end = Offset(size.width * bufferedProgress, centerY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Active track
            drawLine(
                color = Mint,
                start = Offset(0f, centerY),
                end = thumbCenter,
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Soft halo while dragging
            if (isDragging) {
                drawCircle(
                    color = Mint.copy(alpha = 0.25f),
                    radius = thumbSize.toPx(),
                    center = thumbCenter
                )
            }

            // Thumb
            drawCircle(
                color = Mint,
                radius = thumbSize.toPx() / 2,
                center = thumbCenter
            )
        }
    }
}

/* ───────────────────────────── Helpers ───────────────────────────── */

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun downloadVideo(context: Context, url: String, title: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("Downloading video...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "VideoFeed_${System.currentTimeMillis()}.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openOnX(context: Context, video: VideoPost) {
    try {
        val uri = Uri.parse("https://x.com/${video.authorHandle}/status/${video.tweetId}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(Locale.US, h, m, s)
    else "%02d:%02d".format(Locale.US, m, s)
}

private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

/* ───────────────────────────── Info block ───────────────────────────── */

@Composable
private fun VideoInfoBlock(video: VideoPost) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = video.text.ifBlank { "Video by @${video.authorHandle}" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            lineHeight = 27.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AvatarImage(video.authorAvatarUrl, video.authorHandle, 44.dp, showMintRing = true)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    video.authorDisplayName.ifBlank { "@${video.authorHandle}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append("@${video.authorHandle}")
                        if (video.retweetedBy != null) append("  ·  via ${video.retweetedBy}")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            OutlinedButton(
                onClick = { openOnX(context, video) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Mint.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Mint),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Open on X",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (!video.timestampTitle.isNullOrBlank()) {
            Row {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkElevated,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            video.timestampTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedSectionHeader(username: String, count: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(22.dp)
                .background(MintGradient, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                "More from @$username",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                if (count > 0) "$count more video${if (count == 1) "" else "s"}" else "Keep watching",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}
