package com.example.detector

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

data class DetectionResult(
    val isDetected: Boolean,
    val feedType: String,
    val reason: String = ""
)

interface AppDetector {
    val appId: String
    val appName: String
    val targetPackages: List<String>
    val feedType: String

    fun canHandle(packageName: String): Boolean {
        return targetPackages.any { it.equals(packageName, ignoreCase = true) }
    }

    fun detectFeed(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?): DetectionResult
}
