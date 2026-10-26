package com.example.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class FacebookReelsDetector : AppDetector {
    override val appId = "facebook"
    override val appName = "Facebook"
    override val targetPackages = listOf("com.facebook.katana")
    override val feedType = "Reels"

    override fun detectFeed(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): DetectionResult {
        // 1. Check event text or description
        val textList = event.text.map { it.toString() }
        for (text in textList) {
            if (text.contains("Reels and short videos", ignoreCase = true) ||
                text.equals("Reels", ignoreCase = true)
            ) {
                return DetectionResult(true, feedType, "Facebook Reels header: $text")
            }
        }

        val contentDesc = event.contentDescription?.toString() ?: ""
        if (contentDesc.contains("Reels and short videos", ignoreCase = true) ||
            contentDesc.contains("Reel video player", ignoreCase = true)
        ) {
            return DetectionResult(true, feedType, "Facebook Reels content description")
        }

        // 2. Window node inspection
        if (rootNode != null) {
            val fbReelsNodes = rootNode.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/fb_reels")
            if (fbReelsNodes.isNotEmpty()) {
                fbReelsNodes.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected fb_reels container")
            }

            val reelViewerNodes = rootNode.findAccessibilityNodeInfosByViewId("com.facebook.katana:id/reel_viewer_fragment")
            if (reelViewerNodes.isNotEmpty()) {
                reelViewerNodes.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected reel_viewer_fragment")
            }
        }

        return DetectionResult(false, feedType)
    }
}
