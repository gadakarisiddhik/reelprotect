package com.example.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActionConfirmDialog
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusProtectedGreen
import com.example.ui.theme.StatusSetupAmber
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ReelGuardViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun FocusScreen(
    viewModel: ReelGuardViewModel
) {
    val focusSession by viewModel.focusSession.collectAsStateWithLifecycle()
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()

    var selectedPresetMinutes by remember { mutableIntStateOf(25) }
    var remainingMs by remember { mutableLongStateOf(focusSession.remainingMs) }

    // Delayed emergency override state
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var overrideCountdown by remember { mutableIntStateOf(5) }
    var isCountingDownOverride by remember { mutableStateOf(false) }

    LaunchedEffect(focusSession) {
        while (focusSession.isActive) {
            remainingMs = focusSession.remainingMs
            if (remainingMs <= 0) {
                viewModel.endFocusSession()
                break
            }
            delay(1000)
        }
    }

    LaunchedEffect(isCountingDownOverride) {
        if (isCountingDownOverride) {
            overrideCountdown = 5
            while (overrideCountdown > 0) {
                delay(1000)
                overrideCountdown--
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Focus Mode",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Timed deep work sessions with friction-delay override protections.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Service warning banner if Accessibility is off
        if (!isServiceActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSetupAmber)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusSetupAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Accessibility Service is not running. Focus sessions cannot redirect video feeds until enabled in Settings.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Timer Display Hero
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (focusSession.isActive) VioletPrimary else DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        val progress = if (focusSession.isActive && focusSession.durationMs > 0) {
                            (focusSession.durationMs - remainingMs).toFloat() / focusSession.durationMs.toFloat()
                        } else {
                            0f
                        }

                        CircularProgressIndicator(
                            progress = { if (focusSession.isActive) progress else 0f },
                            modifier = Modifier.size(190.dp),
                            color = VioletPrimary,
                            strokeWidth = 8.dp,
                            trackColor = DarkSurfaceVariant
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (focusSession.isActive) {
                                val minutes = (remainingMs / 1000) / 60
                                val seconds = (remainingMs / 1000) % 60
                                val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                                Text(
                                    text = timeFormatted,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "REMAINING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VioletPrimary,
                                    letterSpacing = 1.sp
                                )
                            } else {
                                Text(
                                    text = "${selectedPresetMinutes}:00",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SESSION LENGTH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (focusSession.isActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = StatusProtectedGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Strict Focus Protection Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusProtectedGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Emergency unlock with delayed override
                        OutlinedButton(
                            onClick = {
                                showEmergencyDialog = true
                                isCountingDownOverride = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_emergency_end_focus")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Unlock", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(25, 45, 60, 90).forEach { mins ->
                                val isSelected = selectedPresetMinutes == mins
                                Button(
                                    onClick = { selectedPresetMinutes = mins },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) VioletPrimary else DarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("preset_${mins}m")
                                ) {
                                    Text(
                                        "${mins}m",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.startFocusSession(selectedPresetMinutes) },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_start_focus_session")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start ${selectedPresetMinutes}m Session",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Philosophy & Privacy info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Anti-Impulse Protection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "During an active session, ReelGuard blocks feeds and enforces a 5-second reflection delay on cancellation to prevent mindless impulse unlocks.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Emergency Unlock Dialog
    if (showEmergencyDialog) {
        ActionConfirmDialog(
            title = "End Focus Session?",
            message = if (overrideCountdown > 0) {
                "Take a breath. A 5-second anti-impulse pause is active to help preserve your focus.\n\nOverride unlocks in: $overrideCountdown seconds..."
            } else {
                "Are you sure you want to cancel the remaining focus time?"
            },
            confirmText = if (overrideCountdown > 0) "Wait ($overrideCountdown)" else "Confirm Unlock",
            dismissText = "Stay in Focus",
            onConfirm = {
                if (overrideCountdown <= 0) {
                    viewModel.endFocusSession()
                    showEmergencyDialog = false
                    isCountingDownOverride = false
                }
            },
            onDismiss = {
                showEmergencyDialog = false
                isCountingDownOverride = false
            }
        )
    }
}
