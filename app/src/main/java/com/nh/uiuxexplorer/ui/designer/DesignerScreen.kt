package com.nh.uiuxexplorer.ui.designer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.designer.PromptBuilder
import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.DesignerMode
import com.nh.uiuxexplorer.domain.model.ElementAlignment
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.model.FrameStyle
import com.nh.uiuxexplorer.domain.model.SortMode
import com.nh.uiuxexplorer.domain.search.SearchEngine
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import com.nh.uiuxexplorer.ui.common.AppBackground
import com.nh.uiuxexplorer.ui.common.PaletteBadge
import com.nh.uiuxexplorer.ui.common.shareText
import com.nh.uiuxexplorer.ui.vm.AiUiState
import com.nh.uiuxexplorer.ui.vm.DesignerUi
import com.nh.uiuxexplorer.ui.vm.DesignerViewModel
import kotlinx.coroutines.launch

internal enum class PanelMode(val label: String) {
    IMAGE_SOURCE("Gallery Photo"),
    SIZE("Size (Chhota/Bada)"),
    POSITION("Adjust Position"),
    COLOR("Color"),
    TEXT("Text"),
    FONT("Text Size"),
    NAV_ICONS("Nav Icons"),
    ICON("Icon"),
    ANGLE("Rotate")
}

internal sealed interface IconPickRequest {
    data object AddNewIconElement : IconPickRequest
    data object ChangeElementIcon : IconPickRequest
    data class ChangeNavSlotIcon(val slotIndex: Int) : IconPickRequest
}

@Composable
internal fun SmoothOverlayVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    slideFromBottom: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (slideFromBottom) slideInVertically(tween(220)) { it } + fadeIn(tween(180)) else fadeIn(tween(180)),
        exit = if (slideFromBottom) slideOutVertically(tween(200)) { it } + fadeOut(tween(150)) else fadeOut(tween(150))
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignerScreen(
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onOpenPreviewEditPage: (DesignerMode) -> Unit,
    vm: DesignerViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val style by vm.style.collectAsStateWithLifecycle()
    val designs by vm.allDesigns.collectAsStateWithLifecycle()
    val aiState by vm.aiState.collectAsStateWithLifecycle()
    val aiConfigured by vm.aiConfigured.collectAsStateWithLifecycle()
    val frame by vm.frame.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0
    val theme = remember(style) { styleThemeFor(style) }

    var showStyles by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }
    var showExplainSheet by remember { mutableStateOf(false) }
    var showPngExport by remember { mutableStateOf(false) }
    var showName by remember { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }
    var showFrameMenu by remember { mutableStateOf(false) }
    var iconRequest by remember { mutableStateOf<IconPickRequest?>(null) }
    var pendingSave by remember { mutableStateOf(false) }
    var exitAfterSave by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val savedPath = ImageStorage.importFromGallery(context, uri)
                if (savedPath != null) {
                    vm.setImageUri(savedPath)
                    Toast.makeText(context, "Gallery image applied!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    LaunchedEffect(ui.dirty, exitAfterSave) {
        if (exitAfterSave && !ui.dirty) onBack()
    }

    fun requestSave() {
        if (ui.screenId == null) { pendingSave = true; showName = true } else vm.save()
    }

    fun goBack() {
        if (ui.dirty) showDiscard = true else onBack()
    }

    fun openFullPage(mode: DesignerMode = ui.mode) {
        vm.prepareSessionForNewPage()
        onOpenPreviewEditPage(mode)
    }

    BackHandler(enabled = ui.dirty) { showDiscard = true }

    DesignerLayoutContent(
        ui = ui,
        style = style,
        frame = frame,
        theme = theme,
        isKeyboardOpen = isKeyboardOpen,
        showFrameMenu = showFrameMenu,
        onShowFrameMenuChange = { showFrameMenu = it },
        onBack = ::goBack,
        onShowStyles = { showStyles = true },
        onShowExplain = { showExplainSheet = true },
        onShowPrompt = { showPrompt = true },
        onShowPngExport = { showPngExport = true },
        onShowRename = { pendingSave = false; showName = true },
        onSave = ::requestSave,
        onRequestIcon = { iconRequest = it },
        onPickGalleryImage = { galleryLauncher.launch("image/*") },
        onOpenFullPreview = { openFullPage(DesignerMode.PREVIEW) },
        vm = vm
    )

    iconRequest?.let { req ->
        IconPickerSheet(
            onPick = { key ->
                when (req) {
                    IconPickRequest.AddNewIconElement -> vm.addIcon(key)
                    IconPickRequest.ChangeElementIcon -> vm.setIcon(key)
                    is IconPickRequest.ChangeNavSlotIcon -> vm.setNavIconAt(req.slotIndex, key)
                }
                iconRequest = null
            },
            onDismiss = { iconRequest = null }
        )
    }

    if (showStyles) {
        StylePickerSheet(
            designs = designs,
            currentId = ui.styleId,
            onPick = { vm.setStyle(it.id); showStyles = false },
            onDismiss = { showStyles = false }
        )
    }

    if (showExplainSheet) {
        ExplainLayoutSheet(
            name = ui.name,
            style = style,
            elements = ui.elements,
            layoutNote = ui.layoutNote,
            onUpdateNote = vm::setLayoutNote,
            onAddExplainBoxOnScreen = { text ->
                vm.addExplainTextOnScreen(text)
                showExplainSheet = false
            },
            onDismiss = { showExplainSheet = false }
        )
    }

    if (showPngExport) {
        ExportPngDialog(
            screenName = ui.name,
            elements = ui.elements,
            theme = theme,
            frameStyle = frame,
            onVerifyLicenseKey = vm::verifyAndActivateLicenseKey,
            onClearLicenseKey = vm::clearActivatedLicenseKey,
            onDismiss = { showPngExport = false }
        )
    }

    if (showPrompt) {
        PromptSheet(
            name = ui.name,
            style = style,
            elements = ui.elements,
            customExplanation = ui.layoutNote,
            aiState = aiState,
            aiConfigured = aiConfigured,
            onRefine = vm::refinePrompt,
            onOpenAiSettings = { showPrompt = false; onOpenAiSettings() },
            onDismiss = { showPrompt = false; vm.clearAi() }
        )
    }

    if (showName) {
        RenameScreenDialog(
            initialName = ui.name,
            pendingSave = pendingSave,
            onConfirm = { newName ->
                vm.setName(newName)
                if (pendingSave) vm.save(newName)
                showName = false
                pendingSave = false
            },
            onDismiss = {
                showName = false
                pendingSave = false
                exitAfterSave = false
            }
        )
    }

    if (showDiscard) {
        DiscardChangesDialog(
            onSave = { showDiscard = false; exitAfterSave = true; requestSave() },
            onKeepEditing = { showDiscard = false },
            onDiscard = { showDiscard = false; onBack() }
        )
    }
}

