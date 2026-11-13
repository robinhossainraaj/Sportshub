package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.repository.SportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel(private val repository: SportsRepository) : ViewModel() {

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> = _isScreenLocked.asStateFlow()

    private val _resizeMode = MutableStateFlow(0) // 0: Fit, 1: Fill, 2: Zoom
    val resizeMode: StateFlow<Int> = _resizeMode.asStateFlow()

    private val _activeSourceIndex = MutableStateFlow(0)
    val activeSourceIndex: StateFlow<Int> = _activeSourceIndex.asStateFlow()

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleScreenLock() {
        _isScreenLocked.value = !_isScreenLocked.value
    }

    fun cycleResizeMode() {
        _resizeMode.value = (_resizeMode.value + 1) % 3
    }

    fun selectSource(index: Int) {
        _activeSourceIndex.value = index
    }
}
