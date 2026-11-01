package com.nh.uiuxexplorer.ui.designer

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.ui.vm.DesignerViewModel
import kotlinx.coroutines.launch

@Composable
fun PreviewEditScreen(
    onBack: () -> Unit,
    onOpenAiSettings: () -> Unit,
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
        onOpenFullPreview = null,
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