@Composable
internal fun DesignerLayoutContent(
    ui: DesignerUi,
    style: DesignEntry?,
    frame: FrameStyle,
    theme: StyleTheme,
    isKeyboardOpen: Boolean,
    showFrameMenu: Boolean,
    onShowFrameMenuChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onShowStyles: () -> Unit,
    onShowExplain: () -> Unit,
    onShowPrompt: () -> Unit,
    onShowPngExport: () -> Unit,
    onShowRename: () -> Unit,
    onSave: () -> Unit,
    onRequestIcon: (IconPickRequest) -> Unit,
    onPickGalleryImage: () -> Unit,
    onOpenFullPreview: (() -> Unit)?,
    vm: DesignerViewModel
) {
    val cs = MaterialTheme.colorScheme
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    AppBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = topInset)
                .imePadding()
        ) {
            // 1. Ultra-slim single top header bar (38.dp) so mobile phone screen starts right at the TOP!
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = if (ui.dirty) "${ui.name} *" else ui.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onShowRename() }
                        .padding(horizontal = 4.dp)
                )
                ModeTogglePill(
                    mode = ui.mode,
                    onModeChange = vm::setMode
                )
                IconButton(onClick = vm::toggleLayoutLock, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (ui.layoutLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (ui.layoutLocked) "Unlock layout" else "Lock layout",
                        tint = if (ui.layoutLocked) Color(0xFFFFB300) else cs.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = vm::undo, enabled = ui.canUndo, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Undo, "Undo", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onShowPngExport, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Download, "Save High-Quality PNG", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onShowPrompt, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.AutoAwesome, "Prompt", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onSave, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Save, "Save", modifier = Modifier.size(16.dp))
                }
            }

            // 2. Mobile Phone Screen right at the TOP ("upar mein") in the foreground!
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                InteractivePhoneViewport(
                    elements = ui.elements,
                    theme = theme,
                    frame = frame,
                    selectedId = ui.selectedId,
                    guideV = ui.guideV,
                    guideH = ui.guideH,
                    interactive = ui.mode == DesignerMode.EDIT,
                    layoutLocked = ui.layoutLocked,
                    hideHandles = isKeyboardOpen,
                    loading = ui.loading,
                    resetZoomTick = ui.resetZoomTick,
                    modifier = Modifier.fillMaxSize(),
                    onGestureStart = vm::beginGesture,
                    onCancelGesture = vm::cancelGesture,
                    onTransform = vm::transform,
                    onCornerResize = vm::resizeCorner,
                    onRotateTo = vm::rotateTo,
                    onGestureEnd = vm::endGesture,
                    onBackgroundTap = { vm.select(null) },
                    onToggleLayoutLock = vm::toggleLayoutLock
                )
            }

            // 3. Slim quick-settings chip bar BELOW the phone (hidden when keyboard is open)
            if (!isKeyboardOpen) {
                val sel = ui.elements.firstOrNull { it.id == ui.selectedId }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sel?.type == ElementType.IMAGE) {
                        item {
                            AssistChip(
                                onClick = onPickGalleryImage,
                                label = { Text("Upload Photo", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(13.dp)) },
                                modifier = Modifier.height(25.dp)
                            )
                        }
                    }
                    item {
                        AssistChip(
                            onClick = onShowPngExport,
                            label = { Text("Save PNG", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = { Icon(Icons.Default.Download, null, modifier = Modifier.size(13.dp)) },
                            modifier = Modifier.height(25.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = ui.layoutLocked,
                            onClick = vm::toggleLayoutLock,
                            label = { Text(if (ui.layoutLocked) "Locked" else "Lock Layout", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(
                                    if (ui.layoutLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            modifier = Modifier.height(25.dp)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = onShowStyles,
                            label = { Text(style?.name ?: "Style", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Default.Palette, null, modifier = Modifier.size(13.dp)) },
                            modifier = Modifier.height(25.dp)
                        )
                    }
                    item {
                        Box {
                            AssistChip(
                                onClick = { onShowFrameMenuChange(true) },
                                label = { Text(frame.label, fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(13.dp)) },
                                modifier = Modifier.height(25.dp)
                            )
                            DropdownMenu(expanded = showFrameMenu, onDismissRequest = { onShowFrameMenuChange(false) }) {
                                FrameStyle.entries.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f.label, fontSize = 12.sp) },
                                        onClick = { vm.setFrame(f); onShowFrameMenuChange(false) }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        AssistChip(
                            onClick = onShowExplain,
                            label = { Text("Explain Layout", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null, modifier = Modifier.size(13.dp)) },
                            modifier = Modifier.height(25.dp)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = onShowRename,
                            label = { Text("Rename", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp)) },
                            modifier = Modifier.height(25.dp)
                        )
                    }
                    item {
                        AssistChip(
                            onClick = vm::resetStarterLayout,
                            label = { Text("Reset", fontSize = 10.sp) },
                            leadingIcon = { Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(13.dp)) },
                            modifier = Modifier.height(25.dp)
                        )
                    }
                }
            }

            // 4. Slim Persistent Bottom Dock (NEVER unmounts SelectionInspectorContent when keyboard opens!)
            DesignerBottomDock(
                ui = ui,
                theme = theme,
                isKeyboardOpen = isKeyboardOpen,
                vm = vm,
                onRequestIcon = onRequestIcon,
                onPickGalleryImage = onPickGalleryImage,
                onOpenExplain = onShowExplain,
                onOpenPngExport = onShowPngExport,
                onSave = onSave,
                onOpenFullPreview = onOpenFullPreview
            )
        }
    }
}

