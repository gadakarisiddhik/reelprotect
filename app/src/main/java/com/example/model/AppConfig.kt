package com.example.model

enum class DetectionCapability {
    FULL_FEED_REDIRECT, // High reliability: detects feed and navigates back
    TAB_REDIRECT,       // Detects bottom tab or feed entry point
    LAUNCH_INTERCEPT,   // Dedicated short video app intercept
    LIMITED_SUPPORT     // Partial or device/version dependent
}

enum class ProtectionStatus {
    ACTIVE_PROTECTION,  // Blocked and Accessibility Service is running
    ALLOWED,            // User chose to allow this feed
    SETUP_REQUIRED,     // Blocked requested, but Accessibility Service is off
    NOT_INSTALLED,      // App is not installed on this device
    LIMITED_SUPPORT     // Feasibility constraints or version differences
}

data class SupportedApp(
    val id: String,
    val packageName: String,
    val name: String,
    val feedType: String,
    val isBlocked: Boolean,
    val isInstalled: Boolean = true,
    val detectionCapability: DetectionCapability = DetectionCapability.FULL_FEED_REDIRECT,
    val setupInstructions: String = "",
    val detectionMechanism: String = "",
    val knownLimitations: String = "",
    val lastDetectedTimestamp: Long? = null
) {
    fun calculateStatus(isAccessibilityRunning: Boolean): ProtectionStatus {
        return when {
            !isBlocked -> ProtectionStatus.ALLOWED
            !isInstalled -> ProtectionStatus.NOT_INSTALLED
            !isAccessibilityRunning -> ProtectionStatus.SETUP_REQUIRED
            detectionCapability == DetectionCapability.LIMITED_SUPPORT -> ProtectionStatus.LIMITED_SUPPORT
            else -> ProtectionStatus.ACTIVE_PROTECTION
        }
    }
}
