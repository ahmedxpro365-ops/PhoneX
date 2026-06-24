package com.phonex.app.telecom

import android.telecom.Call
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PhoneXInCallService : InCallService() {
    override fun onCreate() {
        super.onCreate()
        CallManager.setService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        CallManager.setService(null)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.addCall(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.removeCall(call)
    }
}

@Suppress("DEPRECATION")
object CallManager {
    private var inCallService: InCallService? = null
    
    fun setService(service: InCallService?) {
        inCallService = service
    }

    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall.asStateFlow()

    fun addCall(call: Call) {
        _currentCall.value = call
        call.registerCallback(callCallback)
    }

    fun removeCall(call: Call) {
        if (_currentCall.value == call) {
            _currentCall.value = null
        }
        call.unregisterCallback(callCallback)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (_currentCall.value == call) {
                _currentCall.value = null
                _currentCall.value = call
            }
        }
    }
    
    fun answerCall() {
        _currentCall.value?.answer(0)
    }
    
    fun rejectCall() {
        _currentCall.value?.reject(false, null)
    }
    
    fun disconnectCall() {
        _currentCall.value?.disconnect()
    }

    fun toggleMute() {
        val currentMute = inCallService?.callAudioState?.isMuted ?: false
        inCallService?.setMuted(!currentMute)
    }

    fun toggleSpeaker() {
        val currentRoute = inCallService?.callAudioState?.route
        if (currentRoute == android.telecom.CallAudioState.ROUTE_SPEAKER) {
            inCallService?.setAudioRoute(android.telecom.CallAudioState.ROUTE_EARPIECE)
        } else {
            inCallService?.setAudioRoute(android.telecom.CallAudioState.ROUTE_SPEAKER)
        }
    }
}
