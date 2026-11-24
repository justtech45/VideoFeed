package com.example.videofeed.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.videofeed.ui.theme.*
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    username: String,
    vm: FeedViewModel,
    onOpenVideo: (Int) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { idx -> if (idx != null) vm.loadMoreIfNearEnd(idx) }
    }

    Scaffold(
        containerColor = TrueBlack,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarImage(
                                state.profileAvatarUrl,
                                username,
                                38.dp,
                                showMintRing = true
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "@$username",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (state.videos.isNotEmpty()) {
                                    Spacer(Modifier.height(3.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Mint.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            if (state.exhausted) "${state.videos.size} videos"
                                            else "${state.videos.size}+ videos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Mint,
                                            modifier = Modifier.padding(
                                                horizontal = 7.dp,
                                                vertical = 2.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = DarkElevated,
                                contentColor = TextPrimary
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TrueBlack),
                )
                // Hairline separator under the app bar
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
            }
        }
    ) { padding ->
        when {
            state.isLoading && state.videos.isEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    repeat(5) { item { SkeletonCard() } }
                }
            }
            state.error != null && state.videos.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    EmptyState(
                        icon = Icons.Rounded.Refresh,
                        title = "Couldn't load videos",
                        message = state.error ?: "Unknown error",
                        action = {
                            Button(
                                onClick = { vm.retry() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Mint,
                                    contentColor = TextOnMint
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 22.dp,
                                    vertical = 12.dp
                                )
                            ) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Try again", fontWeight = FontWeight.Bold)
                            }
                        },
                    )
                }
            }
            state.videos.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    EmptyState(
                        icon = Icons.Rounded.VideocamOff,
                        title = "No videos yet",
                        message = "@$username hasn't posted any videos we could find.",
                        action = {
                            TextButton(onClick = onBack) {
                                Text("Try another profile", color = Mint, fontWeight = FontWeight.SemiBold)
                            }
                        },
                    )
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
                ) {
                    item {
                        SectionHeader("LATEST VIDEOS")
                    }
                    itemsIndexed(
                        items = state.videos,
                        key = { _, v -> v.tweetId + "|" + v.videoUrl },
                    ) { index, video ->
                        VideoCard(video = video, onClick = { onOpenVideo(index) })
                    }
                    item {
                        FeedFooter(
                            isLoadingMore = state.isLoadingMore,
                            error = state.error,
                            exhausted = state.exhausted,
                            videoCount = state.videos.size,
                            onLoadMore = { vm.loadMoreManually() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(12.dp)
                .background(MintGradient, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
