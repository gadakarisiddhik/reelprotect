package com.example.viewmodel

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReelGuardApplication
import com.example.data.local.AppBlockCount
import com.example.data.local.BlockEventEntity
import com.example.data.preferences.FocusSessionData
import com.example.detector.DetectorRegistry
import com.example.model.ProtectionStatus
import com.example.model.SupportedApp
import com.example.service.ReelGuardAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ReelGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ReelGuardApplication
    private val database = app.database
    private val prefs = app.userPreferences

    // System accessibility service running state
    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    // Base supported apps list
    private val baseApps = DetectorRegistry.getAllSupportedApps(application)
    private val _appBlockedMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val supportedApps: StateFlow<List<SupportedApp>> = combine(
        _appBlockedMap,
        _isServiceActive
    ) { blockedMap, serviceActive ->
        baseApps.map { app ->
            val isBlocked = blockedMap[app.id] ?: app.isBlocked
            app.copy(isBlocked = isBlocked)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        baseApps
    )

    // Focus Session
    val focusSession: StateFlow<FocusSessionData> = prefs.focusSession.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FocusSessionData(isActive = false, startTime = 0L, durationMs = 0L)
    )

    // Today's midnight timestamp
    private val todayMidnightMs: Long
        get() {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

    val todayBlockedCount: StateFlow<Int> = database.blockEventDao()
        .getEventCountSince(todayMidnightMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBlockedCount: StateFlow<Int> = database.blockEventDao()
        .getTotalEventCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentEvents: StateFlow<List<BlockEventEntity>> = database.blockEventDao()
        .getRecentEvents(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockCountsByApp: StateFlow<List<AppBlockCount>> = database.blockEventDao()
        .getBlockCountsByApp()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<String> = prefs.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "dark"
    )

    val showBlockNotifications: StateFlow<Boolean> = prefs.showBlockNotifications.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val isOnboardingSeen: StateFlow<Boolean> = prefs.isOnboardingSeen.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    init {
        loadPreferences()
        pollAccessibilityServiceStatus()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val map = mutableMapOf<String, Boolean>()
            baseApps.forEach { app ->
                val blocked = prefs.isAppBlocked(app.id, app.isBlocked).first()
                map[app.id] = blocked
            }
            _appBlockedMap.value = map
        }
    }

    fun refreshServiceStatus() {
        val am = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK) ?: emptyList()
        val isEnabledInSettings = enabledServices.any {
            it.id.contains("ReelGuardAccessibilityService", ignoreCase = true)
        }
        val isServiceRunningState = ReelGuardAccessibilityService.isServiceRunning.value
        _isServiceActive.value = isEnabledInSettings || isServiceRunningState
    }

    private fun pollAccessibilityServiceStatus() {
        viewModelScope.launch {
            while (true) {
                refreshServiceStatus()
                delay(2000)
            }
        }
    }

    // Toggle single app independently
    fun toggleAppBlocked(appId: String, blocked: Boolean) {
        viewModelScope.launch {
            prefs.setAppBlocked(appId, blocked)
            val updated = _appBlockedMap.value.toMutableMap()
            updated[appId] = blocked
            _appBlockedMap.value = updated
        }
    }

    // Bulk block all supported apps
    fun blockAllApps() {
        viewModelScope.launch {
            val appIds = baseApps.map { it.id }
            prefs.blockAll(appIds)
            val updated = _appBlockedMap.value.toMutableMap()
            appIds.forEach { updated[it] = true }
            _appBlockedMap.value = updated
        }
    }

    // Bulk allow all supported apps
    fun allowAllApps() {
        viewModelScope.launch {
            val appIds = baseApps.map { it.id }
            prefs.allowAll(appIds)
            val updated = _appBlockedMap.value.toMutableMap()
            appIds.forEach { updated[it] = false }
            _appBlockedMap.value = updated
        }
    }

    // Focus Session controls
    fun startFocusSession(minutes: Int) {
        viewModelScope.launch {
            prefs.startFocusSession(minutes)
        }
    }

    fun endFocusSession() {
        viewModelScope.launch {
            prefs.endFocusSession()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }

    fun setShowBlockNotifications(show: Boolean) {
        viewModelScope.launch {
            prefs.setShowBlockNotifications(show)
        }
    }

    fun setOnboardingSeen(seen: Boolean) {
        viewModelScope.launch {
            prefs.setOnboardingSeen(seen)
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            database.blockEventDao().clearAll()
        }
    }

    // Test simulation: verifies event recording and detection reporting without leaving the app
    fun simulateTestDetection(appId: String) {
        viewModelScope.launch {
            val appConfig = baseApps.firstOrNull { it.id == appId } ?: return@launch
            val event = BlockEventEntity(
                appId = appConfig.id,
                appName = appConfig.name,
                packageName = appConfig.packageName,
                feedType = appConfig.feedType,
                actionTaken = "SIMULATED_TEST_REDIRECT",
                wasFocusSession = focusSession.value.isActive
            )
            database.blockEventDao().insert(event)
        }
    }
}
