package com.nh.uiuxexplorer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.data.repo.DeviceUserRecord
import com.nh.uiuxexplorer.domain.security.WatermarkAndAboutConfig
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.PillLabel
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.common.shareText
import com.nh.uiuxexplorer.ui.vm.AdminViewModel
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

/**
 * Admin Login Dialog shown when tapping the Admin Shield icon (which is only visible when
 * the device's hardware Device ID matches the Realtime Database's `adminDeviceIds`).
 */
@Composable
fun AdminLoginDialog(
    onVerify: (String) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = { Text("Admin Login", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Your Device ID is authorized in the Realtime Database. Enter your Admin Password to open the Admin Control Panel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMsg = null
                    },
                    label = { Text("Admin Password", fontSize = 11.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerify(password)) {
                        onSuccess()
                    } else {
                        errorMsg = "Incorrect Admin Password"
                    }
                },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Text("Login", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(32.dp)) {
                Text("Cancel", fontSize = 11.sp)
            }
        }
    )
}

/**
 * Owner Device ID Bootstrap Dialog (triggered by tapping the app version 7 times in Settings -> About).
 * Registers the owner's current Device ID into the Realtime Database's `adminDeviceIds` list so the
 * Admin Icon appears on the owner's phone and remains hidden on all other users' phones.
 */
