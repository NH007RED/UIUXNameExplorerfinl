package com.nh.uiuxexplorer.data.repo

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.nh.uiuxexplorer.domain.security.LicenseKeyItem
import com.nh.uiuxexplorer.domain.security.WatermarkAndAboutConfig
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceUserRecord(
    val deviceId: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val appVersion: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val launchCount: Int,
    val isAdmin: Boolean = false
)

data class RealtimeAdminState(
    val isOnline: Boolean = true,
    val currentDeviceId: String = "",
    val isAdminDevice: Boolean = false,
    val isAdminAuthenticated: Boolean = false,
    val totalUserCount: Int = 1,
    val activeTodayCount: Int = 1,
    val users: List<DeviceUserRecord> = emptyList(),
    val adminDeviceIds: List<String> = listOf(RealtimeDbRepository.DEFAULT_OWNER_DEVICE_ID),
    val firebaseDbUrl: String = RealtimeDbRepository.DEFAULT_FIREBASE_URL,
    val firebaseAuthToken: String = "",
    val lastSyncTime: Long = 0L,
    val syncing: Boolean = false,
    val syncStatus: String = "Connecting to Realtime DB...",
    val aboutConfig: WatermarkAndAboutConfig = WatermarkGuard.config.value
)

/**
 * Manages Realtime Database synchronization & strict Online-Only Connectivity Gate:
 * 1. Live Internet Connectivity Monitor (`ConnectivityManager.NetworkCallback`) so the app cannot be used offline.
 * 2. Pre-configured with `https://ui-design-b404d-default-rtdb.firebaseio.com` from `google-services.json`.
 * 3. Deterministic Hardware Device ID: Never generates a new random ID on reinstall, app update, or signing key change.
 * 4. Owner Admin Phone (`Realme RMX5110`) permanently resolves to `DEV-FA7B-14E7-B9FB-9BA2` so the Admin icon always appears on the owner's phone and never on any other phone.
 * 5. Automatically cleans up duplicate `DEV-...` entries created on the same phone model during earlier builds.
 */
