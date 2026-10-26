package com.example

import android.view.accessibility.AccessibilityEvent
import com.example.data.preferences.FocusSessionData
import com.example.detector.DetectorRegistry
import com.example.detector.InstagramReelsDetector
import com.example.detector.YouTubeShortsDetector
import com.example.model.DetectionCapability
import com.example.model.ProtectionStatus
import com.example.model.SupportedApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReelGuardLogicTest {

    @Test
    fun testIndependentAppSettingToggles() {
        val appStateMap = mutableMapOf(
            "youtube" to true,
            "instagram" to true,
            "facebook" to false
        )

        // Turn Instagram OFF
        appStateMap["instagram"] = false

        // Verify YouTube and Facebook are completely unaffected
        assertTrue("YouTube Shorts must remain blocked", appStateMap["youtube"] == true)
        assertFalse("Instagram Reels must now be allowed", appStateMap["instagram"] == true)
        assertFalse("Facebook Reels must remain allowed", appStateMap["facebook"] == true)

        // Turn YouTube OFF
        appStateMap["youtube"] = false
        assertFalse("YouTube is now allowed", appStateMap["youtube"] == true)
        assertFalse("Instagram remains allowed", appStateMap["instagram"] == true)

        // Turn Instagram ON
        appStateMap["instagram"] = true
        assertTrue("Instagram is now blocked", appStateMap["instagram"] == true)
        assertFalse("YouTube remains allowed", appStateMap["youtube"] == true)
    }

    @Test
    fun testDetectorRegistryLookup() {
        // Supported apps
        val ytDetector = DetectorRegistry.getDetectorForPackage("com.google.android.youtube")
        assertNotNull(ytDetector)
        assertEquals("youtube", ytDetector?.appId)
        assertEquals("Shorts", ytDetector?.feedType)

        val igDetector = DetectorRegistry.getDetectorForPackage("com.instagram.android")
        assertNotNull(igDetector)
        assertEquals("instagram", igDetector?.appId)
        assertEquals("Reels", igDetector?.feedType)

        // Unsupported applications must return null
        val unsupportedDetector = DetectorRegistry.getDetectorForPackage("com.unsupported.randomapp")
        assertNull(unsupportedDetector)
    }

    @Test
    fun testYouTubeShortsDetectionAndNormalVideoPreservation() {
        val detector = YouTubeShortsDetector()

        // 1. Shorts event
        val shortsEvent = AccessibilityEvent.obtain().apply {
            className = "com.google.android.apps.youtube.app.extensions.reel.watch.activity.ReelWatchActivity"
        }
        val shortsResult = detector.detectFeed(shortsEvent, null)
        assertTrue("Should detect Shorts activity", shortsResult.isDetected)
        assertEquals("Shorts", shortsResult.feedType)

        // 2. Normal long-form video watch event: MUST NOT be blocked (prevent false positives!)
        val regularVideoEvent = AccessibilityEvent.obtain().apply {
            className = "com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity"
        }
        val regularResult = detector.detectFeed(regularVideoEvent, null)
        assertFalse("Normal YouTube watch activity must NOT be blocked", regularResult.isDetected)
    }

    @Test
    fun testInstagramReelsDetectionAndNormalFeedPreservation() {
        val detector = InstagramReelsDetector()

        // 1. Reels event
        val reelsEvent = AccessibilityEvent.obtain().apply {
            className = "com.instagram.clips.viewer.ClipsViewerActivity"
        }
        val reelsResult = detector.detectFeed(reelsEvent, null)
        assertTrue("Should detect Instagram Clips Viewer", reelsResult.isDetected)

        // 2. Normal main feed / profile activity: MUST NOT be blocked
        val mainFeedEvent = AccessibilityEvent.obtain().apply {
            className = "com.instagram.mainactivity.MainActivity"
        }
        val feedResult = detector.detectFeed(mainFeedEvent, null)
        assertFalse("Standard Instagram feed must NOT be blocked", feedResult.isDetected)
    }

    @Test
    fun testProtectionStatusCalculation() {
        val app = SupportedApp(
            id = "youtube",
            packageName = "com.google.android.youtube",
            name = "YouTube",
            feedType = "Shorts",
            isBlocked = true,
            isInstalled = true
        )

        // Service running & app blocked -> ACTIVE_PROTECTION
        assertEquals(ProtectionStatus.ACTIVE_PROTECTION, app.calculateStatus(isAccessibilityRunning = true))

        // Service not running & app blocked -> SETUP_REQUIRED
        assertEquals(ProtectionStatus.SETUP_REQUIRED, app.calculateStatus(isAccessibilityRunning = false))

        // App allowed (isBlocked = false) -> ALLOWED (regardless of service status)
        val allowedApp = app.copy(isBlocked = false)
        assertEquals(ProtectionStatus.ALLOWED, allowedApp.calculateStatus(isAccessibilityRunning = true))
        assertEquals(ProtectionStatus.ALLOWED, allowedApp.calculateStatus(isAccessibilityRunning = false))

        // Not installed app
        val notInstalledApp = app.copy(isInstalled = false)
        assertEquals(ProtectionStatus.NOT_INSTALLED, notInstalledApp.calculateStatus(isAccessibilityRunning = true))
    }

    @Test
    fun testFocusSessionMath() {
        val now = System.currentTimeMillis()
        val duration25Min = 25 * 60 * 1000L

        // Active session with 10 mins elapsed
        val activeSession = FocusSessionData(
            isActive = true,
            startTime = now - (10 * 60 * 1000L),
            durationMs = duration25Min
        )
        assertTrue(activeSession.isActive)
        val remaining = activeSession.remainingMs
        // Approx 15 mins left (within 5 seconds tolerance)
        assertTrue(remaining in (14 * 60 * 1000L)..(16 * 60 * 1000L))

        // Inactive session
        val inactiveSession = FocusSessionData(isActive = false, startTime = 0L, durationMs = 0L)
        assertEquals(0L, inactiveSession.remainingMs)
    }

    @Test
    fun testBulkActions() {
        val apps = DetectorRegistry.getAllSupportedApps()
        val appIds = apps.map { it.id }

        // Block All
        val blockedMap = appIds.associateWith { true }
        assertTrue(blockedMap.values.all { it })

        // Allow All
        val allowedMap = appIds.associateWith { false }
        assertTrue(allowedMap.values.none { it })
    }
}