@Composable
fun AdminClaimDeviceDialog(
    currentDeviceId: String,
    isAlreadyAdmin: Boolean,
    onClaim: (String) -> Boolean,
    onOpenAdminPanel: () -> Unit,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var masterKey by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        },
        title = { Text("Realtime DB Admin Device Setup", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Register this phone's Device ID as an Admin Device in the Realtime Database. " +
                        "Only phones matching an authorized Admin Device ID will ever see the Admin icon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("This Phone's Device ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(currentDeviceId, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(currentDeviceId))
                                Toast.makeText(context, "Device ID copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, "Copy Device ID", modifier = Modifier.size(15.dp))
                        }
                    }
                }
                if (isAlreadyAdmin) {
                    Text(
                        "✓ This Device ID is already authorized in Realtime Database!",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF00C853)
                    )
                }
                OutlinedTextField(
                    value = masterKey,
                    onValueChange = {
                        masterKey = it
                        errorMsg = null
                    },
                    label = { Text("Master Admin Key / Password (default: NHADMIN2026)", fontSize = 10.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onClaim(masterKey)) {
                        Toast.makeText(context, "Device ID authorized in Realtime DB!", Toast.LENGTH_SHORT).show()
                        onOpenAdminPanel()
                    } else {
                        errorMsg = "Invalid Master Admin Key"
                    }
                },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Text(if (isAlreadyAdmin) "Open Admin Panel" else "Authorize Device ID", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(32.dp)) {
                Text("Cancel", fontSize = 11.sp)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onReplayLoadingScreens: () -> Unit,
    vm: AdminViewModel = hiltViewModel()
) {
    val st by vm.state.collectAsStateWithLifecycle()
    if (!st.isAdminDevice) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }
        return
    }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val cs = MaterialTheme.colorScheme

    var tab by remember { mutableIntStateOf(0) }
    var userSearch by remember { mutableStateOf("") }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    fun copyText(label: String, value: String) {
        clipboard.setText(AnnotatedString(value))
        toast("$label copied")
    }

    Screen(
        title = "Admin Control Panel",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    vm.refreshSync()
                    toast("Syncing Realtime Database...")
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.CloudSync, "Sync Realtime DB", modifier = Modifier.size(18.dp), tint = cs.primary)
            }
            IconButton(
                onClick = onReplayLoadingScreens,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.PlayArrow, "Replay 2 Loading Screens", modifier = Modifier.size(18.dp))
            }
        }
    ) {
        // Top Realtime DB Status Strip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = cs.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.VerifiedUser, null, tint = cs.primary, modifier = Modifier.size(15.dp))
                    Text(
                        text = "${st.syncStatus} · ${st.totalUserCount} Users · ${st.aboutConfig.licenseKeys.size} Keys",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (st.syncing) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                modifier = Modifier.height(34.dp),
                text = { Text("Users (${st.totalUserCount})", fontSize = 11.sp) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                modifier = Modifier.height(34.dp),
                text = { Text("License Keys (${st.aboutConfig.licenseKeys.size})", fontSize = 11.sp) }
            )
            Tab(
                selected = tab == 2,
                onClick = { tab = 2 },
                modifier = Modifier.height(34.dp),
                text = { Text("Watermark", fontSize = 11.sp) }
            )
            Tab(
                selected = tab == 3,
                onClick = { tab = 3 },
                modifier = Modifier.height(34.dp),
                text = { Text("About / Dev", fontSize = 11.sp) }
            )
            Tab(
                selected = tab == 4,
                onClick = { tab = 4 },
                modifier = Modifier.height(34.dp),
                text = { Text("Realtime DB", fontSize = 11.sp) }
            )
        }

        when (tab) {
            0 -> AdminUsersTab(
                users = st.users,
                currentDeviceId = st.currentDeviceId,
                totalUsers = st.totalUserCount,
                activeToday = st.activeTodayCount,
                searchQuery = userSearch,
                onSearchChange = { userSearch = it },
                onCopyDeviceId = { copyText("Device ID", it) },
                onToggleAdminDevice = { id, _ ->
                    vm.deleteUserRecord(id)
                    toast("Removed old Device ID $id")
                },
                modifier = Modifier.weight(1f)
            )
            1 -> AdminLicenseKeysTab(
                config = st.aboutConfig,
                onGenerateRandomKey = vm::generateNewNhLicenseKey,
                onCreateKey = { rawKey, label ->
                    val savedKey = vm.createLicenseKey(rawKey, label)
                    clipboard.setText(AnnotatedString(savedKey))
                    toast("License Key $savedKey saved to Realtime DB & copied!")
                },
                onDeleteKey = { key ->
                    vm.deleteLicenseKey(key)
                    toast("License Key $key removed from Realtime DB")
                },
                onCopyKey = { key -> copyText("License Key", key) },
                onSaveTelegramLink = { link ->
                    vm.saveWatermarkAndAbout(st.aboutConfig.copy(developerTelegram = link.trim()))
                    toast("Telegram link saved to Realtime DB!")
                },
                modifier = Modifier.weight(1f)
            )
            2 -> AdminWatermarkTab(
                initialConfig = st.aboutConfig,
                onOpenLicenseTab = { tab = 1 },
                onSave = { updated ->
                    vm.saveWatermarkAndAbout(updated)
                    toast("Watermark settings saved to Realtime DB!")
                },
                modifier = Modifier.weight(1f)
            )
            3 -> AdminDeveloperAboutTab(
                initialConfig = st.aboutConfig,
                onSave = { updated ->
                    vm.saveWatermarkAndAbout(updated)
                    toast("Developer & About details saved to Realtime DB!")
                },
                modifier = Modifier.weight(1f)
            )
            4 -> AdminRealtimeDbSecurityTab(
                currentDeviceId = st.currentDeviceId,
                adminDeviceIds = st.adminDeviceIds,
                firebaseUrl = st.firebaseDbUrl,
                firebaseToken = st.firebaseAuthToken,
                rulesJson = vm.buildFirebaseRulesJson(),
                onCopyText = ::copyText,
                onAddAdminId = { id ->
                    vm.addAdminDeviceId(id)
                    toast("Admin Device ID added")
                },
                onRemoveAdminId = { id ->
                    vm.removeAdminDeviceId(id)
                    toast("Admin Device ID removed")
                },
                onSaveFirebase = { url, token ->
                    vm.saveFirebaseConnection(url, token)
                    toast("Realtime Database connection saved!")
                },
                onChangePassword = { newPin ->
                    vm.updateAdminPassword(newPin)
                    toast("Admin login password updated!")
                },
                onReplayLoading = onReplayLoadingScreens,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// TAB 1: USER LIST & DEVICE IDs (FROM REALTIME DATABASE)
// ============================================================================

@Composable
private fun AdminUsersTab(
    users: List<DeviceUserRecord>,
    currentDeviceId: String,
    totalUsers: Int,
    activeToday: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCopyDeviceId: (String) -> Unit,
    onToggleAdminDevice: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val filtered = remember(users, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) users
        else users.filter {
            it.deviceId.lowercase().contains(q) ||
                it.model.lowercase().contains(q) ||
                it.manufacturer.lowercase().contains(q)
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCard(Modifier.weight(1f)) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Groups, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                    Column {
                        Text("$totalUsers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Total Devices", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }
            GlassCard(Modifier.weight(1f)) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00C853), modifier = Modifier.size(18.dp))
                    Column {
                        Text("$activeToday", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Active Today", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
            placeholder = { Text("Search by Device ID, brand, or phone model...", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(filtered, key = { it.deviceId }) { u ->
                val isMe = u.deviceId == currentDeviceId
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${u.manufacturer} ${u.model}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isMe) {
                                    PillLabel("This Phone")
                                }
                                if (u.isAdmin) {
                                    PillLabel("Admin")
                                }
                            }
                            Text(
                                text = "${u.launchCount} launches",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.primary
                            )
                        }

                        // Device ID Row (Visible ONLY here in Admin Panel)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = cs.surface.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ID: ${u.deviceId}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = cs.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(
                                        onClick = { onCopyDeviceId(u.deviceId) },
                                        label = { Text("Copy ID", fontSize = 10.sp) },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(12.dp)) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                    PillLabel(if (u.isAdmin) "Owner Admin ✓" else "User")
                                    if (!u.isAdmin) {
                                        IconButton(
                                            onClick = { onToggleAdminDevice(u.deviceId, false) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, "Delete Old ID", tint = cs.error, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Android ${u.androidVersion} (API ${u.sdkInt}) · v${u.appVersion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                            Text(
                                text = "Last: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(u.lastSeen))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 2: LICENSE KEY GENERATOR & REALTIME DATABASE MANAGER (NH... KEYS)
// ============================================================================

@Composable
private fun AdminLicenseKeysTab(
    config: WatermarkAndAboutConfig,
    onGenerateRandomKey: () -> String,
    onCreateKey: (String, String) -> Unit,
    onDeleteKey: (String) -> Unit,
    onCopyKey: (String) -> Unit,
    onSaveTelegramLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var keyInput by remember { mutableStateOf(onGenerateRandomKey()) }
    var keyLabel by remember { mutableStateOf("") }
    var telegramUrl by remember(config.developerTelegram) { mutableStateOf(config.developerTelegram) }

    val previewNormalized = remember(keyInput) { WatermarkGuard.normalizeNhKey(keyInput) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Create NH... License Key Card
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Key, null, tint = cs.primary, modifier = Modifier.size(17.dp))
                            Text("Create License Key (First 2 Letters: NH...)", style = MaterialTheme.typography.titleMedium)
                        }
                        PillLabel("Without Watermark")
                    }
                    Text(
                        "Create a License Key starting with 'NH' and save it to the Realtime Database. " +
                            "When a user enters a saved NH... key on the Save PNG screen, watermarks are removed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { raw ->
                                val up = raw.uppercase()
                                keyInput = if (up.startsWith("NH")) up else "NH$up"
                            },
                            label = { Text("License Key (Starts with NH...)", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalButton(
                            onClick = { keyInput = onGenerateRandomKey() },
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Auto NH", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = keyLabel,
                        onValueChange = { keyLabel = it },
                        label = { Text("User Name / Note (optional, e.g. Rahul VIP)", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = cs.primaryContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Key to Save in Realtime DB:", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                            Text(
                                text = previewNormalized,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = cs.primary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onCreateKey(keyInput, keyLabel.ifBlank { "VIP User" })
                            keyInput = onGenerateRandomKey()
                            keyLabel = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Create & Save NH License Key to Realtime DB", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Contact Admin Telegram Channel Link Card (Shown on Save PNG Dialog)
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Contact Admin Telegram Link (Shown on Save PNG Dialog)", style = MaterialTheme.typography.titleSmall, color = cs.primary)
                    Text(
                        "When users click 'Contact Admin on Telegram for License Key' inside the Save PNG dialog, this link opens:",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = telegramUrl,
                            onValueChange = { telegramUrl = it },
                            label = { Text("Telegram Channel / Admin Link (https://t.me/...)", fontSize = 10.sp) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { onSaveTelegramLink(telegramUrl) },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Save Link", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. Saved Active License Keys List Header
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Active NH... License Keys in Realtime DB (${config.licenseKeys.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Live Cloud Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary
                )
            }
        }

        if (config.licenseKeys.isEmpty()) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(
                        "No license keys created yet. Generate an NH... key above and tap 'Create & Save' to store it in the Realtime Database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(config.licenseKeys, key = { it.key }) { item ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.key,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = cs.primary
                                )
                                PillLabel(item.label.ifBlank { "Active Key" })
                            }
                            Text(
                                text = "Created: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.createdAt))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(
                                onClick = { onCopyKey(item.key) },
                                label = { Text("Copy", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(12.dp)) },
                                modifier = Modifier.height(26.dp)
                            )
                            IconButton(
                                onClick = {
                                    shareText(
                                        context,
                                        "UI/UX Name Explorer License Key (Without Watermark): ${item.key}",
                                        "License Key"
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Share, "Share Key", modifier = Modifier.size(15.dp), tint = cs.primary)
                            }
                            IconButton(
                                onClick = { onDeleteKey(item.key) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete Key", modifier = Modifier.size(15.dp), tint = cs.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 3: WATERMARK SETTINGS (TILED PNG MICRO-WATERMARK & PROMPT WATERMARK)
// ============================================================================

@Composable
private fun AdminWatermarkTab(
    initialConfig: WatermarkAndAboutConfig,
    onOpenLicenseTab: () -> Unit,
    onSave: (WatermarkAndAboutConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    var wmText by remember(initialConfig.watermarkText) { mutableStateOf(initialConfig.watermarkText) }
    var pngEnabled by remember(initialConfig.pngWatermarkEnabled) { mutableStateOf(initialConfig.pngWatermarkEnabled) }
    var pngOpacity by remember(initialConfig.pngWatermarkOpacity) { mutableFloatStateOf(initialConfig.pngWatermarkOpacity) }
    var pngDensity by remember(initialConfig.pngWatermarkDensity) { mutableIntStateOf(initialConfig.pngWatermarkDensity) }
    var promptEnabled by remember(initialConfig.promptWatermarkEnabled) { mutableStateOf(initialConfig.promptWatermarkEnabled) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Shortcut to NH... License Keys
        GlassCard(Modifier.fillMaxWidth(), onClick = onOpenLicenseTab) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Key, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                    Column {
                        Text(
                            "NH... License Keys (Without Watermark)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${initialConfig.licenseKeys.size} active keys synced in Realtime Database",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(
                    onClick = onOpenLicenseTab,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("Manage Keys", fontSize = 10.sp)
                }
            }
        }

        // Live Tiled Micro-Watermark Preview Card
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Live Export PNG Micro-Watermark Preview (Chhoti-Chhoti Har Jagah)",
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.primary
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1A1F3C), Color(0xFF3A1C71), Color(0xFF00C2D1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated UI content
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Exported UI Design Preview",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    // Repeated tiny diagonal watermarks everywhere
                    if (pngEnabled) {
                        val displayWm = wmText.ifBlank { "UI/UX Name Explorer • NH Developer" }
                        val rows = if (pngDensity == 3) 5 else if (pngDensity == 2) 4 else 3
                        Column(
                            Modifier
                                .fillMaxSize()
                                .rotate(-18f),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(rows) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(3) {
                                        Text(
                                            text = displayWm,
                                            color = Color.White.copy(alpha = pngOpacity.coerceIn(0.08f, 0.45f) * 1.4f),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WaterDrop, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                        Text("Watermark Configuration", style = MaterialTheme.typography.titleMedium)
                    }
                }

                OutlinedTextField(
                    value = wmText,
                    onValueChange = { wmText = it },
                    label = { Text("Watermark Text (shown on PNGs & Prompts)", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("PNG Export Tiled Micro-Watermark", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Stamps tiny transparent watermarks all over exported PNGs",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Switch(checked = pngEnabled, onCheckedChange = { pngEnabled = it }, modifier = Modifier.scale(0.82f))
                }

                Text(
                    "Watermark Transparency / Opacity: ${(pngOpacity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
                Slider(
                    value = pngOpacity,
                    onValueChange = { pngOpacity = it },
                    valueRange = 0.08f..0.42f,
                    modifier = Modifier.height(26.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Tile Grid Density:", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    listOf(1 to "Light", 2 to "Standard", 3 to "Dense (Har Jagah)").forEach { (lvl, label) ->
                        FilterChip(
                            selected = pngDensity == lvl,
                            onClick = { pngDensity = lvl },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Prompt Watermark Attribution", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Automatically appends watermark signature to every generated prompt & layout explanation",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Switch(checked = promptEnabled, onCheckedChange = { promptEnabled = it }, modifier = Modifier.scale(0.82f))
                }

                Button(
                    onClick = {
                        onSave(
                            initialConfig.copy(
                                watermarkText = wmText.trim(),
                                pngWatermarkEnabled = pngEnabled,
                                pngWatermarkOpacity = pngOpacity,
                                pngWatermarkDensity = pngDensity,
                                promptWatermarkEnabled = promptEnabled
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("Save Watermark Settings to Realtime DB", fontSize = 12.sp)
                }
            }
        }
    }
}

// ============================================================================
// TAB 4: ADMIN / DEVELOPER ABOUT DETAILS
// ============================================================================

@Composable
private fun AdminDeveloperAboutTab(
    initialConfig: WatermarkAndAboutConfig,
    onSave: (WatermarkAndAboutConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    var devName by remember(initialConfig.developerName) { mutableStateOf(initialConfig.developerName) }
    var devRole by remember(initialConfig.developerRole) { mutableStateOf(initialConfig.developerRole) }
    var devAbout by remember(initialConfig.developerAbout) { mutableStateOf(initialConfig.developerAbout) }
    var devTelegram by remember(initialConfig.developerTelegram) { mutableStateOf(initialConfig.developerTelegram) }
    var devEmail by remember(initialConfig.developerEmail) { mutableStateOf(initialConfig.developerEmail) }
    var devWebsite by remember(initialConfig.developerWebsite) { mutableStateOf(initialConfig.developerWebsite) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                    Text("Admin / Developer About Details", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "These details sync via the Realtime Database and display live in the Settings -> About section and Save PNG license dialog.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                OutlinedTextField(
                    value = devName,
                    onValueChange = { devName = it },
                    label = { Text("Developer / Admin Name", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = devRole,
                    onValueChange = { devRole = it },
                    label = { Text("Developer Role / Headline", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = devAbout,
                    onValueChange = { devAbout = it },
                    label = { Text("App & Developer About Description", fontSize = 11.sp) },
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = devTelegram,
                    onValueChange = { devTelegram = it },
                    label = { Text("Telegram Channel / Contact Link", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = devEmail,
                    onValueChange = { devEmail = it },
                    label = { Text("Contact Email (optional)", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = devWebsite,
                    onValueChange = { devWebsite = it },
                    label = { Text("Portfolio / Website URL (optional)", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onSave(
                            initialConfig.copy(
                                developerName = devName.trim(),
                                developerRole = devRole.trim(),
                                developerAbout = devAbout.trim(),
                                developerTelegram = devTelegram.trim(),
                                developerEmail = devEmail.trim(),
                                developerWebsite = devWebsite.trim()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("Save Developer About Details", fontSize = 12.sp)
                }
            }
        }
    }
}

// ============================================================================
// TAB 5: REALTIME DATABASE CONNECTION, DEVICE ID MATCHING & SECURITY RULES
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminRealtimeDbSecurityTab(
    currentDeviceId: String,
    adminDeviceIds: List<String>,
    firebaseUrl: String,
    firebaseToken: String,
    rulesJson: String,
    onCopyText: (String, String) -> Unit,
    onAddAdminId: (String) -> Unit,
    onRemoveAdminId: (String) -> Unit,
    onSaveFirebase: (String, String) -> Unit,
    onChangePassword: (String) -> Unit,
    onReplayLoading: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    var dbUrl by remember(firebaseUrl) { mutableStateOf(firebaseUrl) }
    var dbToken by remember(firebaseToken) { mutableStateOf(firebaseToken) }
    var newAdminDeviceId by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Single Locked Admin Device ID Card (No Second Device Allowed)
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Security, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                        Text("Single Admin Device Lock", style = MaterialTheme.typography.titleMedium)
                    }
                    PillLabel("1 Device Only")
                }
                Text(
                    "Admin access is permanently locked to your single Owner Device ID below. " +
                        "No second device can ever be added to Firebase or open the Admin Panel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cs.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Locked Owner Admin Device ID (Max 1 Device)", style = MaterialTheme.typography.labelSmall, color = cs.primary)
                            Text(
                                adminDeviceIds.firstOrNull() ?: currentDeviceId,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        AssistChip(
                            onClick = { onCopyText("Device ID", adminDeviceIds.firstOrNull() ?: currentDeviceId) },
                            label = { Text("Copy ID", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(12.dp)) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }
        }

        // 2. Firebase Realtime Database Connection Card
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Firebase Realtime Database Endpoint", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Paste your Firebase Realtime Database URL (e.g. https://your-project-default-rtdb.firebaseio.com) to sync all user counts, Device IDs, NH... license keys, watermarks, and developer info to the cloud.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
                OutlinedTextField(
                    value = dbUrl,
                    onValueChange = { dbUrl = it },
                    label = { Text("Realtime Database URL (https://...)", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dbToken,
                    onValueChange = { dbToken = it },
                    label = { Text("Database Auth Token / Secret (optional)", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onSaveFirebase(dbUrl, dbToken) },
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("Save & Sync Realtime Database", fontSize = 12.sp)
                }
            }
        }

        // 3. Firebase Realtime Database Security Rules Card
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Realtime Database Security Rules (rules.json)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Copy and paste these rules into Firebase Console -> Realtime Database -> Rules. It pre-fills your authorized Admin Device ID.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cs.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rulesJson,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                OutlinedButton(
                    onClick = { onCopyText("Firebase Rules JSON", rulesJson) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Copy Firebase Rules JSON", fontSize = 11.sp)
                }
            }
        }

        // 4. Admin Login Password & Loading Screen Replay Card
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Lock, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                    Text("Admin Login Password & Loading Preview", style = MaterialTheme.typography.titleMedium)
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Admin Password (min 4 chars)", fontSize = 10.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newPassword.trim().length >= 4) {
                                onChangePassword(newPassword.trim())
                                newPassword = ""
                            }
                        },
                        enabled = newPassword.trim().length >= 4,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("Update PIN", fontSize = 11.sp)
                    }
                }
                FilledTonalButton(
                    onClick = onReplayLoading,
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Preview 2 Animated Loading Screens Again", fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}
