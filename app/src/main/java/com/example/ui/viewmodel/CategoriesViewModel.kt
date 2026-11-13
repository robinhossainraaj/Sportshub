package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavouriteChannel
import com.example.data.model.DatabaseEntry
import com.example.data.model.Channel
import com.example.data.repository.SportsRepository
import com.example.utils.M3uParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(private val repository: SportsRepository) : ViewModel() {

    private val _dbState = MutableStateFlow<UiState<List<DatabaseEntry>>>(UiState.Loading)
    val dbState: StateFlow<UiState<List<DatabaseEntry>>> = _dbState.asStateFlow()

    private val _channelsState = MutableStateFlow<UiState<List<Channel>>>(UiState.Loading)
    val channelsState: StateFlow<UiState<List<Channel>>> = _channelsState.asStateFlow()

    init {
        loadDatabaseIndex()
    }

    fun loadDatabaseIndex() {
        viewModelScope.launch {
            _dbState.value = UiState.Loading
            repository.getIPTVDatabase()
                .onSuccess {
                    _dbState.value = UiState.Success(it)
                }
                .onFailure {
                    _dbState.value = UiState.Error(it.message ?: "Failed loading playlist database index")
                }
        }
    }

    fun loadChannelsFromPlaylist(playlistUrl: String) {
        viewModelScope.launch {
            _channelsState.value = UiState.Loading
            try {
                // Parse M3U
                val list = M3uParser.parseFromUrl(playlistUrl)
                if (list.isEmpty()) {
                    _channelsState.value = UiState.Error("No channels found in this playlist")
                } else {
                    _channelsState.value = UiState.Success(list)
                }
            } catch (e: Exception) {
                _channelsState.value = UiState.Error(e.message ?: "Failed parsing playlist")
            }
        }
    }

    // Favourites toggles
    fun toggleFavourite(channel: Channel, isFav: Boolean, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val favourite = FavouriteChannel(
                streamUrl = channel.streamUrl,
                name = channel.name,
                logoUrl = channel.logoUrl,
                category = channel.category
            )
            if (isFav) {
                repository.removeFavourite(favourite)
                onSuccess("Removed from Favourites 🗑")
            } else {
                repository.insertFavourite(favourite)
                onSuccess("Added to Favourites ⭐")
            }
        }
    }

    fun isChannelFavourite(streamUrl: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.isFavourite(streamUrl))
        }
    }
}
