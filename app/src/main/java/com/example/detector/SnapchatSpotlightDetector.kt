package com.example.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SnapchatSpotlightDetector : AppDetector {
    override val appId = "snapchat"
    override val appName = "Snapchat"
    override val targetPackages = listOf("com.snapchat.android")
    override val feedType = "Spotlight"

    override fun detectFeed(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): DetectionResult {
        val className = event.className?.toString() ?: ""
        if (className.contains("Spotlight", ignoreCase = true)) {
            return DetectionResult(true, feedType, "Snapchat Spotlight activity/container: $className")
        }

        val textList = event.text.map { it.toString() }
        for (text in textList) {
            if (text.equals("Spotlight", ignoreCase = true)) {
                return DetectionResult(true, feedType, "Snapchat Spotlight tab selected")
            }
        }

        val contentDesc = event.contentDescription?.toString() ?: ""
        if (contentDesc.contains("Spotlight", ignoreCase = true)) {
            return DetectionResult(true, feedType, "Snapchat Spotlight content description: $contentDesc")
        }

        if (rootNode != null) {
            val spotlightNodes = rootNode.findAccessibilityNodeInfosByViewId("com.snapchat.android:id/spotlight_feed")
            if (spotlightNodes.isNotEmpty()) {
                spotlightNodes.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected spotlight_feed container")
            }
        }

        return DetectionResult(false, feedType)
    }
}
