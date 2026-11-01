package com.nh.uiuxexplorer.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private data class NetworkNode(
    val baseAngle: Float,
    val radiusFraction: Float,
    val speed: Float,
    val sizeDp: Float,
    val color: Color
)

/**
 * Full-screen Anti-Tamper & Screenshot-Bypass Hard Lock Screen:
 * If anyone modifies the APK, changes the signature, or removes/hooks FLAG_SECURE (screenshot/screen-recording block),
 * the application immediately halts here and refuses to function.
 */
@Composable
fun SecurityTamperBlockScreen(
    reason: String,
    onExitApp: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "tamperPulse")
    val pulse by inf.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tamperPulseScale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF09040A), Color(0xFF1F0812), Color(0xFF09040A))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF1A0B12).copy(alpha = 0.96f))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF1744), Color(0xFFFF9100))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(74.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .clip(CircleShape)
                    .background(Color(0xFFFF1744).copy(alpha = 0.20f))
                    .border(2.dp, Color(0xFFFF1744), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = "Security Violation",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(36.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFF1744).copy(alpha = 0.18f),
                modifier = Modifier.border(0.8.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(50))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFFFF5252), modifier = Modifier.size(13.dp))
                    Text(
                        "SECURITY VIOLATION · APP DISABLED",
                        color = Color(0xFFFF8A80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Text(
                text = "App Modified or Screenshot Protection Removed",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "UI/UX Name Explorer is protected against screenshots, screen recording, and APK modification.\n\n" +
                    (reason.ifBlank { "Unauthorized modification or FLAG_SECURE bypass detected." }) +
                    "\n\nPlease install the official unmodified application to continue.",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onExitApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("Close Application", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Full-screen Online-Only Enforcement Gate:
 * Blocks the application from opening or being used when the device is offline,
 * ensuring the Realtime Database user count and server watermark configuration are always active.
 */
@Composable
fun OfflineBlockScreen(
    onRetryConnection: () -> Boolean
) {
    val context = LocalContext.current
    val inf = rememberInfiniteTransition(label = "offlinePulse")
    val pulse by inf.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    val ringWave by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "ringWave"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070913), Color(0xFF121629), Color(0xFF070913))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF15192E).copy(alpha = 0.94f))
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF4D9D).copy(alpha = 0.7f), Color(0xFF4DD8E6).copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxR = size.minDimension / 2f
                    drawCircle(
                        color = Color(0xFFFF4D9D).copy(alpha = (1f - ringWave) * 0.45f),
                        radius = maxR * (0.45f + ringWave * 0.55f),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                Box(
                    Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF4D9D).copy(alpha = 0.25f), Color(0xFF7A5CFF).copy(alpha = 0.25f))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFF4D9D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = "No Internet",
                        tint = Color(0xFFFF8FB8),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFF4D9D).copy(alpha = 0.16f),
                modifier = Modifier.border(0.8.dp, Color(0xFFFF4D9D).copy(alpha = 0.45f), RoundedCornerShape(50))
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.CloudOff, null, tint = Color(0xFFFF8FB8), modifier = Modifier.size(13.dp))
                    Text(
                        "OFFLINE MODE DISABLED",
                        color = Color(0xFFFF8FB8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Text(
                text = "Internet Connection Required",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "UI/UX Name Explorer requires an active internet connection to sync live UI styles, user count, and cloud watermark settings from the server.\n\nPlease turn on Mobile Data or Wi-Fi to continue.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    val ok = onRetryConnection()
                    if (!ok) {
                        Toast.makeText(context, "Still offline — please turn on Wi-Fi or Mobile Data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retry Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    runCatching {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                        } else {
                            Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Settings, null, modifier = Modifier.size(15.dp), tint = Color(0xFF9CF0FA))
                Spacer(Modifier.width(6.dp))
                Text("Turn On Wi-Fi / Mobile Data", fontSize = 12.sp, color = Color(0xFF9CF0FA))
            }
        }
    }
}

