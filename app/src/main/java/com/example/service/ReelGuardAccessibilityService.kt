package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ReelGuardApplication
import com.example.data.local.BlockEventEntity
import com.example.detector.DetectorRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReelGuardAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastInterceptTimestamp = 0L
    private val debounceCooldownMs = 1200L

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _lastInterceptedEvent = MutableStateFlow<BlockEventEntity?>(null)
        val lastInterceptedEvent: StateFlow<BlockEventEntity?> = _lastInterceptedEvent.asStateFlow()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        serviceScope.cancel()
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by system
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        val detector = DetectorRegistry.getDetectorForPackage(packageName) ?: return

        val now = SystemClock.elapsedRealtime()
        if (now - lastInterceptTimestamp < debounceCooldownMs) {
            return
        }

        // Run blocking checks asynchronously on IO dispatcher
        serviceScope.launch {
            val app = ReelGuardApplication.getInstance()
            val userPrefs = app.userPreferences

            // 1. Check if user has specifically blocked this app
            val isAppBlocked = userPrefs.isAppBlocked(detector.appId, defaultBlocked = (detector.appId == "youtube" || detector.appId == "instagram")).first()
            val focusSession = userPrefs.focusSession.first()

            // If the app is Allowed and there's no active focus session forcing lock, let the user browse freely!
            if (!isAppBlocked && !focusSession.isActive) {
                return@launch
            }

            // 2. Perform feed detection
            val rootNode = try {
                rootInActiveWindow
            } catch (_: Exception) {
                null
            }

            val detectionResult = detector.detectFeed(event, rootNode)
            if (rootNode != null) {
                try {
                    rootNode.recycle()
                } catch (_: Exception) {}
            }

            if (detectionResult.isDetected) {
                lastInterceptTimestamp = SystemClock.elapsedRealtime()

                // Execute action: Redirect back away from the Shorts/Reels feed
                val redirected = performGlobalAction(GLOBAL_ACTION_BACK)

                // Log the real event to Room database
                val blockEvent = BlockEventEntity(
                    appId = detector.appId,
                    appName = detector.appName,
                    packageName = packageName,
                    feedType = detector.feedType,
                    actionTaken = if (redirected) "REDIRECT_BACK" else "FOCUS_BLOCKED",
                    wasFocusSession = focusSession.isActive
                )

                app.database.blockEventDao().insert(blockEvent)
                _lastInterceptedEvent.value = blockEvent

                // Send non-intrusive notification if user enabled notifications
                val showNotif = userPrefs.showBlockNotifications.first()
                if (showNotif) {
                    showBlockNotification(detector.appName, detector.feedType)
                }
            }
        }
    }

    private fun showBlockNotification(appName: String, feedType: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ReelGuardApplication.CHANNEL_ID_PROTECTION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Focus Protected")
            .setContentText("Redirected you away from $appName $feedType.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}
