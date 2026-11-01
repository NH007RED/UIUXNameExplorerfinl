package com.nh.uiuxexplorer.domain.security

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom

data class LicenseKeyItem(
    val key: String,
    val label: String = "VIP No-Watermark Key",
    val createdAt: Long = System.currentTimeMillis()
)

data class WatermarkAndAboutConfig(
    val watermarkText: String = "",
    val pngWatermarkEnabled: Boolean = true,
    val pngWatermarkOpacity: Float = 0.18f,
    val pngWatermarkDensity: Int = 2, // 1 = Sparse, 2 = Standard, 3 = Dense ("chhoti chhoti har jagah")
    val promptWatermarkEnabled: Boolean = true,
    val developerName: String = "",
    val developerRole: String = "Lead UI/UX Architect & Android Developer",
    val developerAbout: String = "A searchable library of UI/UX design names, styles, systems and patterns with live Compose previews, interactive layout designer, and live AI lookups.",
    val developerTelegram: String = "https://t.me/htmlmovievg",
    val developerEmail: String = "",
    val developerWebsite: String = "",
    val licenseKeys: List<LicenseKeyItem> = emptyList(),
    val activatedLicenseKey: String = ""
) {
    /**
     * True when the user has entered a valid License Key starting with "NH" that exists in the
     * Realtime Database's active `licenseKeys` list.
     */
    val isLicenseValid: Boolean
        get() {
            val clean = activatedLicenseKey.trim().uppercase()
            if (!clean.startsWith("NH") || clean.length < 4) return false
            return licenseKeys.any { it.key.trim().uppercase() == clean }
        }
}

/**
 * Multi-layer Anti-Screenshot (`FLAG_SECURE`), Anti-Tamper, Signature Pinning, License Key & Watermark Guard:
 *
 * 1. Blocks all Screenshots, Screen Recording, Cast capture, and Recent Apps previews via obfuscated `FLAG_SECURE`.
 * 2. Continuously verifies that `FLAG_SECURE` (`0x2000`) remains set on the Activity `Window`.
 * 3. Verifies APK package identity (`com.nh.uiuxexplorer`), release keystore SHA-256 certificate fingerprint,
 *    and checks against hooking frameworks (Xposed / LSPosed / Substrate / Frida).
 * 4. If anyone modifies the APK or removes/hooks `FLAG_SECURE`, `isTampered` becomes `true` and the app locks itself.
 */
object WatermarkGuard {
    private const val XOR_KEY = 0x5A
    private const val ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val rng = SecureRandom()

    // Obfuscated "UI/UX Name Explorer • NH Developer"
    private val OBF_DEFAULT_WATERMARK = intArrayOf(
        0x0F, 0x13, 0x75, 0x0F, 0x02, 0x7A, 0x14, 0x3B, 0x37, 0x3F, 0x7A,
        0x1F, 0x22, 0x2A, 0x36, 0x35, 0x28, 0x3F, 0x28, 0x7A, 0x77, 0x7A,
        0x14, 0x12, 0x7A, 0x1E, 0x3F, 0x2C, 0x3F, 0x36, 0x35, 0x2A, 0x3F, 0x28
    )

    // Obfuscated "NH Developer"
    private val OBF_DEFAULT_DEV = intArrayOf(
        0x14, 0x12, 0x7A, 0x1E, 0x3F, 0x2C, 0x3F, 0x36, 0x35, 0x2A, 0x3F, 0x28
    )

    // Obfuscated "com.nh.uiuxexplorer"
    private val OBF_EXPECTED_PKG = intArrayOf(
        0x39, 0x35, 0x37, 0x74, 0x34, 0x32, 0x74, 0x2F, 0x33, 0x2F, 0x22,
        0x3F, 0x22, 0x2A, 0x36, 0x35, 0x28, 0x3F, 0x28
    )

    // Obfuscated SHA-256 of official release-keystore.jks:
    // "0C055EC5CC11733D8E98CB36B811FA301B28C66165E9BE0307006D7858F114E3"
    private val OBF_EXPECTED_CERT_SHA256 = intArrayOf(
        0x6A, 0x19, 0x6A, 0x6F, 0x6F, 0x1F, 0x19, 0x6F, 0x19, 0x19, 0x6B, 0x6B, 0x6D, 0x69, 0x69, 0x1E,
        0x62, 0x1F, 0x63, 0x62, 0x19, 0x18, 0x69, 0x6C, 0x18, 0x62, 0x6B, 0x6B, 0x1C, 0x1B, 0x69, 0x6A,
        0x6B, 0x18, 0x68, 0x62, 0x19, 0x6C, 0x6C, 0x6B, 0x6C, 0x6F, 0x1F, 0x63, 0x18, 0x1F, 0x6A, 0x69,
        0x6A, 0x6D, 0x6A, 0x6A, 0x6C, 0x1E, 0x6D, 0x62, 0x6F, 0x62, 0x1C, 0x6B, 0x6B, 0x6E, 0x1F, 0x69
    )

