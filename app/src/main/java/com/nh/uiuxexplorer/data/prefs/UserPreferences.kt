package com.nh.uiuxexplorer.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nh.uiuxexplorer.domain.model.AiProvider
import com.nh.uiuxexplorer.domain.model.AppSettings
import com.nh.uiuxexplorer.domain.model.FrameStyle
import com.nh.uiuxexplorer.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val ENDPOINT = stringPreferencesKey("ai_endpoint")
        val MODEL = stringPreferencesKey("ai_model")
        val PROVIDER = stringPreferencesKey("ai_provider")
        val LANGUAGE = stringPreferencesKey("ai_language")
        val FRAME = stringPreferencesKey("designer_frame")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            AppSettings(
                themeMode = runCatching { ThemeMode.valueOf(p[Keys.THEME] ?: "") }.getOrDefault(ThemeMode.SYSTEM),
                dynamicColor = p[Keys.DYNAMIC] ?: false,
                endpoint = p[Keys.ENDPOINT] ?: "",
                model = p[Keys.MODEL] ?: "",
                provider = runCatching { AiProvider.valueOf(p[Keys.PROVIDER] ?: "") }
                    .getOrDefault(AiProvider.OPENAI_COMPATIBLE),
                aiLanguage = p[Keys.LANGUAGE] ?: "English",
                frameStyle = runCatching { FrameStyle.valueOf(p[Keys.FRAME] ?: "") }.getOrDefault(FrameStyle.PUNCH_HOLE)
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC] = enabled }
    }

    suspend fun setFrameStyle(style: FrameStyle) {
        context.dataStore.edit { it[Keys.FRAME] = style.name }
    }

    suspend fun setAiLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setAiConfig(endpoint: String, model: String, provider: AiProvider) {
        context.dataStore.edit {
            it[Keys.ENDPOINT] = endpoint.trim()
            it[Keys.MODEL] = model.trim()
            it[Keys.PROVIDER] = provider.name
        }
    }
}
