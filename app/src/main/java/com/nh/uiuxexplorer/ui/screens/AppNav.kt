package com.nh.uiuxexplorer.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.nh.uiuxexplorer.domain.model.DesignerMode
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import kotlinx.coroutines.delay
import com.nh.uiuxexplorer.ui.designer.DesignerScreen
import com.nh.uiuxexplorer.ui.designer.PreviewEditScreen
import com.nh.uiuxexplorer.ui.designer.SavedScreensScreen
import com.nh.uiuxexplorer.ui.vm.AppViewModel

private object Route {
    const val HOME = "home"
    const val LIBRARY = "library?cat={cat}&focus={focus}"
    const val EXPLORER = "explorer"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{id}"
    const val AI = "ai_settings"
    const val ADMIN = "admin_panel"
    const val SCREENS = "screens"
    const val DESIGNER = "designer?id={id}&style={style}&mode={mode}"
    const val PREVIEW_EDIT = "preview_edit?id={id}&style={style}&mode={mode}&session={session}"

    fun library(cat: Int = -1, focus: Boolean = false) = "library?cat=$cat&focus=${if (focus) 1 else 0}"
    fun detail(id: String) = "detail/$id"
    fun designer(id: Long = -1L, style: String = "", mode: String = "edit") =
        "designer?id=$id&style=$style&mode=$mode"
    fun previewEdit(id: Long = -1L, style: String = "", mode: String = "preview", session: Boolean = false) =
        "preview_edit?id=$id&style=$style&mode=$mode&session=${if (session) 1 else 0}"
}

private class TopItem(val pattern: String, val target: String, val label: String, val icon: ImageVector)

private val topItems = listOf(
    TopItem(Route.HOME, Route.HOME, "Home", Icons.Default.Home),
    TopItem(Route.LIBRARY, Route.library(), "Library", Icons.Default.Search),
    TopItem(Route.EXPLORER, Route.EXPLORER, "Explorer", Icons.Default.Apps),
    TopItem(Route.FAVORITES, Route.FAVORITES, "Saved", Icons.Default.Favorite),
    TopItem(Route.SETTINGS, Route.SETTINGS, "Settings", Icons.Default.Settings)
)

