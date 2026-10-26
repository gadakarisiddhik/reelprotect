package com.example.ui.dashboard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BlockEventEntity
import com.example.data.preferences.FocusSessionData
import com.example.model.SupportedApp
import com.example.ui.components.ActionConfirmDialog
import com.example.ui.components.AppBrandBadge
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.StatusAllowedSlate
import com.example.ui.theme.StatusProtectedGreen
import com.example.ui.theme.StatusSetupAmber
import com.example.ui.theme.VioletContainer
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ReelGuardViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ReelGuardViewModel,
    onNavigateToApps: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToAppDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
    val supportedApps by viewModel.supportedApps.collectAsStateWithLifecycle()
    val focusSession by viewModel.focusSession.collectAsStateWithLifecycle()
    val todayBlockedCount by viewModel.todayBlockedCount.collectAsStateWithLifecycle()
    val recentEvents by viewModel.recentEvents.collectAsStateWithLifecycle()

    val blockedAppsCount = supportedApps.count { it.isBlocked }
    val allowedAppsCount = supportedApps.count { !it.isBlocked }

    var showBlockAllDialog by remember { mutableStateOf(false) }
    var showAllowAllDialog by remember { mutableStateOf(false) }

    var remainingMs by remember { mutableLongStateOf(focusSession.remainingMs) }
    LaunchedEffect(focusSession) {
        while (focusSession.isActive) {
            remainingMs = focusSession.remainingMs
            if (remainingMs <= 0) break
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DashboardHeader()
        }

        // Hero Status Card
        item {
            HeroStatusCard(
                isServiceActive = isServiceActive,
                blockedCount = blockedAppsCount,
                onOpenSettings = {
                    openAccessibilitySettings(context)
                }
            )
        }

        // Quick Metrics Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Feeds Blocked",
                    value = "$blockedAppsCount / ${supportedApps.size}",
                    subtitle = "Selected to block",
                    iconColor = VioletPrimary,
                    testTag = "metric_blocked_feeds"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Today Blocked",
                    value = "$todayBlockedCount",
                    subtitle = "Real measured events",
                    iconColor = StatusProtectedGreen,
                    testTag = "metric_today_events"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Feeds Allowed",
                    value = "$allowedAppsCount",
                    subtitle = "Unrestricted",
                    iconColor = StatusAllowedSlate,
                    testTag = "metric_allowed_feeds"
                )
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showBlockAllDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_block_all"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Block All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { showAllowAllDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_allow_all"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Allow All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Focus Session Banner
        item {
            FocusSessionDashboardCard(
                focusSession = focusSession,
                remainingMs = remainingMs,
                onNavigateToFocus = onNavigateToFocus,
                onQuickStart = { minutes -> viewModel.startFocusSession(minutes) },
                onEndSession = { viewModel.endFocusSession() }
            )
        }

        // Section: Supported Apps Quick Glance
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Protections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onNavigateToApps,
                    modifier = Modifier.testTag("dashboard_view_all_apps")
                ) {
                    Text("View All", color = VioletPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View all",
                        tint = VioletPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // List top 3 apps
        items(supportedApps.take(3)) { app ->
            AppQuickRow(
                app = app,
                isServiceActive = isServiceActive,
                onToggle = { blocked -> viewModel.toggleAppBlocked(app.id, blocked) },
                onClick = { onNavigateToAppDetail(app.id) }
            )
        }

        // Recent Block Events
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Interceptions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.simulateTestDetection("youtube") },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("test_simulation_button")
                ) {
                    Text(
                        text = "Simulate Block",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (recentEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StatusAllowedSlate,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No blocked feeds today",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When you open a blocked Shorts or Reels feed, ReelGuard automatically redirects you back.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentEvents.take(4)) { event ->
                RecentEventCard(event)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showBlockAllDialog) {
        ActionConfirmDialog(
            title = "Block All Feeds?",
            message = "This will set YouTube Shorts, Instagram Reels, Facebook Reels, TikTok, and Snapchat Spotlight to Blocked. You can still customize each app independently later.",
            confirmText = "Block All",
            onConfirm = {
                viewModel.blockAllApps()
                showBlockAllDialog = false
            },
            onDismiss = { showBlockAllDialog = false }
        )
    }

    if (showAllowAllDialog) {
        ActionConfirmDialog(
            title = "Allow All Feeds?",
            message = "This will turn off blocking for all supported applications. Feeds will play without restriction until you re-enable them.",
            confirmText = "Allow All",
            onConfirm = {
                viewModel.allowAllApps()
                showAllowAllDialog = false
            },
            onDismiss = { showAllowAllDialog = false }
        )
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ReelGuard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Distraction Control Center",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(VioletContainer, CircleShape)
                .border(1.dp, VioletPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Shield",
                tint = VioletPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun HeroStatusCard(
    isServiceActive: Boolean,
    blockedCount: Int,
    onOpenSettings: () -> Unit
) {
    val isProtected = isServiceActive && blockedCount > 0
    val statusColor = if (isProtected) StatusProtectedGreen else if (!isServiceActive) StatusSetupAmber else StatusAllowedSlate
    val statusTitle = when {
        !isServiceActive -> "Accessibility Setup Required"
        blockedCount > 0 -> "Active Feed Protection"
        else -> "All Feeds Allowed"
    }
    val statusDesc = when {
        !isServiceActive -> "ReelGuard requires its accessibility service to detect and redirect Shorts/Reels. Tap below to enable it in Android Settings."
        blockedCount > 0 -> "Active protection is live on $blockedCount apps. Distracting short-form feeds will be redirected automatically."
        else -> "You have allowed all feeds. Tap 'Apps' to pick which feeds to block independently."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (!isServiceActive) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSetupAmber),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("enable_accessibility_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Accessibility Service",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    iconColor: Color,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusSessionDashboardCard(
    focusSession: FocusSessionData,
    remainingMs: Long,
    onNavigateToFocus: () -> Unit,
    onQuickStart: (Int) -> Unit,
    onEndSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("focus_dashboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (focusSession.isActive) VioletPrimary.copy(alpha = 0.5f) else DarkSurfaceBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = "Focus",
                        tint = VioletPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (focusSession.isActive) "Focus Mode Active" else "Timed Focus Session",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (focusSession.isActive) {
                    val minutes = (remainingMs / 1000) / 60
                    val seconds = (remainingMs / 1000) % 60
                    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    Text(
                        text = timeFormatted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (focusSession.isActive) {
                Text(
                    text = "Strict mode is locking feeds to prevent impulsive browsing.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToFocus,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Session View", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onEndSession,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("End Session", fontSize = 12.sp)
                    }
                }
            } else {
                Text(
                    text = "Lock in deep work by launching a timed focus session:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25 to "25m", 45 to "45m", 60 to "60m").forEach { (min, label) ->
                        Button(
                            onClick = { onQuickStart(min) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("quick_focus_$label")
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppQuickRow(
    app: SupportedApp,
    isServiceActive: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("app_quick_row_${app.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppBrandBadge(appName = app.name)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${app.feedType} • ${if (app.isBlocked) "Blocked" else "Allowed"}",
                        fontSize = 11.sp,
                        color = if (app.isBlocked) StatusProtectedGreen else StatusAllowedSlate
                    )
                }
            }

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { checked -> onToggle(checked) },
                modifier = Modifier.testTag("switch_${app.id}")
            )
        }
    }
}

@Composable
private fun RecentEventCard(event: BlockEventEntity) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(event.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(StatusProtectedGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = StatusProtectedGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${event.appName} ${event.feedType}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Redirected to protect attention",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = formattedTime,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun openAccessibilitySettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
