package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository

class ReelGuardApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var userPreferences: UserPreferencesRepository
        private set

    companion object {
        const val CHANNEL_ID_PROTECTION = "reelguard_protection_channel"
        const val CHANNEL_ID_FOCUS = "reelguard_focus_channel"

        private var instance: ReelGuardApplication? = null

        fun getInstance(): ReelGuardApplication {
            return instance ?: throw IllegalStateException("ReelGuardApplication not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        userPreferences = UserPreferencesRepository(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val protectionChannel = NotificationChannel(
                CHANNEL_ID_PROTECTION,
                "ReelGuard Protection Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a distracting short-form video feed is intercepted"
                enableVibration(true)
            }

            val focusChannel = NotificationChannel(
                CHANNEL_ID_FOCUS,
                "ReelGuard Focus Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active focus session status"
            }

            notificationManager.createNotificationChannel(protectionChannel)
            notificationManager.createNotificationChannel(focusChannel)
        }
    }
}
