package com.example.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class YouTubeShortsDetector : AppDetector {
    override val appId = "youtube"
    override val appName = "YouTube"
    override val targetPackages = listOf("com.google.android.youtube")
    override val feedType = "Shorts"

    override fun detectFeed(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): DetectionResult {
        // 1. Check event class name
        val className = event.className?.toString() ?: ""
        if (className.contains("ReelWatchActivity", ignoreCase = true) ||
            className.contains("ShortsFragment", ignoreCase = true) ||
            className.contains("ReelWatchFragment", ignoreCase = true)
        ) {
            return DetectionResult(true, feedType, "YouTube Shorts Activity/Fragment: $className")
        }

        // 2. Check event text or content descriptions
        val textList = event.text.map { it.toString() }
        for (text in textList) {
            if (text.equals("Shorts", ignoreCase = true)) {
                return DetectionResult(true, feedType, "Shorts text detected in event")
            }
        }

        val contentDesc = event.contentDescription?.toString() ?: ""
        if (contentDesc.contains("Shorts", ignoreCase = true) &&
            (contentDesc.contains("pause", ignoreCase = true) ||
             contentDesc.contains("play", ignoreCase = true) ||
             contentDesc.contains("like this video", ignoreCase = true) ||
             contentDesc.contains("dislike this video", ignoreCase = true))
        ) {
            return DetectionResult(true, feedType, "Shorts player interaction controls detected")
        }

        // 3. Inspect window node hierarchy safely
        if (rootNode != null) {
            val shortsContainerNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/shorts_container")
            if (shortsContainerNodes.isNotEmpty()) {
                shortsContainerNodes.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected shorts_container view ID")
            }

            val reelRecyclerNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_recycler")
            if (reelRecyclerNodes.isNotEmpty()) {
                reelRecyclerNodes.forEach { it.recycle() }
                return DetectionResult(true, feedType, "Detected reel_recycler view ID")
            }

            val pivotShortsNodes = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/pivot_bar")
            if (pivotShortsNodes.isNotEmpty()) {
                // Bottom pivot bar exists; check if Shorts tab is selected
                for (pivot in pivotShortsNodes) {
                    val childCount = pivot.childCount
                    for (i in 0 until childCount) {
                        val child = pivot.getChild(i)
                        if (child != null) {
                            val desc = child.contentDescription?.toString() ?: ""
                            val isSelected = child.isSelected
                            if (isSelected && desc.contains("Shorts", ignoreCase = true)) {
                                child.recycle()
                                pivot.recycle()
                                return DetectionResult(true, feedType, "YouTube Shorts tab is actively selected")
                            }
                            child.recycle()
                        }
                    }
                    pivot.recycle()
                }
            }
        }

        return DetectionResult(false, feedType)
    }
}
