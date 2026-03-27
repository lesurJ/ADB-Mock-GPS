package com.adbmockgps

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    broadcastStateRepository: BroadcastStateRepository
) : ViewModel() {
    
    val lastBroadcastInfo: StateFlow<LastBroadcastInfo?> = 
        broadcastStateRepository.lastBroadcast
}
