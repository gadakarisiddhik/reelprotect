package com.example.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TikTokDetector : AppDetector {
    override val appId = "tiktok"
    override val appName = "TikTok"
    override val targetPackages = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    override val feedType = "Short Videos"

    override fun detectFeed(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): DetectionResult {
        // Because TikTok is exclusively short-form vertical video, launching or viewing
        // the main feed container triggers feed detection.
        val className = event.className?.toString() ?: ""
        if (className.contains("MainActivity", ignoreCase = true) ||
            className.contains("MainFragment", ignoreCase = true) ||
            className.contains("FeedFragment", ignoreCase = true)
        ) {
            return DetectionResult(true, feedType, "TikTok main short-video feed container: $className")
        }

        val textList = event.text.map { it.toString() }
        for (text in textList) {
            if (text.contains("For You", ignoreCase = true) || text.contains("Following", ignoreCase = true)) {
                return DetectionResult(true, feedType, "TikTok feed selector: $text")
            }
        }

        return DetectionResult(true, feedType, "TikTok active video viewing session")
    }
}
