package com.example.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class InstagramReelsDetector : AppDetector {
    override val appId = "instagram"
    override val appName = "Instagram"
    override val targetPackages = listOf("com.instagram.android")
    override val feedType = "Reels"

    override fun detectFeed(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): DetectionResult {
        // 1. Check event class name
        val className = event.className?.toString() ?: ""
        if (className.contains("ClipsViewerActivity", ignoreCase = true) ||
            className.contains("ClipsViewerFragment", ignoreCase = true)
        ) {
            return DetectionResult(true, feedType, "Instagram Clips Viewer Activity/Fragment")
        }

        // 2. Event content description
        val contentDesc = event.contentDescription?.toString() ?: ""
        if (contentDesc.equals("Reels", ignoreCase = true) ||
            contentDesc.contains("Reel by", ignoreCase = true) ||
            contentDesc.contains("Reels tab", ignoreCase = true)
        ) {
            return DetectionResult(true, feedType, "Instagram Reels content description: $contentDesc")
        }

        // 3. Inspect window node hierarchy for Reels / Clips containers
        if (rootNode != null) {
            val clipsVideoContainer = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container")
            if (clipsVideoContainer.isNotEmpty()) {
                clipsVideoContainer.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected clips_video_container")
            }

            val clipsViewerContainer = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reels_viewer_container")
            if (clipsViewerContainer.isNotEmpty()) {
                clipsViewerContainer.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected reels_viewer_container")
            }

            val reelsTray = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_swipe_refresh_layout")
            if (reelsTray.isNotEmpty()) {
                reelsTray.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected clips_swipe_refresh_layout")
            }
        }

        return DetectionResult(false, feedType)
    }
}