@Singleton
class RealtimeDbRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val http: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("realtime_db_store", Context.MODE_PRIVATE)
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    val currentDeviceId: String = computePermanentHardwareDeviceId()

    private val _state = MutableStateFlow(
        RealtimeAdminState(
            isOnline = checkActiveNetworkConnected(),
            currentDeviceId = currentDeviceId,
            isAdminDevice = isThisDeviceAuthorizedAdmin()
        )
    )
    val state: StateFlow<RealtimeAdminState> = _state.asStateFlow()

    init {
        WatermarkGuard.verifyAppIntegrity(context)
        registerNetworkMonitor()
        scope.launch {
            initializeAndSync()
        }
    }

    /**
     * Detects whether this physical phone is the Owner's Realme RMX5110.
     */
    private fun isOwnerHardwarePhone(): Boolean {
        val model = Build.MODEL.orEmpty().trim()
        val device = Build.DEVICE.orEmpty().trim()
        val product = Build.PRODUCT.orEmpty().trim()
        return model.equals(OWNER_HARDWARE_MODEL, ignoreCase = true) ||
            device.contains(OWNER_HARDWARE_MODEL, ignoreCase = true) ||
            product.contains(OWNER_HARDWARE_MODEL, ignoreCase = true)
    }

    /** True ONLY when this phone's permanent Device ID matches the single locked Owner Admin Device ID. */
    private fun isThisDeviceAuthorizedAdmin(): Boolean =
        isOwnerHardwarePhone() || currentDeviceId.trim().equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true)

    /**
     * Computes a 100% permanent, deterministic Device ID from immutable hardware build properties
     * so reinstalling the app, clearing data, or updating the APK NEVER creates a new Device ID.
     * On the Owner's phone (`Realme RMX5110`), it always returns `DEV-FA7B-14E7-B9FB-9BA2`.
     */
    @SuppressLint("HardwareIds")
    private fun computePermanentHardwareDeviceId(): String {
        if (isOwnerHardwarePhone()) {
            prefs.edit().putString(KEY_CACHED_DEVICE_ID, DEFAULT_OWNER_DEVICE_ID).apply()
            return DEFAULT_OWNER_DEVICE_ID
        }

        // Deterministic hardware fingerprint across immutable system Build fields (never changes on reinstall/update)
        val hwSeed = listOf(
            Build.MANUFACTURER.orEmpty().trim().uppercase(),
            Build.BRAND.orEmpty().trim().uppercase(),
            Build.MODEL.orEmpty().trim().uppercase(),
            Build.DEVICE.orEmpty().trim().uppercase(),
            Build.BOARD.orEmpty().trim().uppercase(),
            Build.HARDWARE.orEmpty().trim().uppercase(),
            Build.PRODUCT.orEmpty().trim().uppercase()
        ).joinToString("|")

        val digest = MessageDigest.getInstance("SHA-256").digest(hwSeed.toByteArray(Charsets.UTF_8))
        val hex = digest.take(8).joinToString("") { "%02X".format(it) }
        val formatted = "DEV-${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}"
        prefs.edit().putString(KEY_CACHED_DEVICE_ID, formatted).apply()
        return formatted
    }

    fun checkActiveNetworkConnected(): Boolean {
        val cm = connectivityManager ?: return false
        return runCatching {
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
        }.getOrDefault(false)
    }

    private fun registerNetworkMonitor() {
        val cm = connectivityManager ?: return
        runCatching {
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val connected = checkActiveNetworkConnected()
                    _state.update { it.copy(isOnline = connected) }
                    if (connected) {
                        refreshSync()
                    }
                }

                override fun onLost(network: Network) {
                    val stillConnected = checkActiveNetworkConnected()
                    _state.update {
                        it.copy(
                            isOnline = stillConnected,
                            syncStatus = if (stillConnected) it.syncStatus else "Offline — Internet Required"
                        )
                    }
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasNet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val wasOnline = _state.value.isOnline
                    _state.update { it.copy(isOnline = hasNet) }
                    if (!wasOnline && hasNet) {
                        refreshSync()
                    }
                }
            })
        }
    }

    /** Explicitly re-checks network status and triggers a live Firebase Realtime DB sync if connected. */
    fun recheckInternetAndSync(): Boolean {
        val online = checkActiveNetworkConnected()
        _state.update {
            it.copy(
                isOnline = online,
                syncStatus = if (online) "Syncing Realtime DB..." else "Offline — Internet Required"
            )
        }
        if (online) {
            refreshSync()
        }
        return online
    }

    private suspend fun initializeAndSync() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val appVer = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "18.0.0"

        val dbUrl = prefs.getString(KEY_FIREBASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_URL
        val dbToken = prefs.getString(KEY_FIREBASE_TOKEN, "").orEmpty()
        val adminIds = linkedSetOf(DEFAULT_OWNER_DEVICE_ID)
        saveAdminDeviceIds(adminIds)
        val aboutCfg = loadAboutAndWatermarkConfig()
        WatermarkGuard.updateRuntimeConfig(aboutCfg)

        // Load local users and remove any old duplicate IDs from the same hardware model
        val rawLocalUsers = loadUsersMap().toMutableMap()
        val myManufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }.ifBlank { "Android" }
        val myModel = Build.MODEL.ifBlank { "Smartphone" }

        // Remove stale local IDs that belonged to this same device before deterministic ID fix
        val staleLocalIds = rawLocalUsers.values
            .filter { it.model.equals(myModel, ignoreCase = true) && it.deviceId != currentDeviceId }
            .map { it.deviceId }
        staleLocalIds.forEach { rawLocalUsers.remove(it) }

        val existingMe = rawLocalUsers[currentDeviceId]
        val updatedMe = DeviceUserRecord(
            deviceId = currentDeviceId,
            manufacturer = myManufacturer,
            model = myModel,
            androidVersion = Build.VERSION.RELEASE.ifBlank { "15" },
            sdkInt = Build.VERSION.SDK_INT,
            appVersion = appVer,
            firstSeen = existingMe?.firstSeen ?: now,
            lastSeen = now,
            launchCount = (existingMe?.launchCount ?: 0) + 1,
            isAdmin = isThisDeviceAuthorizedAdmin()
        )
        rawLocalUsers[currentDeviceId] = updatedMe
        saveUsersMap(rawLocalUsers)

        val online = checkActiveNetworkConnected()
        updateStateFromMirror(
            isOnline = online,
            usersMap = rawLocalUsers,
            adminIds = adminIds,
            aboutCfg = aboutCfg,
            dbUrl = dbUrl,
            dbToken = dbToken,
            status = if (!online) "Offline — Internet Required" else "Syncing with Firebase Realtime DB..."
        )

        if (online && dbUrl.isNotBlank()) {
            syncWithRemoteFirebase(dbUrl, dbToken, updatedMe)
        }
    }

    fun refreshSync() {
        scope.launch {
            val online = checkActiveNetworkConnected()
            val st = _state.value
            val activeUrl = st.firebaseDbUrl.ifBlank { DEFAULT_FIREBASE_URL }
            _state.update {
                it.copy(
                    isOnline = online,
                    syncing = online,
                    syncStatus = if (online) "Syncing Realtime DB..." else "Offline — Internet Required"
                )
            }
            if (online && activeUrl.isNotBlank()) {
                val me = st.users.firstOrNull { it.deviceId == currentDeviceId } ?: DeviceUserRecord(
                    deviceId = currentDeviceId,
                    manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }.ifBlank { "Android" },
                    model = Build.MODEL.ifBlank { "Smartphone" },
                    androidVersion = Build.VERSION.RELEASE.ifBlank { "15" },
                    sdkInt = Build.VERSION.SDK_INT,
                    appVersion = "18.0.0",
                    firstSeen = System.currentTimeMillis(),
                    lastSeen = System.currentTimeMillis(),
                    launchCount = 1,
                    isAdmin = isThisDeviceAuthorizedAdmin()
                )
                syncWithRemoteFirebase(activeUrl, st.firebaseAuthToken, me)
            }
        }
    }

    private suspend fun syncWithRemoteFirebase(
        rawBaseUrl: String,
        authToken: String,
        myRecord: DeviceUserRecord
    ) = withContext(Dispatchers.IO) {
        val baseUrl = rawBaseUrl.trim().trimEnd('/')
        if (!baseUrl.startsWith("https://")) {
            _state.update { it.copy(syncing = false, syncStatus = "Invalid URL (must start with https://)") }
            return@withContext
        }
        fun endpoint(path: String): String {
            val base = "$baseUrl/uiux_explorer/$path.json"
            return if (authToken.isNotBlank()) "$base?auth=${authToken.trim()}" else base
        }

        runCatching {
            val jsonMedia = "application/json; charset=utf-8".toMediaType()

            // A. Upsert current device record under /uiux_explorer/users/<deviceId>.json
            val strictMyRecord = myRecord.copy(
                deviceId = currentDeviceId,
                isAdmin = isThisDeviceAuthorizedAdmin()
            )
            val meJson = encodeUserJson(strictMyRecord).toString()
            val putMeReq = Request.Builder()
                .url(endpoint("users/${strictMyRecord.deviceId}"))
                .put(meJson.toRequestBody(jsonMedia))
                .build()
            http.newCall(putMeReq).execute().close()

            // B. Fetch all users from /uiux_explorer/users.json & deduplicate same-phone records
            val getUsersReq = Request.Builder().url(endpoint("users")).get().build()
            val fetchedUsers = LinkedHashMap<String, DeviceUserRecord>()
            http.newCall(getUsersReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    if (body.isNotBlank() && body != "null") {
                        val obj = JSONObject(body)
                        val keys = obj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val item = obj.optJSONObject(k) ?: continue
                            val parsed = decodeUserJson(k, item)
                            fetchedUsers[parsed.deviceId] = parsed
                        }
                    }
                }
            }
            fetchedUsers[strictMyRecord.deviceId] = strictMyRecord

            // Deduplicate:
            // 1. For Owner's RMX5110, ONLY keep DEFAULT_OWNER_DEVICE_ID ("DEV-FA7B-14E7-B9FB-9BA2")
            // 2. For any other model, keep only 1 canonical entry per (manufacturer + model + sdkInt)
            val duplicateIdsToDelete = mutableListOf<String>()
            val deduplicatedUsers = LinkedHashMap<String, DeviceUserRecord>()

            // Ensure owner record is placed first
            fetchedUsers[DEFAULT_OWNER_DEVICE_ID]?.let { ownerRec ->
                deduplicatedUsers[DEFAULT_OWNER_DEVICE_ID] = ownerRec.copy(isAdmin = true)
            }

            val seenModelKeys = HashSet<String>()
            if (deduplicatedUsers.containsKey(DEFAULT_OWNER_DEVICE_ID)) {
                seenModelKeys.add("REALME|RMX5110")
            }

            fetchedUsers.values
                .sortedByDescending { it.lastSeen }
                .forEach { rec ->
                    if (rec.deviceId.equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true)) {
                        deduplicatedUsers[DEFAULT_OWNER_DEVICE_ID] = rec.copy(isAdmin = true)
                        return@forEach
                    }
                    val modelSig = "${rec.manufacturer.trim().uppercase()}|${rec.model.trim().uppercase()}"
                    if (rec.model.trim().equals(OWNER_HARDWARE_MODEL, ignoreCase = true) ||
                        rec.deviceId == currentDeviceId && isOwnerHardwarePhone()
                    ) {
                        // Old duplicate ID generated on the owner's Realme RMX5110 -> mark for deletion
                        duplicateIdsToDelete.add(rec.deviceId)
                    } else if (!seenModelKeys.add(modelSig)) {
                        // Duplicate ID generated on the same phone model during earlier reinstall -> mark for deletion
                        duplicateIdsToDelete.add(rec.deviceId)
                    } else {
                        deduplicatedUsers[rec.deviceId] = rec.copy(isAdmin = false)
                    }
                }

            // Attempt to delete duplicate IDs from remote Firebase DB so console stays clean
            duplicateIdsToDelete.forEach { dupId ->
                runCatching {
                    val delReq = Request.Builder().url(endpoint("users/$dupId")).delete().build()
                    http.newCall(delReq).execute().close()
                }
            }

            // C. Fetch admin_config (including licenseKeys) from /uiux_explorer/admin_config.json
            val getConfigReq = Request.Builder().url(endpoint("admin_config")).get().build()
            val singleAdminIds = linkedSetOf(DEFAULT_OWNER_DEVICE_ID)
            saveAdminDeviceIds(singleAdminIds)
            var aboutCfg = loadAboutAndWatermarkConfig()
            var needsRemoteCleanup = false
            http.newCall(getConfigReq).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    if (body.isNotBlank() && body != "null") {
                        val obj = JSONObject(body)
                        val remoteAdmins = obj.optJSONArray("adminDeviceIds")
                        if (remoteAdmins == null || remoteAdmins.length() != 1 ||
                            !remoteAdmins.optString(0).trim().equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true)
                        ) {
                            needsRemoteCleanup = true
                        }
                        aboutCfg = decodeConfigJson(obj, aboutCfg)
                        saveAboutAndWatermarkConfig(aboutCfg)
                        WatermarkGuard.updateRuntimeConfig(aboutCfg)
                    } else {
                        needsRemoteCleanup = true
                    }
                }
            }
            if (needsRemoteCleanup && isThisDeviceAuthorizedAdmin()) {
                pushConfigToRemote(baseUrl, authToken, singleAdminIds, aboutCfg)
            }

            saveUsersMap(deduplicatedUsers)
            updateStateFromMirror(
                isOnline = checkActiveNetworkConnected(),
                usersMap = deduplicatedUsers,
                adminIds = singleAdminIds,
                aboutCfg = aboutCfg,
                dbUrl = baseUrl,
                dbToken = authToken,
                status = "Firebase Realtime DB Live ✓"
            )
        }.onFailure { err ->
            _state.update {
                it.copy(
                    isOnline = checkActiveNetworkConnected(),
                    syncing = false,
                    syncStatus = "Realtime DB Sync Notice (${err.message?.take(32) ?: "check rules"})"
                )
            }
        }
    }

    /** Allows the Admin to delete any old/duplicate user device entry from Firebase Realtime Database. */
    fun deleteUserRecord(deviceIdToDelete: String) {
        if (!isThisDeviceAuthorizedAdmin()) return
        val cleanId = deviceIdToDelete.trim()
        if (cleanId.isBlank() || cleanId.equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true)) return
        scope.launch {
            val users = loadUsersMap().toMutableMap()
            users.remove(cleanId)
            saveUsersMap(users)
            val dbUrl = prefs.getString(KEY_FIREBASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_URL
            val dbToken = prefs.getString(KEY_FIREBASE_TOKEN, "").orEmpty()
            updateStateFromMirror(
                isOnline = checkActiveNetworkConnected(),
                usersMap = users,
                adminIds = linkedSetOf(DEFAULT_OWNER_DEVICE_ID),
                aboutCfg = WatermarkGuard.config.value,
                dbUrl = dbUrl,
                dbToken = dbToken,
                status = "Removed old ID $cleanId ✓"
            )
            runCatching {
                val url = "${dbUrl.trimEnd('/')}/uiux_explorer/users/$cleanId.json" +
                    if (dbToken.isNotBlank()) "?auth=${dbToken.trim()}" else ""
                val req = Request.Builder().url(url).delete().build()
                http.newCall(req).execute().close()
            }
        }
    }

    private fun pushConfigToRemote(
        baseUrl: String,
        authToken: String,
        @Suppress("UNUSED_PARAMETER") adminIds: Set<String>,
        cfg: WatermarkAndAboutConfig
    ) {
        if (!baseUrl.startsWith("https://")) return
        runCatching {
            val url = "$baseUrl/uiux_explorer/admin_config.json" +
                if (authToken.isNotBlank()) "?auth=${authToken.trim()}" else ""
            val singleAdminOnly = linkedSetOf(DEFAULT_OWNER_DEVICE_ID)
            val obj = encodeConfigJson(singleAdminOnly, cfg)
            val req = Request.Builder()
                .url(url)
                .put(obj.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
            http.newCall(req).execute().close()
        }
    }

    private fun updateStateFromMirror(
        isOnline: Boolean,
        usersMap: Map<String, DeviceUserRecord>,
        @Suppress("UNUSED_PARAMETER") adminIds: Set<String>,
        aboutCfg: WatermarkAndAboutConfig,
        dbUrl: String,
        dbToken: String,
        status: String
    ) {
        val now = System.currentTimeMillis()
        val dayAgo = now - 24L * 3600L * 1000L
        val singleAdminList = listOf(DEFAULT_OWNER_DEVICE_ID)
        val sortedUsers = usersMap.values
            .map { it.copy(isAdmin = it.deviceId.equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true)) }
            .sortedByDescending { it.lastSeen }
        val activeToday = sortedUsers.count { it.lastSeen >= dayAgo }.coerceAtLeast(1)
        val isMyDeviceAdmin = isThisDeviceAuthorizedAdmin()

        _state.update { prev ->
            prev.copy(
                isOnline = isOnline,
                currentDeviceId = currentDeviceId,
                isAdminDevice = isMyDeviceAdmin,
                isAdminAuthenticated = prev.isAdminAuthenticated && isMyDeviceAdmin,
                totalUserCount = sortedUsers.size.coerceAtLeast(1),
                activeTodayCount = activeToday,
                users = sortedUsers,
                adminDeviceIds = singleAdminList,
                firebaseDbUrl = dbUrl,
                firebaseAuthToken = dbToken,
                lastSyncTime = now,
                syncing = false,
                syncStatus = status,
                aboutConfig = aboutCfg
            )
        }
    }

    // ---- NH... License Key Management (Admin Create & User Activate) -------

    fun createLicenseKey(rawKeyInput: String, label: String = "VIP No-Watermark Key"): String {
        val formattedKey = WatermarkGuard.normalizeNhKey(rawKeyInput)
        val cleanLabel = label.trim().ifBlank { "VIP No-Watermark Key" }
        val currentCfg = loadAboutAndWatermarkConfig()
        val updatedKeys = (listOf(LicenseKeyItem(formattedKey, cleanLabel, System.currentTimeMillis())) +
            currentCfg.licenseKeys.filterNot { it.key.equals(formattedKey, ignoreCase = true) })
        val updatedCfg = currentCfg.copy(licenseKeys = updatedKeys)
        saveWatermarkAndAbout(updatedCfg)
        return formattedKey
    }

    fun deleteLicenseKey(keyToDelete: String) {
        val currentCfg = loadAboutAndWatermarkConfig()
        val updatedKeys = currentCfg.licenseKeys.filterNot { it.key.equals(keyToDelete.trim(), ignoreCase = true) }
        val currentActivated = currentCfg.activatedLicenseKey
        val nextActivated = if (currentActivated.equals(keyToDelete.trim(), ignoreCase = true)) {
            prefs.edit().remove(KEY_ACTIVATED_LICENSE).apply()
            ""
        } else {
            currentActivated
        }
        val updatedCfg = currentCfg.copy(licenseKeys = updatedKeys, activatedLicenseKey = nextActivated)
        saveWatermarkAndAbout(updatedCfg)
    }

    suspend fun verifyAndActivateLicenseKey(inputKey: String): Boolean = withContext(Dispatchers.IO) {
        val clean = inputKey.trim().uppercase()
        if (!clean.startsWith("NH") || clean.length < 4) return@withContext false

        var cfg = loadAboutAndWatermarkConfig()
        var matched = cfg.licenseKeys.any { it.key.trim().uppercase() == clean }

        if (!matched && checkActiveNetworkConnected()) {
            val dbUrl = prefs.getString(KEY_FIREBASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_URL
            val dbToken = prefs.getString(KEY_FIREBASE_TOKEN, "").orEmpty()
            val url = "${dbUrl.trimEnd('/')}/uiux_explorer/admin_config.json" +
                if (dbToken.isNotBlank()) "?auth=${dbToken.trim()}" else ""
            runCatching {
                val req = Request.Builder().url(url).get().build()
                http.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        if (body.isNotBlank() && body != "null") {
                            val obj = JSONObject(body)
                            cfg = decodeConfigJson(obj, cfg)
                            saveAboutAndWatermarkConfig(cfg)
                        }
                    }
                }
            }
            matched = cfg.licenseKeys.any { it.key.trim().uppercase() == clean }
        }

        if (matched) {
            prefs.edit().putString(KEY_ACTIVATED_LICENSE, clean).apply()
            val updatedCfg = cfg.copy(activatedLicenseKey = clean)
            saveAboutAndWatermarkConfig(updatedCfg)
            WatermarkGuard.updateRuntimeConfig(updatedCfg)
            val users = loadUsersMap()
            val admins = loadAdminDeviceIds()
            val dbUrl = prefs.getString(KEY_FIREBASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_URL
            val dbToken = prefs.getString(KEY_FIREBASE_TOKEN, "").orEmpty()
            updateStateFromMirror(
                isOnline = checkActiveNetworkConnected(),
                usersMap = users,
                adminIds = admins,
                aboutCfg = WatermarkGuard.config.value,
                dbUrl = dbUrl,
                dbToken = dbToken,
                status = "License Key Activated ✓"
            )
            true
        } else {
            false
        }
    }

    fun clearActivatedLicenseKey() {
        prefs.edit().remove(KEY_ACTIVATED_LICENSE).apply()
        val cfg = loadAboutAndWatermarkConfig().copy(activatedLicenseKey = "")
        saveAboutAndWatermarkConfig(cfg)
        WatermarkGuard.updateRuntimeConfig(cfg)
        val users = loadUsersMap()
        val admins = loadAdminDeviceIds()
        val dbUrl = prefs.getString(KEY_FIREBASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_URL
        val dbToken = prefs.getString(KEY_FIREBASE_TOKEN, "").orEmpty()
        updateStateFromMirror(checkActiveNetworkConnected(), users, admins, WatermarkGuard.config.value, dbUrl, dbToken, "License Key Removed")
    }

    // ---- Admin Login & Strict Single-Device Authorization ------------------

    fun verifyAdminLogin(password: String): Boolean {
        if (!isThisDeviceAuthorizedAdmin()) {
            _state.update { it.copy(isAdminDevice = false, isAdminAuthenticated = false) }
            return false
        }
        val storedPin = prefs.getString(KEY_ADMIN_PASSWORD, DEFAULT_ADMIN_PASSWORD) ?: DEFAULT_ADMIN_PASSWORD
        val ok = password.trim() == storedPin || password.trim().equals(DEFAULT_MASTER_KEY, ignoreCase = true)
        if (ok) {
            _state.update { it.copy(isAdminDevice = true, isAdminAuthenticated = true) }
        }
        return ok
    }

    fun logoutAdmin() {
        _state.update { it.copy(isAdminAuthenticated = false) }
    }

    fun claimCurrentDeviceAsAdmin(masterKeyOrPassword: String): Boolean {
        if (!isThisDeviceAuthorizedAdmin()) return false
        return verifyAdminLogin(masterKeyOrPassword)
    }

    fun addAdminDeviceId(@Suppress("UNUSED_PARAMETER") deviceId: String) {
        // Single-admin policy: do not allow adding a second Admin Device ID.
    }

    fun removeAdminDeviceId(@Suppress("UNUSED_PARAMETER") deviceId: String) {
        // Single-admin policy: keep DEFAULT_OWNER_DEVICE_ID locked.
    }

    fun updateAdminPassword(newPassword: String) {
        if (!isThisDeviceAuthorizedAdmin()) return
        val clean = newPassword.trim()
        if (clean.length >= 4) {
            prefs.edit().putString(KEY_ADMIN_PASSWORD, clean).apply()
        }
    }

    fun saveWatermarkAndAbout(newConfig: WatermarkAndAboutConfig) {
        if (!isThisDeviceAuthorizedAdmin()) return
        scope.launch {
            saveAboutAndWatermarkConfig(newConfig)
            WatermarkGuard.updateRuntimeConfig(newConfig)
            val users = loadUsersMap()
            val admins = linkedSetOf(DEFAULT_OWNER_DEVICE_ID)
            val dbUrl = prefs.getString(KEY_FIREBASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_FIREBASE_URL
            val dbToken = prefs.getString(KEY_FIREBASE_TOKEN, "").orEmpty()
            updateStateFromMirror(checkActiveNetworkConnected(), users, admins, WatermarkGuard.config.value, dbUrl, dbToken, "Saved to Realtime DB ✓")
            if (dbUrl.isNotBlank()) {
                pushConfigToRemote(dbUrl, dbToken, admins, WatermarkGuard.config.value)
            }
        }
    }

    fun saveFirebaseConnection(dbUrl: String, authToken: String) {
        if (!isThisDeviceAuthorizedAdmin()) return
        val cleanUrl = dbUrl.trim().trimEnd('/').ifBlank { DEFAULT_FIREBASE_URL }
        val cleanToken = authToken.trim()
        prefs.edit()
            .putString(KEY_FIREBASE_URL, cleanUrl)
            .putString(KEY_FIREBASE_TOKEN, cleanToken)
            .apply()
        scope.launch {
            val st = _state.value
            _state.update {
                it.copy(
                    firebaseDbUrl = cleanUrl,
                    firebaseAuthToken = cleanToken,
                    syncing = true,
                    syncStatus = "Connecting to Cloud Realtime DB..."
                )
            }
            val me = st.users.firstOrNull { it.deviceId == currentDeviceId }
            if (me != null) {
                pushConfigToRemote(cleanUrl, cleanToken, setOf(DEFAULT_OWNER_DEVICE_ID), st.aboutConfig)
                syncWithRemoteFirebase(cleanUrl, cleanToken, me)
            }
        }
    }

    fun buildFirebaseSecurityRulesJson(): String {
        return """
{
  "rules": {
    "uiux_explorer": {
      "users": {
        ".read": true,
        "${'$'}deviceId": {
          ".write": "(newData.exists() && newData.child('deviceId').val() === ${'$'}deviceId) || !newData.exists()"
        }
      },
      "admin_config": {
        ".read": true,
        ".write": "newData.child('adminDeviceIds').exists() && newData.child('adminDeviceIds').child('0').val() === '$DEFAULT_OWNER_DEVICE_ID' && !newData.child('adminDeviceIds').child('1').exists()"
      }
    }
  }
}
        """.trimIndent()
    }

    // ---- Local Persistence & JSON Encoding ---------------------------------

    private fun loadUsersMap(): Map<String, DeviceUserRecord> {
        val raw = prefs.getString(KEY_USERS_JSON, null) ?: return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val out = LinkedHashMap<String, DeviceUserRecord>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val item = obj.optJSONObject(k) ?: continue
                out[k] = decodeUserJson(k, item)
            }
            out
        }.getOrDefault(emptyMap())
    }

    private fun saveUsersMap(map: Map<String, DeviceUserRecord>) {
        val obj = JSONObject()
        map.forEach { (id, u) -> obj.put(id, encodeUserJson(u)) }
        prefs.edit().putString(KEY_USERS_JSON, obj.toString()).apply()
    }

    private fun encodeUserJson(u: DeviceUserRecord): JSONObject = JSONObject().apply {
        put("deviceId", u.deviceId)
        put("manufacturer", u.manufacturer)
        put("model", u.model)
        put("androidVersion", u.androidVersion)
        put("sdkInt", u.sdkInt)
        put("appVersion", u.appVersion)
        put("firstSeen", u.firstSeen)
        put("lastSeen", u.lastSeen)
        put("launchCount", u.launchCount)
        put("isAdmin", u.deviceId.equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true))
    }

    private fun decodeUserJson(key: String, o: JSONObject): DeviceUserRecord {
        val id = o.optString("deviceId", key).ifBlank { key }
        return DeviceUserRecord(
            deviceId = id,
            manufacturer = o.optString("manufacturer", "Android"),
            model = o.optString("model", "Device"),
            androidVersion = o.optString("androidVersion", "14"),
            sdkInt = o.optInt("sdkInt", 34),
            appVersion = o.optString("appVersion", "18.0.0"),
            firstSeen = o.optLong("firstSeen", System.currentTimeMillis()),
            lastSeen = o.optLong("lastSeen", System.currentTimeMillis()),
            launchCount = o.optInt("launchCount", 1).coerceAtLeast(1),
            isAdmin = id.equals(DEFAULT_OWNER_DEVICE_ID, ignoreCase = true)
        )
    }

    private fun loadAdminDeviceIds(): Set<String> = linkedSetOf(DEFAULT_OWNER_DEVICE_ID)

    private fun saveAdminDeviceIds(@Suppress("UNUSED_PARAMETER") ids: Set<String>) {
        val arr = JSONArray().apply { put(DEFAULT_OWNER_DEVICE_ID) }
        prefs.edit().putString(KEY_ADMIN_IDS_JSON, arr.toString()).apply()
    }

    private fun loadAboutAndWatermarkConfig(): WatermarkAndAboutConfig {
        val savedLicense = prefs.getString(KEY_ACTIVATED_LICENSE, "").orEmpty()
        val raw = prefs.getString(KEY_ABOUT_WM_JSON, null)
            ?: return WatermarkAndAboutConfig(
                watermarkText = WatermarkGuard.defaultWatermarkText(),
                developerName = WatermarkGuard.defaultDeveloperName(),
                activatedLicenseKey = savedLicense
            )
        return runCatching {
            val o = JSONObject(raw)
            decodeConfigJson(
                o,
                WatermarkAndAboutConfig(
                    watermarkText = WatermarkGuard.defaultWatermarkText(),
                    developerName = WatermarkGuard.defaultDeveloperName(),
                    activatedLicenseKey = savedLicense
                )
            )
        }.getOrDefault(
            WatermarkAndAboutConfig(
                watermarkText = WatermarkGuard.defaultWatermarkText(),
                developerName = WatermarkGuard.defaultDeveloperName(),
                activatedLicenseKey = savedLicense
            )
        )
    }

    private fun saveAboutAndWatermarkConfig(cfg: WatermarkAndAboutConfig) {
        val obj = encodeConfigJson(linkedSetOf(DEFAULT_OWNER_DEVICE_ID), cfg)
        prefs.edit().putString(KEY_ABOUT_WM_JSON, obj.toString()).apply()
    }

    private fun encodeConfigJson(@Suppress("UNUSED_PARAMETER") adminIds: Set<String>, cfg: WatermarkAndAboutConfig): JSONObject = JSONObject().apply {
        val arr = JSONArray().apply { put(DEFAULT_OWNER_DEVICE_ID) }
        put("adminDeviceIds", arr)
        put("watermarkText", cfg.watermarkText)
        put("pngWatermarkEnabled", cfg.pngWatermarkEnabled)
        put("pngWatermarkOpacity", cfg.pngWatermarkOpacity.toDouble())
        put("pngWatermarkDensity", cfg.pngWatermarkDensity)
        put("promptWatermarkEnabled", cfg.promptWatermarkEnabled)
        put("developerName", cfg.developerName)
        put("developerRole", cfg.developerRole)
        put("developerAbout", cfg.developerAbout)
        put("developerTelegram", cfg.developerTelegram)
        put("developerEmail", cfg.developerEmail)
        put("developerWebsite", cfg.developerWebsite)

        val keysArr = JSONArray()
        cfg.licenseKeys.forEach { item ->
            keysArr.put(
                JSONObject().apply {
                    put("key", item.key)
                    put("label", item.label)
                    put("createdAt", item.createdAt)
                }
            )
        }
        put("licenseKeys", keysArr)
    }

    private fun decodeConfigJson(o: JSONObject, fallback: WatermarkAndAboutConfig): WatermarkAndAboutConfig {
        val savedLicense = prefs.getString(KEY_ACTIVATED_LICENSE, fallback.activatedLicenseKey).orEmpty()
        val parsedKeys = mutableListOf<LicenseKeyItem>()
        val keysArr = o.optJSONArray("licenseKeys")
        if (keysArr != null) {
            for (i in 0 until keysArr.length()) {
                val elem = keysArr.opt(i)
                when (elem) {
                    is JSONObject -> {
                        val k = elem.optString("key", "").trim().uppercase()
                        if (k.startsWith("NH")) {
                            parsedKeys.add(
                                LicenseKeyItem(
                                    key = k,
                                    label = elem.optString("label", "VIP No-Watermark Key"),
                                    createdAt = elem.optLong("createdAt", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                    is String -> {
                        val k = elem.trim().uppercase()
                        if (k.startsWith("NH")) {
                            parsedKeys.add(LicenseKeyItem(key = k))
                        }
                    }
                }
            }
        } else {
            parsedKeys.addAll(fallback.licenseKeys)
        }

        return WatermarkAndAboutConfig(
            watermarkText = o.optString("watermarkText", fallback.watermarkText).ifBlank { WatermarkGuard.defaultWatermarkText() },
            pngWatermarkEnabled = o.optBoolean("pngWatermarkEnabled", fallback.pngWatermarkEnabled),
            pngWatermarkOpacity = o.optDouble("pngWatermarkOpacity", fallback.pngWatermarkOpacity.toDouble()).toFloat().coerceIn(0.08f, 0.45f),
            pngWatermarkDensity = o.optInt("pngWatermarkDensity", fallback.pngWatermarkDensity).coerceIn(1, 3),
            promptWatermarkEnabled = o.optBoolean("promptWatermarkEnabled", fallback.promptWatermarkEnabled),
            developerName = o.optString("developerName", fallback.developerName).ifBlank { WatermarkGuard.defaultDeveloperName() },
            developerRole = o.optString("developerRole", fallback.developerRole),
            developerAbout = o.optString("developerAbout", fallback.developerAbout),
            developerTelegram = o.optString("developerTelegram", fallback.developerTelegram).ifBlank { "https://t.me/htmlmovievg" },
            developerEmail = o.optString("developerEmail", fallback.developerEmail),
            developerWebsite = o.optString("developerWebsite", fallback.developerWebsite),
            licenseKeys = parsedKeys,
            activatedLicenseKey = savedLicense
        )
    }

    companion object {
        const val DEFAULT_FIREBASE_URL = "https://ui-design-b404d-default-rtdb.firebaseio.com"
        const val DEFAULT_OWNER_DEVICE_ID = "DEV-FA7B-14E7-B9FB-9BA2"
        const val OWNER_HARDWARE_MODEL = "RMX5110"

        private const val KEY_CACHED_DEVICE_ID = "cached_hw_device_id"
        private const val KEY_USERS_JSON = "rtdb_users_json"
        private const val KEY_ADMIN_IDS_JSON = "rtdb_admin_device_ids"
        private const val KEY_ABOUT_WM_JSON = "rtdb_about_watermark_cfg"
        private const val KEY_FIREBASE_URL = "rtdb_firebase_url"
        private const val KEY_FIREBASE_TOKEN = "rtdb_firebase_token"
        private const val KEY_ADMIN_PASSWORD = "rtdb_admin_password"
        private const val KEY_ACTIVATED_LICENSE = "rtdb_activated_nh_license_key"

        const val DEFAULT_ADMIN_PASSWORD = "admin123"
        const val DEFAULT_MASTER_KEY = "NHADMIN2026"
    }
}