@Composable
fun AppNav(appVm: AppViewModel = hiltViewModel()) {
    val rtState by appVm.realtimeState.collectAsStateWithLifecycle()
    val isTampered by WatermarkGuard.isTampered.collectAsStateWithLifecycle()
    val tamperReason by WatermarkGuard.tamperReason.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showStartupLoading by rememberSaveable { mutableStateOf(true) }

    // Continuous anti-screenshot (FLAG_SECURE) & anti-modification heartbeat check
    LaunchedEffect(Unit) {
        while (true) {
            (context as? Activity)?.let { act ->
                WatermarkGuard.enforceAndVerifySecureWindow(act)
            }
            delay(1200)
        }
    }

    // 0. Strict Anti-Tamper & Screenshot-Bypass Hard Lock:
    // If the APK was modified or FLAG_SECURE was removed/hooked, the app stops working immediately!
    if (isTampered) {
        SecurityTamperBlockScreen(
            reason = tamperReason,
            onExitApp = { (context as? Activity)?.finishAffinity() }
        )
        return
    }

    // 1. Strict Online-Only Gate: App will not open or remain open when offline!
    if (!rtState.isOnline) {
        OfflineBlockScreen(
            onRetryConnection = appVm::recheckInternetAndSync
        )
        return
    }

    // 2. Two-Stage Next-Level Animated Loading Sequence
    if (showStartupLoading) {
        LoadingSequenceScreen(
            totalUsers = rtState.totalUserCount,
            activeToday = rtState.activeTodayCount,
            syncStatus = rtState.syncStatus,
            onFinished = { showStartupLoading = false }
        )
        return
    }

    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val dest = entry?.destination
    val current = topItems.firstOrNull { item -> dest?.hierarchy?.any { it.route == item.pattern } == true }
    val cs = MaterialTheme.colorScheme

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (current != null) {
                Surface(
                    color = cs.surface.copy(alpha = 0.96f),
                    tonalElevation = 4.dp
                ) {
                    Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
                        HorizontalDivider(thickness = 0.5.dp, color = cs.onSurface.copy(alpha = 0.08f))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            topItems.forEach { item ->
                                val selected = item === current
                                val pillBg by animateColorAsState(
                                    targetValue = if (selected) cs.primaryContainer.copy(alpha = 0.75f) else Color.Transparent,
                                    animationSpec = tween(180),
                                    label = "navPillBg"
                                )
                                val iconTint by animateColorAsState(
                                    targetValue = if (selected) cs.primary else cs.onSurfaceVariant,
                                    animationSpec = tween(180),
                                    label = "navIconTint"
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            nav.navigate(item.target) {
                                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        .padding(vertical = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(pillBg)
                                            .padding(horizontal = 12.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            item.icon,
                                            contentDescription = item.label,
                                            tint = iconTint,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = iconTint,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { inner ->
        val base = Modifier.padding(inner)
        NavHost(
            navController = nav,
            startDestination = Route.HOME,
            modifier = if (current == null) base.navigationBarsPadding() else base,
            enterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(220)) { it / 12 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(180)) },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(200)) { it / 12 } }
        ) {
            composable(Route.HOME) {
                HomeScreen(
                    onOpenDesign = { nav.navigate(Route.detail(it)) },
                    onOpenLibrary = { cat, focus -> nav.navigate(Route.library(cat, focus)) },
                    onOpenScreens = { nav.navigate(Route.SCREENS) },
                    onOpenAdmin = { nav.navigate(Route.ADMIN) }
                )
            }
            composable(
                Route.LIBRARY,
                arguments = listOf(
                    navArgument("cat") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("focus") { type = NavType.IntType; defaultValue = 0 }
                )
            ) { LibraryScreen(onOpen = { nav.navigate(Route.detail(it)) }) }
            composable(Route.EXPLORER) { ComponentsScreen() }
            composable(Route.FAVORITES) { FavoritesScreen(onOpen = { nav.navigate(Route.detail(it)) }) }
            composable(Route.SETTINGS) {
                SettingsScreen(
                    onOpenAi = { nav.navigate(Route.AI) },
                    onOpenAdmin = { nav.navigate(Route.ADMIN) },
                    onReplayLoading = { showStartupLoading = true }
                )
            }
            composable(Route.ADMIN) {
                if (rtState.isAdminDevice) {
                    AdminScreen(
                        onBack = { nav.popBackStack() },
                        onReplayLoadingScreens = { showStartupLoading = true }
                    )
                } else {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        nav.popBackStack()
                    }
                }
            }
            composable(
                Route.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                DetailScreen(
                    onBack = { nav.popBackStack() },
                    onOpenDesign = { nav.navigate(Route.detail(it)) },
                    onOpenAiSettings = { nav.navigate(Route.AI) },
                    onDesignScreen = { nav.navigate(Route.previewEdit(style = it, mode = "edit")) },
                    onPreviewScreen = { nav.navigate(Route.previewEdit(style = it, mode = "preview")) }
                )
            }
            composable(Route.SCREENS) {
                SavedScreensScreen(
                    onBack = { nav.popBackStack() },
                    onNew = { nav.navigate(Route.previewEdit(mode = "edit")) },
                    onOpen = { nav.navigate(Route.designer(id = it, mode = "edit")) },
                    onOpenPreviewPage = { id, mode ->
                        nav.navigate(Route.previewEdit(id = id, mode = if (mode == DesignerMode.PREVIEW) "preview" else "edit"))
                    }
                )
            }
            composable(
                Route.DESIGNER,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("style") { type = NavType.StringType; defaultValue = "" },
                    navArgument("mode") { type = NavType.StringType; defaultValue = "edit" }
                )
            ) {
                DesignerScreen(
                    onBack = { nav.popBackStack() },
                    onOpenAiSettings = { nav.navigate(Route.AI) },
                    onOpenPreviewEditPage = { mode ->
                        nav.navigate(
                            Route.previewEdit(
                                mode = if (mode == DesignerMode.PREVIEW) "preview" else "edit",
                                session = true
                            )
                        )
                    }
                )
            }
            composable(
                Route.PREVIEW_EDIT,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("style") { type = NavType.StringType; defaultValue = "" },
                    navArgument("mode") { type = NavType.StringType; defaultValue = "preview" },
                    navArgument("session") { type = NavType.IntType; defaultValue = 0 }
                )
            ) {
                PreviewEditScreen(
                    onBack = { nav.popBackStack() },
                    onOpenAiSettings = { nav.navigate(Route.AI) }
                )
            }
            composable(Route.AI) { AiSettingsScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
