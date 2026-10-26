package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object SupportedApps : Screen("supported_apps", "Apps", Icons.Default.Apps)
    object Focus : Screen("focus", "Focus", Icons.Default.HourglassTop)
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AppDetail : Screen("app_detail/{appId}", "App Details") {
        fun createRoute(appId: String) = "app_detail/$appId"
    }

    companion object {
        val bottomNavItems = listOf(Dashboard, SupportedApps, Focus, Stats, Settings)
    }
}
