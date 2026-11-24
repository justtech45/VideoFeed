package com.example.videofeed

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videofeed.data.ProfileVideoRepository
import com.example.videofeed.data.network.NitterClient
import com.example.videofeed.ui.*
import com.example.videofeed.ui.theme.TrueBlack
import com.example.videofeed.ui.theme.VideoFeedTheme

private const val INSTANCE = "https://xitter.cf"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        setContent {
            VideoFeedTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = TrueBlack) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    var username by rememberSaveable { mutableStateOf<String?>(null) }
    var playingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val currentUsername = username

    if (currentUsername == null) {
        SearchEntry(onStart = { username = it })
        return
    }

    val factory = remember(currentUsername) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val client = NitterClient(INSTANCE)
                return FeedViewModel(ProfileVideoRepository(client)) as T
            }
        }
    }
    val vm: FeedViewModel = viewModel(key = currentUsername, factory = factory)
    LaunchedEffect(currentUsername) { vm.start(currentUsername) }

    val idx = playingIndex
    if (idx == null) {
        VideoListScreen(
            username = currentUsername,
            vm = vm,
            onOpenVideo = { playingIndex = it },
            onBack = { username = null },
        )
    } else {
        VideoPlayerScreen(
            vm = vm,
            initialIndex = idx,
            onBack = { playingIndex = null },
        )
    }
}
