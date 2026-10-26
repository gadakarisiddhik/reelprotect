package com.example.detector

import android.content.Context
import android.content.pm.PackageManager
import com.example.model.DetectionCapability
import com.example.model.SupportedApp

object DetectorRegistry {

    private val allDetectors: List<AppDetector> = listOf(
        YouTubeShortsDetector(),
        InstagramReelsDetector(),
        FacebookReelsDetector(),
        TikTokDetector(),
        SnapchatSpotlightDetector()
    )

    fun getDetectorForPackage(packageName: String): AppDetector? {
        return allDetectors.firstOrNull { it.canHandle(packageName) }
    }

    fun getDetectorByAppId(appId: String): AppDetector? {
        return allDetectors.firstOrNull { it.appId.equals(appId, ignoreCase = true) }
    }

    fun getAllSupportedApps(context: Context? = null): List<SupportedApp> {
        val packageManager = context?.packageManager

        return listOf(
            SupportedApp(
                id = "youtube",
                packageName = "com.google.android.youtube",
                name = "YouTube",
                feedType = "Shorts",
                isBlocked = true, // Default enabled for Shorts blocking
                isInstalled = isPackageInstalled("com.google.android.youtube", packageManager),
                detectionCapability = DetectionCapability.FULL_FEED_REDIRECT,
                setupInstructions = "Enable ReelGuard Accessibility Service in Android Settings. No extra setup required.",
                detectionMechanism = "Inspects window events and view identifiers (reel_recycler, shorts_container, Shorts navigation pivot). When Shorts are opened, ReelGuard executes a swift system Back action to return you to standard long-form videos.",
                knownLimitations = "Standard videos, subscriptions, search, and channel browsing remain 100% accessible. Shorts carousels in Home require being tapped before intercepting."
            ),
            SupportedApp(
                id = "instagram",
                packageName = "com.instagram.android",
                name = "Instagram",
                feedType = "Reels",
                isBlocked = true, // Default enabled for Reels blocking
                isInstalled = isPackageInstalled("com.instagram.android", packageManager),
                detectionCapability = DetectionCapability.FULL_FEED_REDIRECT,
                setupInstructions = "Enable ReelGuard Accessibility Service. Works automatically with all modern Instagram versions.",
                detectionMechanism = "Detects the Reels tab and ClipsViewerActivity/Fragment containers. Intercepts the infinite scroll reel and navigates back to your main feed.",
                knownLimitations = "Direct messages, stories, profile viewing, and photo feed remain intact. Reels received as direct messages are not blocked so you can communicate freely."
            ),
            SupportedApp(
                id = "facebook",
                packageName = "com.facebook.katana",
                name = "Facebook",
                feedType = "Reels",
                isBlocked = false,
                isInstalled = isPackageInstalled("com.facebook.katana", packageManager),
                detectionCapability = DetectionCapability.TAB_REDIRECT,
                setupInstructions = "Enable ReelGuard Accessibility Service.",
                detectionMechanism = "Identifies the Facebook Reels section and full-screen reel viewer player, redirecting back to the standard News Feed.",
                knownLimitations = "Newsfeed articles, groups, marketplace, and regular feed posts remain accessible."
            ),
            SupportedApp(
                id = "tiktok",
                packageName = "com.zhiliaoapp.musically",
                name = "TikTok",
                feedType = "Short Videos",
                isBlocked = false,
                isInstalled = isPackageInstalled("com.zhiliaoapp.musically", packageManager) ||
                              isPackageInstalled("com.ss.android.ugc.trill", packageManager),
                detectionCapability = DetectionCapability.LAUNCH_INTERCEPT,
                setupInstructions = "Enable ReelGuard Accessibility Service.",
                detectionMechanism = "TikTok is fundamentally built around infinite vertical feeds. ReelGuard detects the video player container and redirects or offers focus pause.",
                knownLimitations = "Unlike YouTube and Instagram where regular long-form content is spared, blocking TikTok affects the primary feed."
            ),
            SupportedApp(
                id = "snapchat",
                packageName = "com.snapchat.android",
                name = "Snapchat",
                feedType = "Spotlight",
                isBlocked = false,
                isInstalled = isPackageInstalled("com.snapchat.android", packageManager),
                detectionCapability = DetectionCapability.TAB_REDIRECT,
                setupInstructions = "Enable ReelGuard Accessibility Service.",
                detectionMechanism = "Monitors the Spotlight tab pivot selector in Snapchat and redirects to camera/chats.",
                knownLimitations = "Friend chats, camera snaps, and personal stories are unaffected."
            )
        )
    }

    private fun isPackageInstalled(packageName: String, pm: PackageManager?): Boolean {
        if (pm == null) return true // default assumed true if context unavailable
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}
