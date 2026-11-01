package com.nh.uiuxexplorer.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.model.AI_LANGUAGES
import com.nh.uiuxexplorer.domain.model.ThemeMode
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.vm.SettingsViewModel

private const val PRIVACY_TEXT =
    "UI/UX Name Explorer has no accounts, no analytics and no ads. Favorites, history and settings stay on this device. " +
        "The design library is bundled and works offline. If you configure an AI model, the text you send " +
        "(for example a design name) goes only to the endpoint you entered, only when you tap a button. " +
        "Your API key is encrypted with the Android Keystore and is not included in backups. " +
        "You can remove the key and clear all history at any time from Settings."

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onOpenAi: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    onReplayLoading: () -> Unit = {},
    vm: SettingsViewModel = hiltViewModel()
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val rtState by vm.realtimeState.collectAsStateWithLifecycle()
    val about = rtState.aboutConfig
    val context = LocalContext.current
    var showPrivacy by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var showAdminLogin by remember { mutableStateOf(false) }

    val cs = MaterialTheme.colorScheme
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "17.0.0"
    }

    Screen(
        title = "Settings",
        actions = {
            // Admin Icon in Settings top bar ONLY appears if Device ID matches the single Owner Admin Device ID
            if (rtState.isAdminDevice) {
                IconButton(
                    onClick = {
                        if (rtState.isAdminAuthenticated) onOpenAdmin() else showAdminLogin = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Control Panel",
                        tint = cs.primary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Admin Section Card — ONLY visible when currentDeviceId matches the single Owner Admin Device ID!
            if (rtState.isAdminDevice) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AdminPanelSettings, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                            Text("Admin Control Panel", style = MaterialTheme.typography.titleMedium, color = cs.primary)
                        }
                        Text(
                            "Single Owner Admin Device verified (${rtState.totalUserCount} users registered). Manage License Keys, Watermarks, Developer About details, and User Device IDs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                if (rtState.isAdminAuthenticated) onOpenAdmin() else showAdminLogin = true
                            },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (rtState.isAdminAuthenticated) "Open Admin Section" else "Admin Login", fontSize = 12.sp)
                        }
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Theme & Startup Animation", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (m, label) ->
                            FilterChip(
                                selected = s.themeMode == m,
                                onClick = { vm.setTheme(m) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Dynamic color", style = MaterialTheme.typography.bodyMedium)
                                Text("Use colors from your wallpaper", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                            Switch(checked = s.dynamicColor, onCheckedChange = vm::setDynamic, modifier = Modifier.scale(0.82f))
                        }
                    } else {
                        Text("Dynamic color needs Android 12 or newer.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    FilledTonalButton(
                        onClick = onReplayLoading,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Replay 2 Next-Level Loading Screens", fontSize = 11.sp)
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AI model", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (s.endpoint.isBlank()) "Not configured" else "${s.model.ifBlank { "no model set" }} via ${s.provider.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenAi,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) { Text("AI model settings & API configuration", fontSize = 12.sp) }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Language", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "The app interface and curated library are in English. This choice sets the language AI models are asked to answer in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AI_LANGUAGES.forEach { l ->
                            FilterChip(
                                selected = s.aiLanguage == l,
                                onClick = { vm.setLanguage(l) },
                                label = { Text(l, fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Data", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val freed = clearCache(context.cacheDir)
                                Toast.makeText(context, "Cache cleared (${freed / 1024} KB)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) { Text("Clear cache", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { confirmClear = true },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) { Text("Clear history", fontSize = 11.sp) }
                    }
                    Text(
                        "History = recently viewed, search history and saved AI responses. Favorites are kept.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    // 7-tap version claim backdoor removed so no other phone can ever open or claim Admin
                    Text(
                        text = "UI/UX Name Explorer · version $version",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        about.developerAbout,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        "Developed by ${about.developerName}" +
                            if (about.developerRole.isNotBlank()) " · ${about.developerRole}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = cs.primary
                    )
                    if (about.developerEmail.isNotBlank()) {
                        Text("Contact: ${about.developerEmail}", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showPrivacy = true },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Privacy policy", fontSize = 11.sp) }
                        if (about.developerTelegram.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(about.developerTelegram)))
                                    }
                                },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) { Text("Telegram", fontSize = 11.sp) }
                        }
                        if (about.developerWebsite.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(about.developerWebsite)))
                                    }
                                },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) { Text("Website", fontSize = 11.sp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showAdminLogin && rtState.isAdminDevice) {
        AdminLoginDialog(
            onVerify = vm::verifyAdminLogin,
            onSuccess = {
                showAdminLogin = false
                onOpenAdmin()
            },
            onDismiss = { showAdminLogin = false }
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacy policy") },
            text = { Text(PRIVACY_TEXT) },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("Close") } }
        )
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear history?") },
            text = { Text("Recently viewed, search history and saved AI responses will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAllHistory(); confirmClear = false
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } }
        )
    }
}

private fun clearCache(dir: java.io.File): Long {
    var bytes = 0L
    dir.listFiles()?.forEach { f ->
        bytes += f.walkBottomUp().filter { it.isFile }.sumOf { it.length() }
        f.deleteRecursively()
    }
    return bytes
}