@Composable
internal fun DesignerBottomDock(
    ui: DesignerUi,
    theme: StyleTheme,
    isKeyboardOpen: Boolean,
    vm: DesignerViewModel,
    onRequestIcon: (IconPickRequest) -> Unit,
    onPickGalleryImage: () -> Unit,
    onOpenExplain: () -> Unit,
    onOpenPngExport: () -> Unit,
    onSave: () -> Unit,
    onOpenFullPreview: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    val sel = ui.elements.firstOrNull { it.id == ui.selectedId }

    if (ui.mode == DesignerMode.PREVIEW) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            shape = RoundedCornerShape(14.dp),
            color = cs.surfaceVariant.copy(alpha = 0.65f)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Preview Mode · Pinch to zoom",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    FilledTonalButton(
                        onClick = onOpenPngExport,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Save PNG", fontSize = 10.sp)
                    }
                    FilledTonalButton(
                        onClick = { vm.setMode(DesignerMode.EDIT) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Edit", fontSize = 10.sp)
                    }
                    Button(
                        onClick = onSave,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Save", fontSize = 10.sp)
                    }
                }
            }
        }
        return
    }

    val dockHeight = if (isKeyboardOpen) 132.dp else 168.dp
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(dockHeight)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .border(
                width = if (isKeyboardOpen) 1.4.dp else 0.8.dp,
                color = if (isKeyboardOpen) cs.primary else cs.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = cs.surface.copy(alpha = 0.98f),
        tonalElevation = 5.dp
    ) {
        if (sel != null) {
            SelectionInspectorContent(
                sel = sel,
                theme = theme,
                layoutLocked = ui.layoutLocked,
                isKeyboardOpen = isKeyboardOpen,
                vm = vm,
                onRequestIcon = onRequestIcon,
                onPickGalleryImage = onPickGalleryImage
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (ui.layoutLocked) "🔒 Layout Locked — Unlock to move/add elements:"
                            else "1. Tap element on phone or below to edit / lock:",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (ui.layoutLocked) Color(0xFFE65100) else cs.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (onOpenFullPreview != null) {
                            Text(
                                "Full Page >",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.tertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp).clickable { onOpenFullPreview() }
                            )
                        }
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        contentPadding = PaddingValues(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ui.elements.isNotEmpty()) {
                            val allLocked = ui.elements.all { it.locked }
                            item {
                                AssistChip(
                                    onClick = { if (allLocked) vm.unlockAllElements() else vm.lockAllElements() },
                                    label = { Text(if (allLocked) "Unlock All" else "Lock All", fontSize = 10.sp) },
                                    leadingIcon = {
                                        Icon(
                                            if (allLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    },
                                    modifier = Modifier.height(25.dp)
                                )
                            }
                        }
                        itemsIndexed(ui.elements, key = { _, item -> item.id }) { index, item ->
                            val label = item.text.takeIf { it.isNotBlank() }?.take(12) ?: item.type.label
                            FilterChip(
                                selected = item.locked,
                                onClick = { vm.select(item.id) },
                                label = { Text("${index + 1}. $label", fontSize = 10.sp) },
                                leadingIcon = {
                                    Icon(
                                        if (item.locked) Icons.Default.Lock else Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                modifier = Modifier.height(25.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = cs.onSurface.copy(alpha = 0.08f))

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        "2. Add new element to layout (Image uploads from Gallery):",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        contentPadding = PaddingValues(vertical = 1.dp)
                    ) {
                        items(ElementType.entries) { t ->
                            AssistChip(
                                onClick = {
                                    if (t == ElementType.ICON) {
                                        onRequestIcon(IconPickRequest.AddNewIconElement)
                                    } else {
                                        vm.addElement(t)
                                    }
                                },
                                enabled = !ui.layoutLocked,
                                label = { Text(t.label, fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp)) },
                                modifier = Modifier.height(25.dp)
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onOpenPngExport,
                        modifier = Modifier.weight(1f).height(29.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Save PNG", fontSize = 10.sp, maxLines = 1)
                    }
                    FilledTonalButton(
                        onClick = { vm.setMode(DesignerMode.PREVIEW) },
                        modifier = Modifier.weight(0.9f).height(29.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Preview", fontSize = 10.sp, maxLines = 1)
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f).height(29.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Save Layout", fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectionInspectorContent(
    sel: DesignElement,
    theme: StyleTheme,
    layoutLocked: Boolean,
    isKeyboardOpen: Boolean,
    vm: DesignerViewModel,
    onRequestIcon: (IconPickRequest) -> Unit,
    onPickGalleryImage: () -> Unit
) {
    val modes = remember(sel.type) {
        buildList {
            if (sel.type == ElementType.IMAGE) {
                add(PanelMode.IMAGE_SOURCE)
            }
            if (sel.type == ElementType.BOTTOM_NAV || sel.type == ElementType.TOP_BAR) {
                add(PanelMode.NAV_ICONS)
            }
            if (sel.type.hasText) {
                add(PanelMode.TEXT)
                add(PanelMode.COLOR)
                add(PanelMode.FONT)
            } else {
                add(PanelMode.COLOR)
            }
            if (sel.type == ElementType.ICON || sel.type == ElementType.EXPLAIN_TEXT ||
                sel.type == ElementType.INPUT || sel.type == ElementType.FAB || sel.type == ElementType.IMAGE
            ) {
                add(PanelMode.ICON)
            }
            add(PanelMode.SIZE)
            add(PanelMode.POSITION)
            add(PanelMode.ANGLE)
        }
    }
    var mode by remember(sel.id) { mutableStateOf(modes.first()) }
    var localText by remember(sel.id) { mutableStateOf(sel.text) }
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val isLocked = sel.locked || layoutLocked

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = (if (isLocked) "🔒 " else "") + sel.type.label +
                    if (isKeyboardOpen && sel.type.hasText) ": \"${localText.ifBlank { "..." }}\"" else "",
                style = MaterialTheme.typography.titleSmall,
                color = if (isLocked) Color(0xFFE65100) else cs.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = if (sel.locked) Color(0xFFFFB300).copy(alpha = 0.25f) else cs.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { vm.toggleSelectedElementLock() }
            ) {
                Row(
                    Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        if (sel.locked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (sel.locked) "Unlock element" else "Lock element",
                        tint = if (sel.locked) Color(0xFFE65100) else cs.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        if (sel.locked) "Unlock" else "Lock",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (sel.locked) Color(0xFFE65100) else cs.onSurfaceVariant
                    )
                }
            }
            if (!isKeyboardOpen) {
                IconButton(onClick = vm::duplicateSelected, enabled = !layoutLocked, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.FileCopy, "Duplicate", modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = vm::bringToFront, enabled = !isLocked, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.FlipToFront, "Bring to front", modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = vm::sendToBack, enabled = !isLocked, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.FlipToBack, "Send to back", modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = vm::deleteSelected, enabled = !isLocked, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = if (isLocked) cs.onSurfaceVariant else cs.error, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(3.dp))
            Button(
                onClick = {
                    if (isKeyboardOpen) {
                        focusManager.clearFocus()
                    } else {
                        vm.finishElementEdit()
                    }
                },
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 1.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(if (isKeyboardOpen) "OK" else "Done", fontSize = 10.sp)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(modes) { m ->
                FilterChip(
                    selected = mode == m,
                    onClick = { mode = m },
                    label = { Text(m.label, fontSize = 10.sp) },
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            val locksPositionOrSize = isLocked && (mode == PanelMode.SIZE || mode == PanelMode.POSITION || mode == PanelMode.ANGLE)
            if (locksPositionOrSize) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFB300).copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (layoutLocked) "🔒 Whole Layout is Locked" else "🔒 ${sel.type.label} is Locked",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Position, size, and rotation are protected from accidental touch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (layoutLocked) vm.toggleLayoutLock() else vm.toggleSelectedElementLock()
                            },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Unlock", fontSize = 10.sp)
                        }
                    }
                }
            } else {
                when (mode) {
                    PanelMode.IMAGE_SOURCE -> {
                        val hasCustomImage = sel.imageUri.isNotBlank()
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = onPickGalleryImage,
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(if (hasCustomImage) "Change Gallery Image" else "Upload Photo from Gallery", fontSize = 11.sp)
                                }
                                if (hasCustomImage) {
                                    OutlinedButton(
                                        onClick = { vm.setImageUri("") },
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("Remove", fontSize = 10.sp)
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Image Fit:", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                                FilterChip(
                                    selected = sel.imageCrop,
                                    onClick = { vm.setImageCrop(true) },
                                    label = { Text("Crop (Fill Box)", fontSize = 10.sp) },
                                    modifier = Modifier.height(24.dp)
                                )
                                FilterChip(
                                    selected = !sel.imageCrop,
                                    onClick = { vm.setImageCrop(false) },
                                    label = { Text("Fit (Full Image)", fontSize = 10.sp) },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                    PanelMode.NAV_ICONS -> {
                        if (sel.type == ElementType.BOTTOM_NAV) {
                            val navKeys = DesignerDefaults.bottomNavIcons(sel)
                            Column(
                                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Tap any tab icon below to change (${navKeys.size} tabs):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        AssistChip(
                                            onClick = { vm.removeBottomNavSlot() },
                                            enabled = navKeys.size > 2,
                                            label = { Text("- Tab", fontSize = 10.sp) },
                                            modifier = Modifier.height(24.dp)
                                        )
                                        AssistChip(
                                            onClick = { vm.addBottomNavSlot() },
                                            enabled = navKeys.size < 5,
                                            label = { Text("+ Tab", fontSize = 10.sp) },
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    itemsIndexed(navKeys) { idx, key ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = cs.surfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .border(0.8.dp, cs.primary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                                .clickable { onRequestIcon(IconPickRequest.ChangeNavSlotIcon(idx)) }
                                        ) {
                                            Row(
                                                Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    IconCatalog.get(key),
                                                    contentDescription = key,
                                                    tint = cs.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Column {
                                                    Text("Tab ${idx + 1}", fontSize = 9.sp, color = cs.onSurfaceVariant)
                                                    Text(
                                                        IconCatalog.label(key),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val (leftKey, rightKey) = DesignerDefaults.topBarIcons(sel)
                            Row(
                                Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { onRequestIcon(IconPickRequest.ChangeNavSlotIcon(0)) },
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(IconCatalog.get(leftKey), null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Left: ${IconCatalog.label(leftKey)}", fontSize = 10.sp, maxLines = 1)
                                }
                                FilledTonalButton(
                                    onClick = { onRequestIcon(IconPickRequest.ChangeNavSlotIcon(1)) },
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(IconCatalog.get(rightKey), null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Right: ${IconCatalog.label(rightKey)}", fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                    PanelMode.COLOR -> {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Choose color for ${sel.type.label} (Text / Icon / Accent):",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(DESIGNER_COLOR_SWATCHES, key = { it.key }) { swatch ->
                                    val isSelected = (sel.colorKey == swatch.key) ||
                                        (swatch.key == "default" && (sel.colorKey.isBlank() || (sel.type != ElementType.ICON && sel.colorKey == "accent"))) ||
                                        (swatch.key == "theme_accent" && sel.type == ElementType.ICON && sel.colorKey == "accent")
                                    val dotColor = swatch.previewColor ?: when (swatch.key) {
                                        "theme_accent" -> theme.iconAccent
                                        "text" -> theme.onSurface
                                        "muted" -> theme.muted
                                        else -> theme.onSurface
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { vm.setColorKey(swatch.key) },
                                        leadingIcon = {
                                            Box(
                                                Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                                    .border(1.dp, cs.onSurface.copy(alpha = 0.3f), CircleShape)
                                            )
                                        },
                                        label = { Text(swatch.label, fontSize = 10.sp) },
                                        modifier = Modifier.height(25.dp)
                                    )
                                }
                            }
                        }
                    }
                    PanelMode.SIZE -> {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.scaleSelected(0.90f) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Chhota (-)", fontSize = 10.sp)
                                    }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.scaleSelected(1.10f) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Bada (+)", fontSize = 10.sp)
                                    }
                                }
                                item {
                                    AssistChip(
                                        onClick = { vm.applySizePreset("slim") },
                                        label = { Text("Slim", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = { vm.applySizePreset("small") },
                                        label = { Text("Small", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = { vm.applySizePreset("medium") },
                                        label = { Text("Medium", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = { vm.applySizePreset("large") },
                                        label = { Text("Large", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                item {
                                    AssistChip(
                                        onClick = { vm.applySizePreset("full") },
                                        label = { Text("Full Width", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            StepSliderRow(
                                label = "W",
                                value = sel.w,
                                range = DesignerDefaults.MIN_W..360f,
                                fineStep = 1f,
                                fastStep = 10f,
                                onChange = vm::setWidth
                            )
                            StepSliderRow(
                                label = "H",
                                value = sel.h,
                                range = DesignerDefaults.MIN_H..600f,
                                fineStep = 1f,
                                fastStep = 10f,
                                onChange = vm::setHeight
                            )
                        }
                    }
                    PanelMode.POSITION -> {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(-1f, 0f) },
                                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("←1", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(1f, 0f) },
                                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("→1", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(0f, -1f) },
                                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("↑1", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(0f, 1f) },
                                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("↓1", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(-8f, 0f) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("⇐8", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(8f, 0f) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("⇒8", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(0f, -8f) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("⇑8", fontSize = 10.sp) }
                                }
                                item {
                                    FilledTonalButton(
                                        onClick = { vm.nudgeSelected(0f, 8f) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) { Text("⇓8", fontSize = 10.sp) }
                                }
                                items(ElementAlignment.entries) { align ->
                                    AssistChip(
                                        onClick = { vm.alignSelected(align) },
                                        label = { Text(align.label, fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            StepSliderRow(
                                label = "X",
                                value = sel.x,
                                range = 0f..340f,
                                fineStep = 1f,
                                fastStep = 8f,
                                onChange = vm::setX
                            )
                            StepSliderRow(
                                label = "Y",
                                value = sel.y,
                                range = 0f..740f,
                                fineStep = 1f,
                                fastStep = 8f,
                                onChange = vm::setY
                            )
                        }
                    }
                    PanelMode.TEXT -> {
                        val isMultiLine = sel.type == ElementType.PARAGRAPH || sel.type == ElementType.EXPLAIN_TEXT
                        OutlinedTextField(
                            value = localText,
                            onValueChange = {
                                localText = it
                                vm.setText(it)
                            },
                            singleLine = !isMultiLine,
                            maxLines = if (isMultiLine) 2 else 1,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = if (isMultiLine) ImeAction.Default else ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            trailingIcon = {
                                if (localText.isNotEmpty()) {
                                    IconButton(onClick = { localText = ""; vm.setText("") }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear text", modifier = Modifier.size(15.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    if (sel.type == ElementType.EXPLAIN_TEXT) "Explain layout text / note" else "Element text",
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                    PanelMode.FONT -> {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            StepSliderRow(
                                label = "Font",
                                value = sel.fontSize,
                                range = 8f..64f,
                                fineStep = 1f,
                                fastStep = 4f,
                                onChange = vm::setFontSize
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                items(listOf(10f, 12f, 14f, 16f, 20f, 26f, 34f, 44f)) { fs ->
                                    AssistChip(
                                        onClick = { vm.setFontSize(fs) },
                                        label = { Text("${fs.toInt()} sp", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    PanelMode.ICON -> Row(
                        Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val currentKey = sel.icon.ifBlank {
                            when (sel.type) {
                                ElementType.EXPLAIN_TEXT -> "info"
                                ElementType.INPUT -> "search"
                                ElementType.FAB -> "add"
                                ElementType.IMAGE -> "image"
                                else -> "star"
                            }
                        }
                        Icon(IconCatalog.get(currentKey), null, modifier = Modifier.size(24.dp))
                        Text(IconCatalog.label(currentKey), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1)
                        if (sel.type == ElementType.IMAGE) {
                            FilledTonalButton(
                                onClick = onPickGalleryImage,
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("Gallery", fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = { onRequestIcon(IconPickRequest.ChangeElementIcon) },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                        ) {
                            Text("Change icon", fontSize = 10.sp)
                        }
                    }
                    PanelMode.ANGLE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                items(listOf(-90f, -45f, 0f, 45f, 90f, 180f)) { deg ->
                                    AssistChip(
                                        onClick = { vm.setRotation(deg) },
                                        label = { Text("${deg.toInt()}°", fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                            StepSliderRow(
                                label = "Deg",
                                value = sel.rotation.coerceIn(-180f, 180f),
                                range = -180f..180f,
                                fineStep = 1f,
                                fastStep = 15f,
                                onChange = vm::setRotation
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExportPngDialog(
    screenName: String,
    elements: List<DesignElement>,
    theme: StyleTheme,
    frameStyle: FrameStyle,
    onVerifyLicenseKey: (suspend (String) -> Boolean)? = null,
    onClearLicenseKey: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val wmConfig by WatermarkGuard.config.collectAsStateWithLifecycle()
    val isLicenseActive = wmConfig.isLicenseValid

    var includeFrame by remember { mutableStateOf(frameStyle != FrameStyle.NONE) }
    var exporting by remember { mutableStateOf(false) }
    var licenseInput by remember { mutableStateOf("") }
    var verifyingKey by remember { mutableStateOf(false) }
    var keyStatusMsg by remember { mutableStateOf<String?>(null) }
    var keyStatusOk by remember { mutableStateOf(false) }

    fun openAdminTelegram() {
        val rawUrl = wmConfig.developerTelegram.trim().ifBlank { "https://t.me/htmlmovievg" }
        val fullUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) rawUrl else "https://$rawUrl"
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            Toast.makeText(context, "Telegram link: $fullUrl", Toast.LENGTH_LONG).show()
        }
    }

    fun activateKey() {
        val clean = licenseInput.trim().uppercase()
        if (!clean.startsWith("NH") || clean.length < 4) {
            keyStatusOk = false
            keyStatusMsg = "License key must start with 'NH...' (e.g. NH-8F4K-29XM)"
            return
        }
        if (verifyingKey) return
        verifyingKey = true
        keyStatusMsg = null
        scope.launch {
            val valid = if (onVerifyLicenseKey != null) {
                onVerifyLicenseKey(clean)
            } else {
                wmConfig.licenseKeys.any { it.key.equals(clean, ignoreCase = true) }
            }
            verifyingKey = false
            if (valid) {
                WatermarkGuard.updateConfig(wmConfig.copy(activatedLicenseKey = clean))
                keyStatusOk = true
                keyStatusMsg = "✓ License Key Activated! Watermark Removed."
                Toast.makeText(context, "License Key Activated! Watermark removed.", Toast.LENGTH_SHORT).show()
            } else {
                keyStatusOk = false
                keyStatusMsg = "Invalid Key. Contact Admin on Telegram to get a working NH... key."
            }
        }
    }

    fun export(share: Boolean) {
        if (exporting) return
        exporting = true
        scope.launch {
            val result = PngExporter.saveToGallery(
                context = context,
                screenName = screenName,
                elements = elements,
                theme = theme,
                frameStyle = frameStyle,
                includePhoneFrame = includeFrame,
                shareAfterSave = share
            )
            exporting = false
            result.onSuccess {
                val wmNotice = if (WatermarkGuard.config.value.isLicenseValid) "Without Watermark" else "With Watermark"
                Toast.makeText(
                    context,
                    "High-quality PNG ($wmNotice) saved to Gallery!",
                    Toast.LENGTH_LONG
                ).show()
                onDismiss()
            }.onFailure { err ->
                Toast.makeText(
                    context,
                    "Failed to export PNG: ${err.message ?: "unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!exporting) onDismiss() },
        title = { Text("Save Complete UI as High-Quality PNG", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    Modifier.padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val previewFrame = if (includeFrame) frameStyle else FrameStyle.NONE
                    PhoneFrame(88.dp, previewFrame, theme) { corner, statusOverlay ->
                        ScreenCanvas(
                            elements = elements,
                            theme = theme,
                            widthDp = 88.dp,
                            cornerDp = corner,
                            overlay = {
                                statusOverlay()
                                if (!isLicenseActive) {
                                    Canvas(Modifier.fillMaxSize()) {
                                        drawIntoCanvas { c ->
                                            PngExporter.drawTiledMicroWatermark(
                                                canvas = c.nativeCanvas,
                                                widthPx = size.width,
                                                heightPx = size.height,
                                                lightContent = theme.lightContent,
                                                scaleFactor = size.width / DesignerDefaults.W
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                Text(
                    if (isLicenseActive) {
                        "Ultra-HD Resolution: 1440 × 3120 px (4x Lossless PNG)\n✓ License Key Active (${wmConfig.activatedLicenseKey}) · Exports WITHOUT Watermark."
                    } else {
                        "Ultra-HD Resolution: 1440 × 3120 px (4x Lossless PNG)\nIncludes transparent micro-watermark · Saves to Gallery (Pictures/UIUXExplorer)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLicenseActive) Color(0xFF00E676) else cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = includeFrame,
                        onClick = { includeFrame = true },
                        label = { Text("With Phone Frame", fontSize = 11.sp) },
                        enabled = frameStyle != FrameStyle.NONE,
                        modifier = Modifier.height(27.dp)
                    )
                    FilterChip(
                        selected = !includeFrame,
                        onClick = { includeFrame = false },
                        label = { Text("Screen Only", fontSize = 11.sp) },
                        modifier = Modifier.height(27.dp)
                    )
                }

                // License Key (Without Watermark) + Contact Admin Telegram Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cs.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.9.dp,
                            color = if (isLicenseActive) Color(0xFF00E676).copy(alpha = 0.55f) else cs.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLicenseActive) Icons.Default.CheckCircle else Icons.Default.Key,
                                    contentDescription = null,
                                    tint = if (isLicenseActive) Color(0xFF00E676) else cs.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isLicenseActive) "License Active (Without Watermark)" else "License Key (Without Watermark)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLicenseActive) Color(0xFF00E676) else cs.onSurface
                                )
                            }
                            if (isLicenseActive) {
                                Text(
                                    text = "Change",
                                    fontSize = 10.sp,
                                    color = cs.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable {
                                        onClearLicenseKey?.invoke()
                                        WatermarkGuard.updateConfig(wmConfig.copy(activatedLicenseKey = ""))
                                        keyStatusMsg = null
                                    }
                                )
                            }
                        }

                        if (isLicenseActive) {
                            Text(
                                text = "Active Key: ${wmConfig.activatedLicenseKey} · Watermark is disabled for PNG & Prompts.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = cs.onSurfaceVariant
                            )
                        } else {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = licenseInput,
                                    onValueChange = {
                                        licenseInput = it.uppercase()
                                        keyStatusMsg = null
                                    },
                                    placeholder = { Text("Enter NH... License Key", fontSize = 10.sp) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = ::activateKey,
                                    enabled = !verifyingKey && licenseInput.isNotBlank(),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    if (verifyingKey) {
                                        CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp, color = cs.onPrimary)
                                    } else {
                                        Text("Apply Key", fontSize = 11.sp)
                                    }
                                }
                            }
                            if (keyStatusMsg != null) {
                                Text(
                                    text = keyStatusMsg!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (keyStatusOk) Color(0xFF00E676) else cs.error
                                )
                            }
                        }

                        // Click to Contact Admin Telegram Channel Link
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = cs.primary.copy(alpha = 0.14f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { openAdminTelegram() }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = cs.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Get License Key · Contact Admin on Telegram",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = cs.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = "Open Telegram",
                                    tint = cs.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                if (exporting) {
                    CircularProgressIndicator(Modifier.size(22.dp))
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { export(share = true) },
                    enabled = !exporting,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share PNG", fontSize = 11.sp)
                }
                Button(
                    onClick = { export(share = false) },
                    enabled = !exporting,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save to Gallery", fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !exporting, modifier = Modifier.height(32.dp)) {
                Text("Cancel", fontSize = 11.sp)
            }
        }
    )
}

@Composable
internal fun StepSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    fineStep: Float = 1f,
    fastStep: Float = 10f,
    onChange: (Float) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label ${value.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(44.dp)
        )
        Box(
            Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(cs.surfaceVariant)
                .clickable { onChange((value - fastStep).coerceIn(range.start, range.endInclusive)) }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("-${fastStep.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.width(3.dp))
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant)
                .clickable { onChange((value - fineStep).coerceIn(range.start, range.endInclusive)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, "Decrease $label", modifier = Modifier.size(12.dp))
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
        )
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant)
                .clickable { onChange((value + fineStep).coerceIn(range.start, range.endInclusive)) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, "Increase $label", modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(3.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(cs.surfaceVariant)
                .clickable { onChange((value + fastStep).coerceIn(range.start, range.endInclusive)) }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("+${fastStep.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExplainLayoutSheet(
    name: String,
    style: DesignEntry?,
    elements: List<DesignElement>,
    layoutNote: String,
    onUpdateNote: (String) -> Unit,
    onAddExplainBoxOnScreen: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var noteText by remember { mutableStateOf(layoutNote) }
    var boxText by remember {
        mutableStateOf("Section title or layout note")
    }
    val breakdown = remember(name, style, elements, noteText) {
        PromptBuilder.explainLayout(name, style, elements, noteText)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Explain Layout & Add Explain Text", style = MaterialTheme.typography.titleLarge)
            Text(
                "Add an Explain Text callout box directly onto the phone layout, or write custom explanation notes for this screen layout.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. Add Explain Text Box on Phone Layout", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = boxText,
                        onValueChange = { boxText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        label = { Text("Text for on-screen Explain Box", fontSize = 11.sp) }
                    )
                    Button(
                        onClick = { onAddExplainBoxOnScreen(boxText) },
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Add Explain Text Box to Phone Screen", fontSize = 12.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("2. Overall Layout Explanation / Notes", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            onUpdateNote(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        label = { Text("Describe how your layout works (included in Prompt)", fontSize = 11.sp) }
                    )
                }
            }

            HorizontalDivider()
            Text("Auto-Generated Layout Breakdown", style = MaterialTheme.typography.titleMedium)
            Text(
                breakdown,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(breakdown))
                        Toast.makeText(context, "Layout explanation copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Copy Explanation", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { shareText(context, "$name - Layout Explanation", breakdown) },
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Share", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
internal fun ModeTogglePill(
    mode: DesignerMode,
    onModeChange: (DesignerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(cs.surfaceVariant.copy(alpha = 0.80f))
            .border(0.8.dp, cs.onSurface.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DesignerMode.entries.forEach { item ->
            val selected = mode == item
            val bg by animateColorAsState(
                targetValue = if (selected) cs.primary else Color.Transparent,
                animationSpec = tween(160),
                label = "modeBg"
            )
            val fg by animateColorAsState(
                targetValue = if (selected) cs.onPrimary else cs.onSurfaceVariant,
                animationSpec = tween(160),
                label = "modeFg"
            )
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable { onModeChange(item) }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (item == DesignerMode.PREVIEW) Icons.Default.Visibility else Icons.Default.Tune,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (item == DesignerMode.EDIT) "Edit" else "Preview",
                    color = fg,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IconPickerSheet(onPick: (String) -> Unit, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    val keys = remember(q) {
        val needle = q.trim().lowercase().replace(' ', '_')
        IconCatalog.icons.keys.filter { needle.isEmpty() || it.contains(needle) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 14.dp).padding(bottom = 14.dp)) {
            Text("Choose an icon", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                q,
                { q = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                label = { Text("Search icons", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
            if (keys.isEmpty()) Text("No icon matches that name.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyVerticalGrid(columns = GridCells.Adaptive(64.dp), modifier = Modifier.heightIn(max = 340.dp)) {
                items(keys, key = { it }) { k ->
                    Column(
                        Modifier.clickable { onPick(k) }.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(IconCatalog.get(k), IconCatalog.label(k), modifier = Modifier.size(24.dp))
                        Text(
                            IconCatalog.label(k),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StylePickerSheet(
    designs: List<DesignEntry>,
    currentId: String,
    onPick: (DesignEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var q by remember { mutableStateOf("") }
    val results = remember(designs, q) { SearchEngine.search(designs, q, null, SortMode.RELEVANCE) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 14.dp).padding(bottom = 14.dp)) {
            Text("Choose a UI design style", style = MaterialTheme.typography.titleLarge)
            Text(
                "All ${designs.size} library styles work. Ones marked Full theme have a hand-tuned look; the rest use their own palette.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                q,
                { q = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                label = { Text("Search styles", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                items(results, key = { it.id }) { e ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onPick(e) }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PaletteBadge(e, 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                e.category + if (e.preview in THEMED_KEYS) " · Full theme" else " · Palette-tinted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (e.id == currentId) Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PromptSheet(
    name: String,
    style: DesignEntry?,
    elements: List<DesignElement>,
    customExplanation: String = "",
    aiState: AiUiState,
    aiConfigured: Boolean,
    onRefine: (String) -> Unit,
    onOpenAiSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val prompt = remember(name, elements, style, customExplanation) {
        PromptBuilder.build(name, style, elements, customExplanation)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Generated prompt", style = MaterialTheme.typography.titleLarge)
            Text(prompt, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(prompt))
                        Toast.makeText(context, "Prompt copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Copy", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { shareText(context, name, prompt) },
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Share", fontSize = 11.sp)
                }
            }
            HorizontalDivider()
            Text("Refine with AI (optional)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Sends this prompt to your configured model and shows its real reply. The prompt above is built locally and works offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (aiConfigured) {
                Button(
                    onClick = { onRefine(prompt) },
                    enabled = aiState !is AiUiState.Loading,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text("Refine with connected model", fontSize = 11.sp)
                }
            } else {
                OutlinedButton(
                    onClick = onOpenAiSettings,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) { Text("Configure AI model", fontSize = 11.sp) }
            }
            when (val s = aiState) {
                AiUiState.Idle -> {}
                AiUiState.Loading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                is AiUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                is AiUiState.Success -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "LIVE AI RESPONSE - ${s.model} - ${s.latencyMs} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(s.text, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp)
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(s.text))
                            Toast.makeText(context, "AI response copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) { Text("Copy AI version", fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
internal fun RenameScreenDialog(
    initialName: String,
    pendingSave: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (pendingSave) "Name your screen" else "Rename screen") },
        text = { OutlinedTextField(text, { text = it }, singleLine = true, label = { Text("Screen name") }) },
        confirmButton = {
            TextButton(onClick = {
                val n = text.trim().ifBlank { "Untitled screen" }
                onConfirm(n)
            }) { Text(if (pendingSave) "Save" else "OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
internal fun DiscardChangesDialog(
    onSave: () -> Unit,
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = { Text("Unsaved changes") },
        text = { Text("Save this screen before leaving?") },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onKeepEditing) { Text("Keep editing") }
                TextButton(onClick = onDiscard) { Text("Discard") }
            }
        }
    )
}