    private fun decodeObfuscated(arr: IntArray): String {
        val chars = CharArray(arr.size)
        for (i in arr.indices) {
            chars[i] = (arr[i] xor XOR_KEY).toChar()
        }
        return String(chars).replace(" - ", " • ")
    }

    fun defaultWatermarkText(): String = decodeObfuscated(OBF_DEFAULT_WATERMARK)
    fun defaultDeveloperName(): String = decodeObfuscated(OBF_DEFAULT_DEV)
    fun expectedCertSha256(): String = decodeObfuscated(OBF_EXPECTED_CERT_SHA256)

    /**
     * Dynamically computes WindowManager.LayoutParams.FLAG_SECURE (0x00002000 = 8192)
     * without storing literal 0x2000 in bytecode so simple Smali searches cannot strip it.
     */
    fun resolveSecureWindowFlag(): Int {
        val a = 0x7F3A
        val b = 0x5F3A
        return a xor b // 0x2000 (8192 = FLAG_SECURE)
    }

    private val _config = MutableStateFlow(
        WatermarkAndAboutConfig(
            watermarkText = defaultWatermarkText(),
            developerName = defaultDeveloperName()
        )
    )
    val config: StateFlow<WatermarkAndAboutConfig> = _config.asStateFlow()

    private val _isTampered = MutableStateFlow(false)
    val isTampered: StateFlow<Boolean> = _isTampered.asStateFlow()

    private val _tamperReason = MutableStateFlow("")
    val tamperReason: StateFlow<String> = _tamperReason.asStateFlow()

    @Volatile
    private var tamperedPackage: Boolean = false

    fun updateConfig(newConfig: WatermarkAndAboutConfig) = updateRuntimeConfig(newConfig)

    fun updateRuntimeConfig(newConfig: WatermarkAndAboutConfig) {
        val safeWm = newConfig.watermarkText.trim().ifBlank { defaultWatermarkText() }
        val safeDev = newConfig.developerName.trim().ifBlank { defaultDeveloperName() }
        val safeTg = newConfig.developerTelegram.trim().ifBlank { "https://t.me/htmlmovievg" }
        _config.value = newConfig.copy(
            watermarkText = safeWm,
            developerName = safeDev,
            developerTelegram = safeTg,
            pngWatermarkOpacity = newConfig.pngWatermarkOpacity.coerceIn(0.08f, 0.45f),
            pngWatermarkDensity = newConfig.pngWatermarkDensity.coerceIn(1, 3)
        )
    }

    /** Generates a new formatted license key starting with "NH" (e.g. "NH-8F4K-29XM"). */
    fun generateNewNhLicenseKey(): String {
        val part1 = (1..4).map { ALPHABET[rng.nextInt(ALPHABET.length)] }.joinToString("")
        val part2 = (1..4).map { ALPHABET[rng.nextInt(ALPHABET.length)] }.joinToString("")
        return "NH-$part1-$part2"
    }

    /** Normalizes any admin-entered key so the first 2 characters are always "NH". */
    fun normalizeNhKey(input: String): String {
        val cleaned = input.trim().uppercase().replace(" ", "")
        if (cleaned.isBlank()) return generateNewNhLicenseKey()
        return if (cleaned.startsWith("NH")) {
            if (cleaned.length < 4) cleaned + (1..4).map { ALPHABET[rng.nextInt(ALPHABET.length)] }.joinToString("")
            else cleaned
        } else {
            "NH-$cleaned"
        }
    }

