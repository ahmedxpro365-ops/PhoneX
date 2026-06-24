package com.phonex.app.ui.screens

import android.telecom.Call
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phonex.app.viewmodel.CallViewModel
import com.phonex.app.viewmodel.safeState
import kotlinx.coroutines.delay

@Composable
fun InCallScreen(viewModel: CallViewModel) {
    val call by viewModel.currentCall.collectAsState()

    if (call == null) return

    val state = call?.safeState ?: Call.STATE_DISCONNECTED
    val isIncoming = state == Call.STATE_RINGING
    val isDialing = state == Call.STATE_DIALING || state == Call.STATE_CONNECTING
    val details = call?.details
    val handle = details?.handle?.schemeSpecificPart ?: "Unknown Caller"
    val callerName = details?.callerDisplayName ?: handle

    var callDuration by remember { mutableStateOf(0L) }
    var isRecording by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state == Call.STATE_ACTIVE) {
            while (true) {
                delay(1000)
                callDuration++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E2130),
            Color(0xFF0D0F14)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Top Area: SIM Info & Recording Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "SIM 1",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Red.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REC",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Avatar Area
            Box(contentAlignment = Alignment.Center) {
                if (isDialing || isIncoming) {
                    // Pulse Effect
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale * 0.9f)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFF32374E)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Caller Details
            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (callerName != handle) {
                Text(
                    text = handle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Call State / Duration
            val stateText = when (state) {
                Call.STATE_RINGING -> "Incoming call..."
                Call.STATE_DIALING, Call.STATE_CONNECTING -> "Calling..."
                Call.STATE_ACTIVE -> String.format("%02d:%02d", callDuration / 60, callDuration % 60)
                Call.STATE_HOLDING -> "On Hold"
                Call.STATE_DISCONNECTED -> "Call Ended"
                else -> "Connecting..."
            }

            Text(
                text = stateText,
                style = MaterialTheme.typography.titleMedium,
                color = if (state == Call.STATE_ACTIVE) Color(0xFF81C784) else Color.White.copy(alpha = 0.7f),
                fontWeight = if (state == Call.STATE_ACTIVE) FontWeight.Medium else FontWeight.Normal
            )

            Spacer(modifier = Modifier.weight(1f))

            // Animated Controls & Keypad Area
            AnimatedContent(
                targetState = showKeypad,
                transitionSpec = {
                    slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                            slideOutVertically(targetOffsetY = { it }) + fadeOut()
                }
            ) { keypadVisible ->
                if (keypadVisible) {
                    InCallKeypad(
                        onKeyPressed = { viewModel.playDtmf(it) },
                        onHideKeypad = { showKeypad = false }
                    )
                } else {
                    if (!isIncoming) {
                        InCallControls(
                            viewModel = viewModel,
                            state = state,
                            isRecording = isRecording,
                            onToggleRecord = { isRecording = !isRecording },
                            onShowKeypad = { showKeypad = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Actions (Answer/Decline or End Call)
            if (isIncoming) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CallActionButton(
                        icon = Icons.Default.CallEnd,
                        color = Color(0xFFF44336),
                        onClick = { viewModel.rejectCall() }
                    )
                    CallActionButton(
                        icon = Icons.Default.Call,
                        color = Color(0xFF4CAF50),
                        onClick = { viewModel.answerCall() }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CallActionButton(
                        icon = Icons.Default.CallEnd,
                        color = Color(0xFFF44336),
                        onClick = { viewModel.disconnectCall() },
                        size = 72.dp
                    )
                }
            }
        }
    }
}

@Composable
fun InCallControls(
    viewModel: CallViewModel,
    state: Int,
    isRecording: Boolean,
    onToggleRecord: () -> Unit,
    onShowKeypad: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ControlIconButton(
                icon = Icons.Default.FiberManualRecord,
                label = "Record",
                isActive = isRecording,
                activeColor = Color.Red,
                onClick = onToggleRecord
            )
            ControlIconButton(
                icon = Icons.Default.Pause,
                label = "Hold",
                isActive = state == Call.STATE_HOLDING,
                onClick = { viewModel.toggleHold() }
            )
            ControlIconButton(
                icon = Icons.Default.PersonAdd,
                label = "Add call",
                onClick = { /* Simulated */ }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ControlIconButton(
                icon = Icons.Default.MicOff,
                label = "Mute",
                onClick = { viewModel.toggleMute() }
            )
            ControlIconButton(
                icon = Icons.Default.Dialpad,
                label = "Keypad",
                onClick = onShowKeypad
            )
            ControlIconButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "Speaker",
                onClick = { viewModel.toggleSpeaker() }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ControlIconButton(
                icon = Icons.Default.Bluetooth,
                label = "Bluetooth",
                onClick = { /* Simulated */ }
            )
            ControlIconButton(
                icon = Icons.Default.NoteAlt,
                label = "Notes",
                onClick = { /* Simulated */ }
            )
            ControlIconButton(
                icon = Icons.Default.Contacts,
                label = "Contacts",
                onClick = { /* Simulated */ }
            )
        }
    }
}

@Composable
fun ControlIconButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) activeColor else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = label,
            color = if (isActive) activeColor else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    Surface(
        shape = CircleShape,
        color = color,
        shadowElevation = 8.dp,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}

@Composable
fun InCallKeypad(
    onKeyPressed: (Char) -> Unit,
    onHideKeypad: () -> Unit
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf('*', '0', '#')
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    KeypadButton(key = key, onClick = { onKeyPressed(key) })
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onHideKeypad)
        ) {
            Text(
                text = "Hide",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun KeypadButton(key: Char, onClick: () -> Unit) {
    val letters = when (key) {
        '2' -> "ABC"
        '3' -> "DEF"
        '4' -> "GHI"
        '5' -> "JKL"
        '6' -> "MNO"
        '7' -> "PQRS"
        '8' -> "TUV"
        '9' -> "WXYZ"
        else -> ""
    }

    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

