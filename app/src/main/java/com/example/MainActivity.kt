package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.apps.AppDetailScreen
import com.example.ui.apps.SupportedAppsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.focus.FocusScreen
import com.example.ui.navigation.Screen
import com.example.ui.onboarding.OnboardingDialog
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ReelGuardTheme
import com.example.ui.theme.VioletPrimary
import com.example.viewmodel.ReelGuardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ReelGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isOnboardingSeen by viewModel.isOnboardingSeen.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "system" -> systemDark
                else -> true // default to dark theme for wellbeing aesthetic
            }

            ReelGuardTheme(darkTheme = isDarkTheme) {
                ReelGuardApp(
                    viewModel = viewModel,
                    isOnboardingSeen = isOnboardingSeen,
                    onDismissOnboarding = { viewModel.setOnboardingSeen(true) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshServiceStatus()
    }
}

@Composable
fun ReelGuardApp(
    viewModel: ReelGuardViewModel,
    isOnboardingSeen: Boolean,
    onDismissOnboarding: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showOnboarding by remember(isOnboardingSeen) {
        mutableStateOf(!isOnboardingSeen)
    }

    val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav_bar")
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = VioletPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = VioletPrimary
                            ),
                            modifier = Modifier.testTag("nav_tab_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToApps = {
                        navController.navigate(Screen.SupportedApps.route)
                    },
                    onNavigateToFocus = {
                        navController.navigate(Screen.Focus.route)
                    },
                    onNavigateToAppDetail = { appId: String ->
                        navController.navigate(Screen.AppDetail.createRoute(appId))
                    }
                )
            }

            composable(Screen.SupportedApps.route) {
                SupportedAppsScreen(
                    viewModel = viewModel,
                    onNavigateToAppDetail = { appId ->
                        navController.navigate(Screen.AppDetail.createRoute(appId))
                    }
                )
            }

            composable(Screen.Focus.route) {
                FocusScreen(viewModel = viewModel)
            }

            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.AppDetail.route,
                arguments = listOf(navArgument("appId") { type = NavType.StringType })
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getString("appId") ?: "youtube"
                AppDetailScreen(
                    appId = appId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showOnboarding) {
        OnboardingDialog(
            onDismiss = {
                showOnboarding = false
                onDismissOnboarding()
            }
        )
    }
}