/**
 * Two-stage next-level animated loading experience:
 * Stage 0 -> Loading Screen 1: Live Realtime Database User Count with Cybernetic Radar & Constellation Animation (Device ID is never shown).
 * Stage 1 -> Loading Screen 2: UI/UX Design Engine Next-Level Holographic Assembly Animation.
 */
@Composable
fun LoadingSequenceScreen(
    totalUsers: Int,
    activeToday: Int,
    syncStatus: String,
    onFinished: () -> Unit
) {
    var stage by remember { mutableIntStateOf(0) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(stage) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
        if (stage == 0) {
            stage = 1
        } else {
            delay(120)
            onFinished()
        }
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF050711), Color(0xFF0B1024), Color(0xFF070914))
                )
            )
            .padding(top = topInset)
            .navigationBarsPadding()
    ) {
        // Top stage indicator pills + Skip button
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                StagePill(
                    index = 1,
                    title = "Live Users",
                    active = stage == 0,
                    completed = stage > 0,
                    onClick = { stage = 0 }
                )
                StagePill(
                    index = 2,
                    title = "UI/UX Studio",
                    active = stage == 1,
                    completed = false,
                    onClick = { stage = 1 }
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.10f),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (stage == 0) stage = 1 else onFinished()
                    }
            ) {
                Text(
                    text = if (stage == 0) "Next >" else "Enter App >",
                    color = Color(0xFF9CF0FA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Main Animated Stage Content
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                (fadeIn(tween(360)) + scaleIn(tween(360), initialScale = 0.92f)) togetherWith
                    fadeOut(tween(260))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp, bottom = 54.dp),
            label = "loadingStageTransition"
        ) { currentStage ->
            if (currentStage == 0) {
                UserCountNextLevelLoadingPage(
                    totalUsers = totalUsers,
                    activeToday = activeToday,
                    syncStatus = syncStatus
                )
            } else {
                UiUxDesignNextLevelLoadingPage()
            }
        }

        // Bottom sleek progress bar
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (stage == 0) "Syncing Realtime User Network..." else "Initializing UI/UX Design Engine...",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                val totalPct = (((stage * 1f + progressAnim.value) / 2f) * 100f).roundToInt().coerceIn(0, 100)
                Text(
                    text = "$totalPct%",
                    color = Color(0xFF4DD8E6),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                val fillFrac = ((stage * 1f + progressAnim.value) / 2f).coerceIn(0.02f, 1f)
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillFrac)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00FFA3), Color(0xFF00C2D1), Color(0xFF7A5CFF))
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun StagePill(
    index: Int,
    title: String,
    active: Boolean,
    completed: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        active -> Color(0xFF00C2D1).copy(alpha = 0.24f)
        completed -> Color(0xFF00FFA3).copy(alpha = 0.18f)
        else -> Color.White.copy(alpha = 0.06f)
    }
    val borderCol = when {
        active -> Color(0xFF4DD8E6)
        completed -> Color(0xFF00FFA3).copy(alpha = 0.6f)
        else -> Color.White.copy(alpha = 0.15f)
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(0.8.dp, borderCol, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (active) Color(0xFF4DD8E6) else if (completed) Color(0xFF00FFA3) else Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = Color(0xFF050711),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = title,
            color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ============================================================================
// LOADING SCREEN 1: LIVE USER COUNT WITH NEXT-LEVEL ANIMATION
// (Shows animated global community counter from Realtime DB; never shows Device ID)
// ============================================================================

@Composable
private fun UserCountNextLevelLoadingPage(
    totalUsers: Int,
    activeToday: Int,
    syncStatus: String
) {
    val inf = rememberInfiniteTransition(label = "userCountRadar")
    val radarAngle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "radarAngle"
    )
    val pulseWave by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulseWave"
    )
    val glowPulse by inf.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    val orbitPhase by inf.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "orbitPhase"
    )

    val animatedCount = remember { Animatable(0f) }
    LaunchedEffect(totalUsers) {
        animatedCount.animateTo(
            targetValue = totalUsers.coerceAtLeast(1).toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val nodes = remember {
        val palette = listOf(Color(0xFF00FFA3), Color(0xFF4DD8E6), Color(0xFF7A5CFF), Color(0xFFFF4D9D))
        List(26) { i ->
            NetworkNode(
                baseAngle = (i * 137.5f) % 360f,
                radiusFraction = 0.28f + (i % 7) * 0.095f,
                speed = if (i % 2 == 0) 1f else -0.75f,
                sizeDp = 3.2f + (i % 4) * 1.4f,
                color = palette[i % palette.size]
            )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Live Realtime Database Badge
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF00FFA3).copy(alpha = 0.12f),
            modifier = Modifier.border(0.8.dp, Color(0xFF00FFA3).copy(alpha = 0.45f), RoundedCornerShape(50))
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .graphicsLayer { scaleX = glowPulse; scaleY = glowPulse }
                        .clip(CircleShape)
                        .background(Color(0xFF00FFA3))
                )
                Icon(Icons.Default.CloudDone, null, tint = Color(0xFF00FFA3), modifier = Modifier.size(13.dp))
                Text(
                    text = syncStatus.uppercase(),
                    color = Color(0xFF00FFA3),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Central Next-Level Radar + Constellation + Live Counter Orb
        Box(
            Modifier.size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient background glow
            Box(
                Modifier
                    .size(190.dp)
                    .blur(42.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00C2D1).copy(alpha = 0.42f), Color(0xFF5B2EFF).copy(alpha = 0.22f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxR = size.minDimension / 2f

                // Concentric cyber rings
                listOf(0.38f, 0.62f, 0.84f, 0.98f).forEachIndexed { idx, frac ->
                    drawCircle(
                        color = Color(0xFF4DD8E6).copy(alpha = 0.14f - idx * 0.02f),
                        radius = maxR * frac,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Expanding sonar shockwave rings
                for (waveIdx in 0..1) {
                    val phase = (pulseWave + waveIdx * 0.5f) % 1f
                    val waveR = maxR * (0.28f + phase * 0.72f)
                    val waveAlpha = (1f - phase) * 0.45f
                    drawCircle(
                        color = Color(0xFF00FFA3).copy(alpha = waveAlpha),
                        radius = waveR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Rotating radar sweep sector
                rotate(radarAngle, pivot = Offset(cx, cy)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.75f to Color.Transparent,
                            0.95f to Color(0xFF00C2D1).copy(alpha = 0.22f),
                            1f to Color(0xFF00FFA3).copy(alpha = 0.55f),
                            center = Offset(cx, cy)
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = true,
                        topLeft = Offset(cx - maxR * 0.92f, cy - maxR * 0.92f),
                        size = Size(maxR * 1.84f, maxR * 1.84f)
                    )
                }

                // Compute orbital node positions and draw constellation links
                val positions = nodes.map { n ->
                    val rad = Math.toRadians(n.baseAngle.toDouble()) + orbitPhase * n.speed
                    val r = maxR * n.radiusFraction
                    Offset(
                        x = cx + (cos(rad) * r).toFloat(),
                        y = cy + (sin(rad) * r).toFloat()
                    )
                }

                for (i in positions.indices) {
                    for (j in i + 1 until positions.size) {
                        val p1 = positions[i]
                        val p2 = positions[j]
                        val dist = hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble()).toFloat()
                        val threshold = maxR * 0.36f
                        if (dist < threshold) {
                            val lineAlpha = ((1f - dist / threshold) * 0.28f).coerceIn(0f, 0.28f)
                            drawLine(
                                color = Color(0xFF9CF0FA).copy(alpha = lineAlpha),
                                start = p1,
                                end = p2,
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                // Draw glowing user nodes
                positions.forEachIndexed { idx, pt ->
                    val node = nodes[idx]
                    drawCircle(
                        color = node.color.copy(alpha = 0.28f),
                        radius = (node.sizeDp * 2.2f).dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = node.color,
                        radius = node.sizeDp.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Center Glassmorphic User Counter Core
            Column(
                modifier = Modifier
                    .size(138.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF141E38).copy(alpha = 0.92f), Color(0xFF0A0F20).copy(alpha = 0.96f))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF00FFA3), Color(0xFF4DD8E6), Color(0xFF7A5CFF))
                        ),
                        shape = CircleShape
                    )
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = Color(0xFF4DD8E6),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "%,d".format(animatedCount.value.roundToInt()),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "TOTAL USERS",
                    color = Color(0xFF9CF0FA),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Global UI/UX Designer Community",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Connected to Realtime Database · Live user count synced automatically",
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        // Compact Metric Cards Row (Strictly NO Device ID shown)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricPillCard(
                icon = Icons.Default.Public,
                label = "Registered Devices",
                value = "%,d Users".format(totalUsers.coerceAtLeast(1)),
                accent = Color(0xFF4DD8E6),
                modifier = Modifier.weight(1f)
            )
            MetricPillCard(
                icon = Icons.Default.AutoAwesome,
                label = "Active Designers",
                value = "%,d Online".format(activeToday.coerceAtLeast(1)),
                accent = Color(0xFF00FFA3),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricPillCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(0.8.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
        }
    }
}

// ============================================================================
// LOADING SCREEN 2: UI/UX DESIGN NEXT-LEVEL ANIMATION
// (Holographic phone wireframe assembling UI layers, palette orbs, and styles)
// ============================================================================

@Composable
private fun UiUxDesignNextLevelLoadingPage() {
    val inf = rememberInfiniteTransition(label = "uiuxDesignAnim")
    val floatY by inf.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY"
    )
    val orbitDeg by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "orbitDeg"
    )
    val shimmerPhase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "shimmerPhase"
    )

    val assembleProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        assembleProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val styleNames = remember {
        listOf("Glassmorphism", "Liquid Glass", "Neumorphism", "Cyberpunk UI", "Bento Grid", "Material 3")
    }
    var styleIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(420)
            styleIdx = (styleIdx + 1) % styleNames.size
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cycling Style Pill
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF7A5CFF).copy(alpha = 0.18f),
            modifier = Modifier.border(0.8.dp, Color(0xFFB6A8FF).copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Palette, null, tint = Color(0xFFB6A8FF), modifier = Modifier.size(13.dp))
                Text(
                    text = "STYLE ENGINE · ${styleNames[styleIdx].uppercase()}",
                    color = Color(0xFFE6E0FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Central Next-Level Holographic Phone & Orbiting UI Layers
        Box(
            Modifier.size(256.dp),
            contentAlignment = Alignment.Center
        ) {
            // Aurora plasma blobs behind phone
            Box(
                Modifier
                    .size(160.dp)
                    .offset(x = (-24).dp, y = (-18).dp)
                    .blur(38.dp)
                    .background(Color(0xFF5B2EFF).copy(alpha = 0.45f), CircleShape)
            )
            Box(
                Modifier
                    .size(150.dp)
                    .offset(x = 28.dp, y = 22.dp)
                    .blur(36.dp)
                    .background(Color(0xFF00C2D1).copy(alpha = 0.42f), CircleShape)
            )

            // Orbiting Color Palette Swatch Orbs & Vector Guides
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val rx = size.width * 0.46f
                val ry = size.height * 0.38f

                // Dashed orbital track
                drawOval(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = Offset(cx - rx, cy - ry),
                    size = Size(rx * 2f, ry * 2f),
                    style = Stroke(width = 1.dp.toPx())
                )

                val swatchColors = listOf(
                    Color(0xFF00FFA3),
                    Color(0xFF00C2D1),
                    Color(0xFF7A5CFF),
                    Color(0xFFFF4D9D),
                    Color(0xFFFFD166)
                )
                swatchColors.forEachIndexed { idx, col ->
                    val deg = orbitDeg + idx * (360f / swatchColors.size)
                    val rad = Math.toRadians(deg.toDouble())
                    val ox = cx + (cos(rad) * rx).toFloat()
                    val oy = cy + (sin(rad) * ry).toFloat()
                    drawCircle(col.copy(alpha = 0.32f), radius = 11.dp.toPx(), center = Offset(ox, oy))
                    drawCircle(col, radius = 6.dp.toPx(), center = Offset(ox, oy))
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(ox, oy), style = Stroke(width = 1.2.dp.toPx()))
                }
            }

            // Self-Assembling Holographic Phone Canvas
            val p = assembleProgress.value.coerceIn(0f, 1f)
            Box(
                Modifier
                    .width(126.dp)
                    .height(232.dp)
                    .graphicsLayer {
                        translationY = floatY.dp.toPx()
                        rotationZ = -2.5f * (1f - p * 0.5f)
                    }
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1A1F3C).copy(alpha = 0.92f), Color(0xFF0E1226).copy(alpha = 0.96f))
                        )
                    )
                    .border(
                        width = 1.6.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF4DD8E6), Color(0xFF7A5CFF), Color(0xFFFF4D9D))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(8.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Animated Top Bar
                    val topBarY = (1f - p) * (-30f)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.12f * p),
                        topLeft = Offset(0f, topBarY),
                        size = Size(w, 20.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF4DD8E6).copy(alpha = p),
                        radius = 3.5.dp.toPx(),
                        center = Offset(10.dp.toPx(), topBarY + 10.dp.toPx())
                    )

                    // 2. Animated Hero Media Card with Shimmer
                    val heroY = 26.dp.toPx()
                    val heroH = 68.dp.toPx() * p
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF5B2EFF), Color(0xFF00C2D1), Color(0xFFFF4D9D)),
                            start = Offset(w * shimmerPhase, heroY),
                            end = Offset(w * shimmerPhase + w, heroY + heroH)
                        ),
                        topLeft = Offset(0f, heroY),
                        size = Size(w, heroH),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )

                    // 3. Animated Typography Hierarchy Bars
                    val textY1 = 102.dp.toPx()
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.85f * p),
                        topLeft = Offset(0f, textY1),
                        size = Size(w * 0.72f * p, 7.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.42f * p),
                        topLeft = Offset(0f, textY1 + 12.dp.toPx()),
                        size = Size(w * 0.92f * p, 5.dp.toPx()),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )

                    // 4. Animated 2-Column Bento Cards
                    val bentoY = 128.dp.toPx()
                    val colW = (w - 6.dp.toPx()) / 2f
                    drawRoundRect(
                        color = Color(0xFF00FFA3).copy(alpha = 0.22f * p),
                        topLeft = Offset(0f, bentoY),
                        size = Size(colW, 40.dp.toPx() * p),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF7A5CFF).copy(alpha = 0.26f * p),
                        topLeft = Offset(colW + 6.dp.toPx(), bentoY),
                        size = Size(colW, 40.dp.toPx() * p),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )

                    // 5. Animated CTA Button + FAB
                    val btnY = h - 38.dp.toPx()
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF00C2D1), Color(0xFF5B2EFF))),
                        topLeft = Offset(0f, btnY),
                        size = Size(w * 0.72f * p, 22.dp.toPx()),
                        cornerRadius = CornerRadius(11.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFFF4D9D).copy(alpha = p),
                        radius = 11.dp.toPx() * p,
                        center = Offset(w - 11.dp.toPx(), btnY + 11.dp.toPx())
                    )

                    // 6. Bottom Home Indicator Pill
                    val navPillW = w * 0.38f
                    drawLine(
                        color = Color.White.copy(alpha = 0.55f),
                        start = Offset((w - navPillW) / 2f, h - 4.dp.toPx()),
                        end = Offset((w + navPillW) / 2f, h - 4.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Interactive UI/UX Design Studio",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Pinch-to-zoom canvas · Gallery photo upload · 4x Ultra-HD PNG export",
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricPillCard(
                icon = Icons.Default.Layers,
                label = "Interactive Canvas",
                value = "13 UI Blocks",
                accent = Color(0xFFB6A8FF),
                modifier = Modifier.weight(1f)
            )
            MetricPillCard(
                icon = Icons.Default.Palette,
                label = "Visual Themes",
                value = "18+ Live Styles",
                accent = Color(0xFFFF8FB8),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
