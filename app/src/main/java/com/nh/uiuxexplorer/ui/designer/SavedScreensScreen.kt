package com.nh.uiuxexplorer.ui.designer

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.designer.PromptBuilder
import com.nh.uiuxexplorer.domain.model.DesignerMode
import com.nh.uiuxexplorer.domain.model.SavedScreen
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.vm.ScreensViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavedScreensScreen(
    onBack: () -> Unit,
    onNew: () -> Unit,
    onOpen: (Long) -> Unit,
    onOpenPreviewPage: (Long, DesignerMode) -> Unit = { id, _ -> onOpen(id) },
    vm: ScreensViewModel = hiltViewModel()
) {
    val saved by vm.saved.collectAsStateWithLifecycle()
    val designs by vm.designs.collectAsStateWithLifecycle()
    val frame by vm.frame.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var toDelete by remember { mutableStateOf<SavedScreen?>(null) }
    var toExportPng by remember { mutableStateOf<SavedScreen?>(null) }
    val cs = MaterialTheme.colorScheme
    val compactBtnPad = PaddingValues(horizontal = 8.dp, vertical = 2.dp)

    Screen("My Screens", onBack = onBack) {
        Button(
            onClick = {
                vm.clearActiveDraft()
                onNew()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(36.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Create new screen", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        if (saved.isEmpty()) {
            Text(
                "No saved screens yet. Create one, choose a UI style, arrange elements with your fingers, then save it as a screen or high-quality PNG.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(saved, key = { it.id }) { s ->
                val entry = designs.firstOrNull { it.id == s.styleId }
                val theme = remember(entry) { styleThemeFor(entry) }
                GlassCard(
                    Modifier.fillMaxWidth(),
                    onClick = {
                        vm.clearActiveDraft()
                        onOpenPreviewPage(s.id, DesignerMode.EDIT)
                    }
                ) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PhoneFrame(62.dp, frame, theme) { corner, overlay ->
                            ScreenCanvas(s.elements, theme, 62.dp, cornerDp = corner, overlay = overlay)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                s.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                entry?.name ?: "Style removed",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.primary
                            )
                            Text(
                                "${s.elements.size} elements · " +
                                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(s.updatedAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        vm.clearActiveDraft()
                                        onOpenPreviewPage(s.id, DesignerMode.PREVIEW)
                                    },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = compactBtnPad
                                ) { Text("Preview", fontSize = 10.sp) }
                                FilledTonalButton(
                                    onClick = {
                                        vm.clearActiveDraft()
                                        onOpenPreviewPage(s.id, DesignerMode.EDIT)
                                    },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = compactBtnPad
                                ) { Text("Edit", fontSize = 10.sp) }
                                FilledTonalButton(
                                    onClick = { toExportPng = s },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = compactBtnPad
                                ) { Text("Save PNG", fontSize = 10.sp) }
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(PromptBuilder.build(s.name, entry, s.elements)))
                                        Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = compactBtnPad
                                ) { Text("Prompt", fontSize = 10.sp) }
                                OutlinedButton(
                                    onClick = { vm.duplicate(s.id) },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = compactBtnPad
                                ) { Text("Clone", fontSize = 10.sp) }
                                TextButton(
                                    onClick = { toDelete = s },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = compactBtnPad
                                ) { Text("Delete", fontSize = 10.sp, color = cs.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    toExportPng?.let { s ->
        val entry = designs.firstOrNull { it.id == s.styleId }
        val theme = remember(entry) { styleThemeFor(entry) }
        ExportPngDialog(
            screenName = s.name,
            elements = s.elements,
            theme = theme,
            frameStyle = frame,
            onVerifyLicenseKey = vm::verifyAndActivateLicenseKey,
            onClearLicenseKey = vm::clearActivatedLicenseKey,
            onDismiss = { toExportPng = null }
        )
    }

    toDelete?.let { s ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete screen?") },
            text = { Text("\"${s.name}\" will be removed from this device.") },
            confirmButton = { TextButton(onClick = { vm.delete(s.id); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}
