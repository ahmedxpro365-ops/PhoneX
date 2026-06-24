package com.phonex.app.viewmodel

import android.app.Application
import android.telecom.Call
import android.telecom.VideoProfile
import androidx.lifecycle.AndroidViewModel
import com.phonex.app.telecom.CallManager
import kotlinx.coroutines.flow.StateFlow

import android.os.Build

val Call.safeState: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.details?.state ?: Call.STATE_DISCONNECTED
    } else {
        @Suppress("DEPRECATION")
        this.state
    }

class CallViewModel(application: Application) : AndroidViewModel(application) {
    val currentCall: StateFlow<Call?> = CallManager.currentCall

    fun answerCall() {
        CallManager.answerCall()
    }

    fun rejectCall() {
        CallManager.rejectCall()
    }

    fun disconnectCall() {
        CallManager.disconnectCall()
    }
    
    fun toggleMute() {
        CallManager.toggleMute()
    }
    
    fun toggleSpeaker() {
        CallManager.toggleSpeaker()
    }
    
    fun toggleHold() {
        val call = currentCall.value ?: return
        if (call.safeState == Call.STATE_HOLDING) {
            call.unhold()
        } else {
            call.hold()
        }
    }
    
    fun playDtmf(char: Char) {
        currentCall.value?.playDtmfTone(char)
        currentCall.value?.stopDtmfTone()
    }
}
