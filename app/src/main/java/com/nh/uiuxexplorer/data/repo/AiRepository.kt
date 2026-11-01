package com.nh.uiuxexplorer.data.repo

import com.nh.uiuxexplorer.data.local.AiResponseDao
import com.nh.uiuxexplorer.data.local.AiResponseEntity
import com.nh.uiuxexplorer.data.prefs.UserPreferences
import com.nh.uiuxexplorer.data.remote.AiClient
import com.nh.uiuxexplorer.data.remote.AiResult
import com.nh.uiuxexplorer.data.secure.SecureKeyStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(
    private val client: AiClient,
    private val dao: AiResponseDao,
    private val prefs: UserPreferences,
    private val keys: SecureKeyStore
) {
    val history: Flow<List<AiResponseEntity>> = dao.observeAll()

    /** Sends a prompt and stores only genuine successful responses in history. */
    suspend fun ask(prompt: String, designId: String?): AiResult {
        val settings = prefs.settings.first()
        val fullPrompt = if (settings.aiLanguage == "English") prompt
        else "$prompt\n\nPlease answer in ${settings.aiLanguage}."
        val result = client.send(settings, keys.load(), fullPrompt)
        if (result.ok) {
            dao.insert(
                AiResponseEntity(
                    designId = designId,
                    prompt = prompt,
                    response = result.text,
                    model = settings.model,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    suspend fun testConnection(): AiResult {
        val settings = prefs.settings.first()
        return client.send(settings, keys.load(), "Reply with the single word: OK")
    }

    suspend fun deleteResponse(id: Long) = dao.delete(id)

    suspend fun clearHistory() = dao.clear()
}
