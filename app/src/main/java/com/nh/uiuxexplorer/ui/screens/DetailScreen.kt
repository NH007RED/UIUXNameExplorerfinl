package com.nh.uiuxexplorer.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.common.parseHex
import com.nh.uiuxexplorer.ui.common.shareText
import com.nh.uiuxexplorer.ui.preview.StylePreview
import com.nh.uiuxexplorer.ui.vm.AiUiState
import com.nh.uiuxexplorer.ui.vm.DetailViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onOpenDesign: (String) -> Unit,
    onOpenAiSettings: () -> Unit,
    onDesignScreen: (String) -> Unit,
    onPreviewScreen: (String) -> Unit = onDesignScreen,
    vm: DetailViewModel = hiltViewModel()
) {
    val entry by vm.entry.collectAsStateWithLifecycle()
    val fav by vm.isFavorite.collectAsStateWithLifecycle()
    val related by vm.related.collectAsStateWithLifecycle()
    val aiState by vm.aiState.collectAsStateWithLifecycle()
    val aiConfigured by vm.aiConfigured.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val e = entry

    fun withPromptWatermark(rawPrompt: String): String {
        val sig = WatermarkGuard.resolvePromptSignature()
        return if (sig.isNotBlank()) "$rawPrompt\n\n$sig" else rawPrompt
    }

    fun copy(text: String, what: String) {
        clipboard.setText(AnnotatedString(text))
        Toast.makeText(context, "$what copied", Toast.LENGTH_SHORT).show()
    }

    Screen(
        title = e?.name ?: "Design",
        onBack = onBack,
        actions = {
            if (e != null) {
                IconButton(onClick = { onPreviewScreen(e.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.OpenInFull, "Open full-screen Preview / Edit Layout", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = vm::toggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = {
                        shareText(
                            context,
                            e.name,
                            "${e.name} - ${e.description}\n\nSample prompt:\n${withPromptWatermark(e.prompt)}"
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp))
                }
            }
        }
    ) {
        if (e == null) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Screen
        }
        val cs = MaterialTheme.colorScheme
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPreviewScreen(e.id) }
            ) {
                StylePreview(e.preview, entry = e)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = cs.surface.copy(alpha = 0.88f),
                    tonalElevation = 2.dp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.OpenInFull, null, modifier = Modifier.size(12.dp), tint = cs.primary)
                        Text("Tap for Preview / Edit Page", style = MaterialTheme.typography.labelSmall, color = cs.onSurface)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onPreviewScreen(e.id) },
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Preview Layout", fontSize = 12.sp)
                }
                Button(
                    onClick = { onDesignScreen(e.id) },
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Edit Layout", fontSize = 12.sp)
                }
            }

            Text(
                if (e.preview == "concept") "Concept card: this is a principle or pattern, so there is no single visual style to render. Open Preview / Edit Layout to build a screen with its palette."
                else "Rendered with Jetpack Compose as an illustrative sample. Tap above to open the full-screen Preview / Edit Layout page with finger pinch-zoom.",
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant
            )

            Block("Overview") {
                Text(e.category, color = cs.primary, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(3.dp))
                Text(e.description, style = MaterialTheme.typography.bodyMedium)
                if (e.aliases.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Also known as", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(3.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        e.aliases.forEach {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(it, fontSize = 11.sp) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                }
            }

            Block("AI recognition terminology (curated library)") {
                Text(
                    "These are curated reference terms, not output from any AI model. Different models may use different words for the same design.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(5.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (listOf(e.name) + e.aliases).forEach {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(it, fontSize = 11.sp) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }

            Block("Live AI response") {
                Text(
                    "Asks your connected model how it describes this term. Shown separately from the curated data above; never simulated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                if (!aiConfigured) {
                    OutlinedButton(
                        onClick = onOpenAiSettings,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) { Text("Configure AI model", fontSize = 11.sp) }
                } else {
                    Button(
                        onClick = vm::askAi,
                        enabled = aiState !is AiUiState.Loading,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Cloud, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ask connected model", fontSize = 11.sp)
                    }
                }
                when (val s = aiState) {
                    AiUiState.Idle -> {}
                    AiUiState.Loading -> Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Waiting for the model...", style = MaterialTheme.typography.bodySmall)
                    }
                    is AiUiState.Error -> Text(s.message, color = cs.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    is AiUiState.Success -> Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "LIVE AI RESPONSE - ${s.model} - ${s.latencyMs} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.tertiary
                        )
                        Text(s.text, style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(
                            onClick = { copy(withPromptWatermark(s.text), "AI response") },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy response", fontSize = 11.sp)
                        }
                    }
                }
            }

            Block("Characteristics") {
                e.characteristics.forEachIndexed { i, c -> Text("${i + 1}. $c", style = MaterialTheme.typography.bodyMedium) }
            }

            Block("Color palette") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    e.palette.forEach { hex ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(parseHex(hex)))
                            Text("#${hex.uppercase()}", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(e.colors, style = MaterialTheme.typography.bodyMedium)
            }

            Block("Typography") {
                val fam = when {
                    e.typography.contains("mono", true) -> FontFamily.Monospace
                    e.typography.contains("serif", true) && !e.typography.contains("sans", true) -> FontFamily.Serif
                    else -> FontFamily.SansSerif
                }
                Text("Aa Bb Cc 123", fontFamily = fam, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(e.typography, style = MaterialTheme.typography.bodyMedium)
            }

            Block("UI components") { Text(e.components, style = MaterialTheme.typography.bodyMedium) }
            Block("Layout") { Text(e.layout, style = MaterialTheme.typography.bodyMedium) }
            Block("Animation") { Text(e.animation, style = MaterialTheme.typography.bodyMedium) }
            Block("Best use cases") { e.useCases.forEach { Text("- $it", style = MaterialTheme.typography.bodyMedium) } }
            Block("Advantages") { e.advantages.forEach { Text("+ $it", style = MaterialTheme.typography.bodyMedium) } }
            Block("Disadvantages") { e.disadvantages.forEach { Text("- $it", style = MaterialTheme.typography.bodyMedium) } }
            Block("Example UI") { Text(e.example, style = MaterialTheme.typography.bodyMedium) }

            if (related.isNotEmpty()) {
                Block("Related designs") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        related.forEach { r ->
                            AssistChip(
                                onClick = { onOpenDesign(r.id) },
                                label = { Text(r.name, fontSize = 11.sp) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onDesignScreen(e.id) },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Design a screen with this style", fontSize = 12.sp)
            }

            Block("Sample design prompt") {
                val watermarkedPrompt = withPromptWatermark(e.prompt)
                Text(watermarkedPrompt, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { copy(watermarkedPrompt, "Prompt") },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy design prompt", fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun Block(title: String, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(11.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}