    /**
     * Applies FLAG_SECURE to block all screenshots & screen recordings, disables Recents screen capture,
     * and verifies that neither the APK signature nor the window's FLAG_SECURE bit has been modified/hooked.
     */
    fun enforceAndVerifySecureWindow(activity: Activity): Boolean {
        return runCatching {
            val win = activity.window ?: return false
            val secureBit = resolveSecureWindowFlag()

            // 1. Apply FLAG_SECURE via both addFlags and setFlags
            win.addFlags(secureBit)
            win.setFlags(secureBit, secureBit)

            // 2. Disable Android 13+ Recent Apps task snapshot screenshot
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runCatching { activity.setRecentsScreenshotEnabled(false) }
            }

            // 3. Read back window flags to verify FLAG_SECURE was not stripped by bytecode mod or Xposed hook
            val currentFlags = win.attributes?.flags ?: 0
            val flagIntact = (currentFlags and secureBit) == secureBit

            // 4. Verify APK signature & anti-hook integrity
            val appIntact = verifyAppIntegrity(activity)

            if (!flagIntact) {
                markTampered("Screenshot / Screen Recording protection (FLAG_SECURE) was removed or bypassed.")
                false
            } else if (!appIntact) {
                false
            } else {
                true
            }
        }.getOrElse {
            markTampered("Window security verification failed.")
            false
        }
    }

    private fun markTampered(reason: String) {
        tamperedPackage = true
        _tamperReason.value = reason
        _isTampered.value = true
    }

    /**
     * Detects Xposed, LSPosed, EdXposed, Substrate, or Frida hooking frameworks that attempt
     * to bypass FLAG_SECURE or tamper with runtime security checks.
     */
    private fun detectHookingFramework(): Boolean {
        val suspiciousClasses = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XC_MethodHook",
            "com.saurik.substrate.MS\$2"
        )
        for (cls in suspiciousClasses) {
            val loaded = runCatching { Class.forName(cls) }.isSuccess
            if (loaded) return true
        }
        val stackHasHook = Throwable().stackTrace.any { frame ->
            val cn = frame.className.lowercase()
            cn.contains("xposed") || cn.contains("lsposed") || cn.contains("edxposed") || cn.contains("frida")
        }
        if (stackHasHook) return true

        val mapsHasFridaOrXposed = runCatching {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists() && mapsFile.canRead()) {
                mapsFile.useLines { lines ->
                    lines.any { line ->
                        val lower = line.lowercase()
                        lower.contains("frida-agent") || lower.contains("frida-gadget") || lower.contains("xposedbridge.jar")
                    }
                }
            } else {
                false
            }
        }.getOrDefault(false)

        return mapsHasFridaOrXposed
    }

    /**
     * Verifies:
     * 1. Package name matches `com.nh.uiuxexplorer`.
     * 2. APK signing certificate SHA-256 matches the official `release-keystore.jks` certificate.
     * 3. No Xposed/LSPosed/Frida hooking framework is active.
     * If any modification is detected, locks the entire application (`isTampered = true`).
     */
    @Suppress("DEPRECATION")
    fun verifyAppIntegrity(context: Context): Boolean {
        return runCatching {
            val expectedPkg = decodeObfuscated(OBF_EXPECTED_PKG)
            if (context.packageName != expectedPkg) {
                markTampered("Application package name was modified (${context.packageName}).")
                return false
            }

            if (detectHookingFramework()) {
                markTampered("Hooking or screenshot-bypass module (Xposed / LSPosed / Frida) detected.")
                return false
            }

            val certBytesList: List<ByteArray> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = info.signingInfo
                val sigs = if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners
                    else signingInfo.signingCertificateHistory
                } else null
                sigs?.map { it.toByteArray() }.orEmpty()
            } else {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                info.signatures?.map { it.toByteArray() }.orEmpty()
            }

            if (certBytesList.isEmpty()) {
                markTampered("APK signing certificate is missing or stripped.")
                return false
            }

            val expectedSha256 = expectedCertSha256()
            val certMatched = certBytesList.any { bytes ->
                val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
                val hex = digest.joinToString("") { "%02X".format(it) }
                hex.equals(expectedSha256, ignoreCase = true)
            }

            if (!certMatched) {
                markTampered("APK modification detected: Signing certificate does not match official release key.")
                return false
            }

            true
        }.getOrElse {
            markTampered("APK integrity verification failed.")
            false
        }
    }

    /** True when the user has unlocked watermark-free export via a valid Realtime DB "NH..." License Key. */
    fun isWatermarkUnlockedByLicense(): Boolean {
        if (tamperedPackage) return false
        return _config.value.isLicenseValid
    }

    /**
     * Returns the active watermark text to stamp across exported PNGs.
     * Returns empty string when a valid "NH..." License Key is active.
     */
    fun resolvePngWatermark(): String {
        val cur = _config.value
        if (tamperedPackage) return defaultWatermarkText()
        if (cur.isLicenseValid) return ""
        if (!cur.pngWatermarkEnabled) return ""
        return cur.watermarkText.trim().ifBlank { defaultWatermarkText() }
    }

    fun resolvePngOpacity(): Float {
        if (tamperedPackage) return 0.22f
        return _config.value.pngWatermarkOpacity.coerceIn(0.08f, 0.45f)
    }

    fun resolvePngDensity(): Int {
        if (tamperedPackage) return 3
        return _config.value.pngWatermarkDensity.coerceIn(1, 3)
    }

    /**
     * Returns the watermark signature appended to every generated UI prompt and layout explanation.
     * Returns empty string when a valid "NH..." License Key is active.
     */
    fun resolvePromptSignature(): String {
        val cur = _config.value
        if (!tamperedPackage && (cur.isLicenseValid || !cur.promptWatermarkEnabled)) return ""
        val wm = cur.watermarkText.trim().ifBlank { defaultWatermarkText() }
        val dev = cur.developerName.trim().ifBlank { defaultDeveloperName() }
        return "[Watermark: $wm | Crafted with UI/UX Name Explorer by $dev]"
    }
}
