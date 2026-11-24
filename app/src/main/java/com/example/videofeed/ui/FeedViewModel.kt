package com.justtech45.videofeed.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justtech45.videofeed.data.ProfileVideoRepository
import com.justtech45.videofeed.data.model.VideoPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedState(
    val username: String = "",
    val profileAvatarUrl: String? = null, // NEW
    val videos: List<VideoPost> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val exhausted: Boolean = false
)

class FeedViewModel(
    private val repo: ProfileVideoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state = _state.asStateFlow()

    private var nextUrl: String? = null
    private var loading = false
    private val seenUrls = mutableSetOf<String>()

    fun start(username: String) {
        if (_state.value.username == username && _state.value.videos.isNotEmpty()) return
        _state.value = FeedState(username = username, isLoading = true)
        nextUrl = repo.firstUrl(username)
        seenUrls.clear()
        viewModelScope.launch { fetchNext() }
    }

    fun loadMoreIfNearEnd(currentIndex: Int) {
        val s = _state.value
        if (s.exhausted || loading) return
        if (currentIndex >= s.videos.size - 5) {
            viewModelScope.launch { fetchNext() }
        }
    }

    fun loadMoreManually() {
        if (loading) return
        if (nextUrl == null) {
            _state.update { it.copy(exhausted = false, error = null) }
            nextUrl = repo.fallbackUrl(_state.value.username)
            seenUrls.clear()
        }
        viewModelScope.launch { fetchNext() }
    }

    fun retry() {
        val u = _state.value.username
        if (u.isNotBlank()) {
            _state.value = FeedState(username = u, isLoading = true)
            nextUrl = repo.firstUrl(u)
            seenUrls.clear()
            viewModelScope.launch { fetchNext() }
        }
    }

    private suspend fun fetchNext() {
        val url = nextUrl
        if (url == null) {
            _state.update { it.copy(exhausted = true, isLoading = false, isLoadingMore = false) }
            return
        }
        if (loading) return
        if (url in seenUrls) {
            _state.update { it.copy(exhausted = true, isLoading = false, isLoadingMore = false) }
            return
        }

        seenUrls.add(url)
        loading = true
        _state.update { it.copy(isLoadingMore = true, error = null) }

        try {
            kotlinx.coroutines.delay(400)
            val result = repo.page(url)
            val existing = _state.value.videos.map { it.tweetId }.toSet()
            val fresh = result.videos.filter { it.tweetId.isNotBlank() && it.tweetId !in existing }

            val next = result.nextPageUrl
            nextUrl = next?.takeIf { it !in seenUrls }

            _state.update {
                it.copy(
                    profileAvatarUrl = it.profileAvatarUrl ?: result.profileAvatarUrl, // Keep the first one found
                    videos = it.videos + fresh,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    exhausted = nextUrl == null
                )
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(isLoadingMore = false, isLoading = false, error = e.message) }
        } finally {
            loading = false
        }
    }
}
