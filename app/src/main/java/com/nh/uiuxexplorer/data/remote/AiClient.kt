package com.nh.uiuxexplorer.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import com.nh.uiuxexplorer.domain.model.AiProvider
import com.nh.uiuxexplorer.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

data class AiResult(
    val ok: Boolean,
    val text: String,
    val httpCode: Int? = null,
    val latencyMs: Long = 0
)

/** Sends one prompt to the user-configured endpoint. Never fabricates a response. */
@Singleton
class AiClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AiApi
) {
    suspend fun send(settings: AppSettings, apiKey: String?, prompt: String): AiResult {
        val endpoint = settings.endpoint.trim()
        if (endpoint.isEmpty()) return fail("No API endpoint configured. Open AI Model Settings first.")
        if (!endpoint.startsWith("https://")) return fail("Only https:// endpoints are allowed.")
        if (endpoint.toHttpUrlOrNull() == null) return fail("The endpoint is not a valid URL.")
        if (settings.model.isBlank()) return fail("No model name configured.")
        if (settings.provider == AiProvider.ANTHROPIC && apiKey.isNullOrBlank()) {
            return fail("An API key is required for the Anthropic provider.")
        }
        if (!isOnline()) {
            return fail("No internet connection. The offline library still works; AI responses need a network.")
        }

        val headers = mutableMapOf("Accept" to "application/json")
        val json = JSONObject()
        when (settings.provider) {
            AiProvider.OPENAI_COMPATIBLE -> {
                if (!apiKey.isNullOrBlank()) headers["Authorization"] = "Bearer $apiKey"
                json.put("model", settings.model.trim())
                json.put(
                    "messages",
                    JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
                )
            }
            AiProvider.ANTHROPIC -> {
                headers["x-api-key"] = apiKey.orEmpty()
                headers["anthropic-version"] = "2023-06-01"
                json.put("model", settings.model.trim())
                json.put("max_tokens", 1024)
                json.put(
                    "messages",
                    JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
                )
            }
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val start = SystemClock.elapsedRealtime()
        return try {
            val response = api.post(endpoint, headers, body)
            val raw = (if (response.isSuccessful) response.body() else response.errorBody())
                ?.string().orEmpty()
            val latency = SystemClock.elapsedRealtime() - start
            if (!response.isSuccessful) {
                AiResult(false, "HTTP ${response.code()}: ${extractError(raw)}", response.code(), latency)
            } else {
                val text = parseText(settings.provider, raw)
                if (text.isNullOrBlank()) {
                    AiResult(false, "The server replied, but no text could be read from the response.", response.code(), latency)
                } else {
                    AiResult(true, text.trim(), response.code(), latency)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: UnknownHostException) {
            fail("Host not found. Check the endpoint URL and your connection.")
        } catch (e: SocketTimeoutException) {
            fail("The request timed out.")
        } catch (e: IOException) {
            fail("Network error: ${e.message ?: "unknown"}")
        } catch (e: Exception) {
            fail("Unexpected error: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun fail(message: String) = AiResult(false, message)

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun parseText(provider: AiProvider, raw: String): String? = try {
        val root = JSONObject(raw)
        when (provider) {
            AiProvider.OPENAI_COMPATIBLE -> {
                val msg = root.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
                if (msg.isNull("content")) null else msg.getString("content")
            }
            AiProvider.ANTHROPIC -> {
                val arr = root.getJSONArray("content")
                buildString {
                    for (i in 0 until arr.length()) {
                        val block = arr.getJSONObject(i)
                        if (block.optString("type") == "text") append(block.optString("text"))
                    }
                }
            }
        }
    } catch (e: Exception) {
        null
    }

    private fun extractError(raw: String): String {
        if (raw.isBlank()) return "empty response body"
        return try {
            val root = JSONObject(raw)
            val err = root.opt("error")
            when (err) {
                is JSONObject -> err.optString("message").ifBlank { err.toString() }
                is String -> err
                else -> root.optString("message").ifBlank { raw.take(300) }
            }
        } catch (e: Exception) {
            raw.take(300)
        }
    }
}
