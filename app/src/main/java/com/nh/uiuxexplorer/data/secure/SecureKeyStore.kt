package com.nh.uiuxexplorer.data.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the user's API key encrypted with an AES-256-GCM key that lives in the Android Keystore.
 * The key is never hardcoded, never logged and never included in backups (allowBackup=false).
 */
@Singleton
class SecureKeyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("secure_ai", Context.MODE_PRIVATE)

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    fun save(apiKey: String): Boolean {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            clear()
            return true
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val iv = cipher.iv
            val cipherText = cipher.doFinal(trimmed.toByteArray(Charsets.UTF_8))
            val blob = Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(cipherText, Base64.NO_WRAP)
            prefs.edit().putString(KEY_BLOB, blob).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun load(): String? {
        val blob = prefs.getString(KEY_BLOB, null) ?: return null
        return try {
            val parts = blob.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val data = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(data), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun hasKey(): Boolean = prefs.contains(KEY_BLOB)

    /** Returns something like "••••1234" so the UI can confirm a key exists without revealing it. */
    fun masked(): String? {
        val k = load() ?: return null
        return "••••" + k.takeLast(4)
    }

    fun clear() {
        prefs.edit().remove(KEY_BLOB).apply()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "uiux_name_explorer_ai_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BLOB = "api_key_blob"
    }
}
