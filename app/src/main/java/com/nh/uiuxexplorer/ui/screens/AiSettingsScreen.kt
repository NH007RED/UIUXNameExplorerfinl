package com.nh.uiuxexplorer.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.model.AiProvider
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.vm.AiSettingsViewModel
import com.nh.uiuxexplorer.ui.vm.AiUiState
import java.text.DateFormat
import java.util.Date

private data class Preset(val label: String, val url: String, val provider: AiProvider)

private val presets = listOf(
    Preset("OpenAI", "https://api.openai.com/v1/chat/completions", AiProvider.OPENAI_COMPATIBLE),
    Preset("Anthropic", "https://api.anthropic.com/v1/messages", AiProvider.ANTHROPIC),
    Preset("OpenRouter", "https://openrouter.ai/api/v1/chat/completions", AiProvider.OPENAI_COMPATIBLE),
    Preset("Groq", "https://api.groq.com/openai/v1/chat/completions", AiProvider.OPENAI_COMPATIBLE),
    Preset("Gemini (OpenAI-compat)", "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions", AiProvider.OPENAI_COMPATIBLE)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiSettingsScreen(onBack: () -> Unit, vm: AiSettingsViewModel = hiltViewModel()) {
    val stored by vm.settings.collectAsStateWithLifecycle()
    val masked by vm.maskedKey.collectAsStateWithLifecycle()
    val test by vm.test.collectAsStateWithLifecycle()
    val playground by vm.playground.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val cs = MaterialTheme.colorScheme

    var endpoint by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf(AiProvider.OPENAI_COMPATIBLE) }
    var keyInput by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(stored) {
        val s = stored
        if (s != null && !initialized) {
            endpoint = s.endpoint; model = s.model; provider = s.provider; initialized = true
        }
    }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    Screen("AI Model Settings", onBack = onBack) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Security notice", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Your API key is encrypted with an Android Keystore key and never hardcoded. " +
                            "Even so, calling an AI provider directly from a phone can expose the key (for example on a rooted device " +
                            "or through a shared key). For production, point the endpoint at your own secure backend proxy that holds the real key; " +
                            "an OpenAI-compatible proxy needs no key here. Only https:// endpoints are accepted. " +
                            "The offline library never needs any of this.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Connection", style = MaterialTheme.typography.titleMedium)
                    Text("Presets (fill the endpoint, you still choose the model)", style = MaterialTheme.typography.labelSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presets.forEach { p ->
                            AssistChip(
                                onClick = { endpoint = p.url; provider = p.provider },
                                label = { Text(p.label, fontSize = 11.sp) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                    Text("Request format", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AiProvider.entries.forEach { p ->
                            FilterChip(
                                selected = provider == p,
                                onClick = { provider = p },
                                label = { Text(p.label, fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                    OutlinedTextField(
                        endpoint,
                        { endpoint = it },
                        label = { Text("API endpoint (full https URL)", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    OutlinedTextField(
                        model,
                        { model = it },
                        label = { Text("Model name", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { vm.saveConfig(endpoint, model, provider); toast("Settings saved") },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) { Text("Save connection settings", fontSize = 12.sp) }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("API key", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (masked != null) "A key is stored securely ($masked)" else "No key stored",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    OutlinedTextField(
                        keyInput,
                        { keyInput = it },
                        label = { Text("Paste API key", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (keyInput.isBlank()) toast("Paste a key first")
                                else if (vm.saveKey(keyInput)) { keyInput = ""; toast("Key stored securely") }
                                else toast("Could not write to secure storage")
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) { Text("Save key", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { vm.clearKey(); toast("Key removed") },
                            enabled = masked != null,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) { Text("Remove key", fontSize = 11.sp) }
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Test connection", style = MaterialTheme.typography.titleMedium)
                    Text("Save settings first. Sends one short real request.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    Button(
                        onClick = vm::runTest,
                        enabled = !test.running,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) { Text("Run test", fontSize = 11.sp) }
                    if (test.running) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    test.result?.let { r ->
                        Text(
                            if (r.ok) "Connected (${r.latencyMs} ms). Model replied: ${r.text.take(120)}" else r.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (r.ok) cs.primary else cs.error
                        )
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Model response viewer", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        prompt,
                        { prompt = it },
                        label = { Text("Ask the model anything", fontSize = 11.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Button(
                        onClick = { vm.ask(prompt) },
                        enabled = playground !is AiUiState.Loading && prompt.isNotBlank(),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) { Text("Send", fontSize = 11.sp) }
                    when (val s = playground) {
                        AiUiState.Idle -> {}
                        AiUiState.Loading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        is AiUiState.Error -> Text(s.message, color = cs.error, style = MaterialTheme.typography.bodySmall)
                        is AiUiState.Success -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("LIVE AI RESPONSE - ${s.model} - ${s.latencyMs} ms", style = MaterialTheme.typography.labelSmall, color = cs.tertiary)
                            Text(s.text, style = MaterialTheme.typography.bodyMedium)
                            TextButton(
                                onClick = { clipboard.setText(AnnotatedString(s.text)); toast("Copied") },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp)
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
                        Text("Response history", style = MaterialTheme.typography.titleMedium)
                        TextButton(
                            onClick = vm::clearHistory,
                            enabled = history.isNotEmpty(),
                            modifier = Modifier.height(26.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("Clear", fontSize = 11.sp) }
                    }
                    if (history.isEmpty()) {
                        Text("No saved responses yet. Only real, successful responses are saved.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    history.forEach { h ->
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "${h.model} - ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(h.createdAt))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.tertiary
                            )
                            Text("Q: ${h.prompt.take(140)}", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            Text(h.response.take(500), style = MaterialTheme.typography.bodyMedium)
                            Row {
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(h.response)); toast("Copied") },
                                    modifier = Modifier.size(28.dp)
                                ) { Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(15.dp)) }
                                IconButton(
                                    onClick = { vm.deleteResponse(h.id) },
                                    modifier = Modifier.size(28.dp)
                                ) { Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(15.dp)) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
