package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reelguard_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        private val KEY_THEME = stringPreferencesKey("theme_mode") // "dark", "light", "system"
        private val KEY_SHOW_BLOCK_NOTIFICATIONS = booleanPreferencesKey("show_block_notifications")

        // Focus Mode session keys
        private val KEY_FOCUS_ACTIVE = booleanPreferencesKey("focus_active")
        private val KEY_FOCUS_START_TIME = longPreferencesKey("focus_start_time")
        private val KEY_FOCUS_DURATION_MS = longPreferencesKey("focus_duration_ms")

        fun appBlockKey(appId: String) = booleanPreferencesKey("app_block_${appId.lowercase()}")
    }

    val isOnboardingSeen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_SEEN] ?: false
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_SEEN] = seen
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "dark"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = mode
        }
    }

    val showBlockNotifications: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_BLOCK_NOTIFICATIONS] ?: true
    }

    suspend fun setShowBlockNotifications(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_BLOCK_NOTIFICATIONS] = show
        }
    }

    // Observe blocking status for a specific app
    fun isAppBlocked(appId: String, defaultBlocked: Boolean = false): Flow<Boolean> {
        val key = appBlockKey(appId)
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: defaultBlocked
        }
    }

    // Update blocking status for a SINGLE app independently
    suspend fun setAppBlocked(appId: String, blocked: Boolean) {
        val key = appBlockKey(appId)
        context.dataStore.edit { prefs ->
            prefs[key] = blocked
        }
    }

    // Bulk action: Block all specified app IDs
    suspend fun blockAll(appIds: List<String>) {
        context.dataStore.edit { prefs ->
            appIds.forEach { id ->
                prefs[appBlockKey(id)] = true
            }
        }
    }

    // Bulk action: Allow all specified app IDs
    suspend fun allowAll(appIds: List<String>) {
        context.dataStore.edit { prefs ->
            appIds.forEach { id ->
                prefs[appBlockKey(id)] = false
            }
        }
    }

    // Focus Session state
    val focusSession: Flow<FocusSessionData> = context.dataStore.data.map { prefs ->
        val active = prefs[KEY_FOCUS_ACTIVE] ?: false
        val startTime = prefs[KEY_FOCUS_START_TIME] ?: 0L
        val durationMs = prefs[KEY_FOCUS_DURATION_MS] ?: 0L
        val now = System.currentTimeMillis()
        val isExpired = active && (now >= startTime + durationMs)

        FocusSessionData(
            isActive = active && !isExpired,
            startTime = startTime,
            durationMs = durationMs
        )
    }

    suspend fun startFocusSession(durationMinutes: Int) {
        val now = System.currentTimeMillis()
        val durationMs = durationMinutes * 60 * 1000L
        context.dataStore.edit { prefs ->
            prefs[KEY_FOCUS_ACTIVE] = true
            prefs[KEY_FOCUS_START_TIME] = now
            prefs[KEY_FOCUS_DURATION_MS] = durationMs
        }
    }

    suspend fun endFocusSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_FOCUS_ACTIVE] = false
            prefs[KEY_FOCUS_START_TIME] = 0L
            prefs[KEY_FOCUS_DURATION_MS] = 0L
        }
    }
}

data class FocusSessionData(
    val isActive: Boolean,
    val startTime: Long,
    val durationMs: Long
) {
    val remainingMs: Long
        get() {
            if (!isActive) return 0L
            val elapsed = System.currentTimeMillis() - startTime
            val left = durationMs - elapsed
            return if (left > 0) left else 0L
        }
}
