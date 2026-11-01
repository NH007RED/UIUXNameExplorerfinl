package com.nh.uiuxexplorer.ui.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nh.uiuxexplorer.data.local.AiResponseEntity
import com.nh.uiuxexplorer.data.prefs.UserPreferences
import com.nh.uiuxexplorer.data.remote.AiResult
import com.nh.uiuxexplorer.data.repo.AiRepository
import com.nh.uiuxexplorer.data.repo.DesignRepository
import com.nh.uiuxexplorer.data.repo.DraftSession
import com.nh.uiuxexplorer.data.repo.RealtimeAdminState
import com.nh.uiuxexplorer.data.repo.RealtimeDbRepository
import com.nh.uiuxexplorer.data.repo.ScreenRepository
import com.nh.uiuxexplorer.data.secure.SecureKeyStore
import com.nh.uiuxexplorer.domain.designer.PromptBuilder
import com.nh.uiuxexplorer.domain.model.AiProvider
import com.nh.uiuxexplorer.domain.model.AppSettings
import com.nh.uiuxexplorer.domain.model.Categories
import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.DesignerMode
import com.nh.uiuxexplorer.domain.model.ElementAlignment
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.model.FrameStyle
import com.nh.uiuxexplorer.domain.model.ResizeHandle
import com.nh.uiuxexplorer.domain.model.SavedScreen
import com.nh.uiuxexplorer.domain.model.SortMode
import com.nh.uiuxexplorer.domain.model.ThemeMode
import com.nh.uiuxexplorer.domain.search.SearchEngine
import com.nh.uiuxexplorer.domain.security.WatermarkAndAboutConfig
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

private val WhileUi = SharingStarted.WhileSubscribed(5_000)

@HiltViewModel
class AppViewModel @Inject constructor(
    prefs: UserPreferences,
    private val rtdb: RealtimeDbRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        prefs.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val realtimeState: StateFlow<RealtimeAdminState> = rtdb.state

    fun recheckInternetAndSync(): Boolean = rtdb.recheckInternetAndSync()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: DesignRepository,
    private val rtdb: RealtimeDbRepository
) : ViewModel() {
    val designs: StateFlow<List<DesignEntry>> = repo.designs
    val loaded: StateFlow<Boolean> = repo.loaded
    val recent: StateFlow<List<DesignEntry>> = repo.recent.stateIn(viewModelScope, WhileUi, emptyList())
    val favorites: StateFlow<List<DesignEntry>> = repo.favorites.stateIn(viewModelScope, WhileUi, emptyList())
    val realtimeState: StateFlow<RealtimeAdminState> = rtdb.state

    fun verifyAdminLogin(password: String): Boolean = rtdb.verifyAdminLogin(password)
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val rtdb: RealtimeDbRepository
) : ViewModel() {
    val state: StateFlow<RealtimeAdminState> = rtdb.state

    fun refreshSync() = rtdb.refreshSync()
    fun verifyAdminLogin(password: String): Boolean = rtdb.verifyAdminLogin(password)
    fun claimCurrentDeviceAsAdmin(masterKey: String): Boolean = rtdb.claimCurrentDeviceAsAdmin(masterKey)
    fun addAdminDeviceId(deviceId: String) = rtdb.addAdminDeviceId(deviceId)
    fun removeAdminDeviceId(deviceId: String) = rtdb.removeAdminDeviceId(deviceId)
    fun updateAdminPassword(newPassword: String) = rtdb.updateAdminPassword(newPassword)
    fun saveWatermarkAndAbout(config: WatermarkAndAboutConfig) = rtdb.saveWatermarkAndAbout(config)
    fun saveFirebaseConnection(url: String, token: String) = rtdb.saveFirebaseConnection(url, token)
    fun buildFirebaseRulesJson(): String = rtdb.buildFirebaseSecurityRulesJson()
    fun createLicenseKey(rawKeyInput: String, label: String): String = rtdb.createLicenseKey(rawKeyInput, label)
    fun deleteLicenseKey(keyToDelete: String) = rtdb.deleteLicenseKey(keyToDelete)
    fun generateNewNhLicenseKey(): String = WatermarkGuard.generateNewNhLicenseKey()
    fun deleteUserRecord(deviceId: String) = rtdb.deleteUserRecord(deviceId)
}

data class LibraryFilter(
    val query: String = "",
    val category: String? = null,
    val sort: SortMode = SortMode.RELEVANCE,
    val favoritesOnly: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: DesignRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    val autoFocus: Boolean = (savedState.get<Int>("focus") ?: 0) == 1

    private val _filter = MutableStateFlow(
        LibraryFilter(category = (savedState.get<Int>("cat") ?: -1).let { Categories.names.getOrNull(it) })
    )
    val filter: StateFlow<LibraryFilter> = _filter.asStateFlow()

    val results: StateFlow<List<DesignEntry>> =
        combine(repo.designs, _filter, repo.favoriteIds) { all, f, favIds ->
            val base = if (f.favoritesOnly) all.filter { it.id in favIds } else all
            SearchEngine.search(base, f.query, f.category, f.sort)
        }.stateIn(viewModelScope, WhileUi, emptyList())

    val favoriteIds: StateFlow<Set<String>> =
        repo.favoriteIds.map { it.toSet() }.stateIn(viewModelScope, WhileUi, emptySet())

    val history: StateFlow<List<String>> = repo.searchHistory.stateIn(viewModelScope, WhileUi, emptyList())

    fun setQuery(q: String) = _filter.update { it.copy(query = q) }
    fun setCategory(c: String?) = _filter.update { it.copy(category = c) }
    fun setSort(s: SortMode) = _filter.update { it.copy(sort = s) }
    fun toggleFavoritesOnly() = _filter.update { it.copy(favoritesOnly = !it.favoritesOnly) }

    fun commitSearch() {
        val q = _filter.value.query
        viewModelScope.launch { repo.saveSearch(q) }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { repo.toggleFavorite(id) }
    }

    fun deleteSearch(q: String) {
        viewModelScope.launch { repo.deleteSearch(q) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repo.clearSearchHistory() }
    }
}

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val text: String, val model: String, val latencyMs: Long) : AiUiState
    data class Error(val message: String) : AiUiState
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repo: DesignRepository,
    private val ai: AiRepository,
    private val prefs: UserPreferences,
    savedState: SavedStateHandle
) : ViewModel() {
    private val id: String = savedState.get<String>("id").orEmpty()

    val entry: StateFlow<DesignEntry?> = repo.observeEntry(id).stateIn(viewModelScope, WhileUi, null)
    val isFavorite: StateFlow<Boolean> = repo.isFavorite(id).stateIn(viewModelScope, WhileUi, false)
    val related: StateFlow<List<DesignEntry>> =
        combine(repo.designs, repo.observeEntry(id)) { all, e ->
            if (e == null) emptyList() else all.filter { it.name in e.related }
        }.stateIn(viewModelScope, WhileUi, emptyList())
    val aiConfigured: StateFlow<Boolean> =
        prefs.settings.map { it.endpoint.isNotBlank() && it.model.isNotBlank() }
            .stateIn(viewModelScope, WhileUi, false)

    private val _ai = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _ai.asStateFlow()

    init {
        viewModelScope.launch { repo.markViewed(id) }
    }

    fun toggleFavorite() {
        viewModelScope.launch { repo.toggleFavorite(id) }
    }

    fun askAi() {
        val e = entry.value ?: return
        if (_ai.value is AiUiState.Loading) return
        _ai.value = AiUiState.Loading
        viewModelScope.launch {
            val r: AiResult = ai.ask(buildPrompt(e.name), e.id)
            _ai.value = if (r.ok) AiUiState.Success(r.text, prefs.settings.first().model, r.latencyMs)
            else AiUiState.Error(r.text)
        }
    }

    companion object {
        fun buildPrompt(name: String) =
            "In 150 words or fewer: what is the UI/UX design style or term \"$name\"? " +
                "List the alternative names and keywords you associate with it and its key visual characteristics."
    }
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repo: DesignRepository) : ViewModel() {
    val favorites: StateFlow<List<DesignEntry>> = repo.favorites.stateIn(viewModelScope, WhileUi, emptyList())
    val recent: StateFlow<List<DesignEntry>> = repo.recent.stateIn(viewModelScope, WhileUi, emptyList())
    val searches: StateFlow<List<String>> = repo.searchHistory.stateIn(viewModelScope, WhileUi, emptyList())

    fun remove(id: String) {
        viewModelScope.launch { repo.toggleFavorite(id) }
    }

    fun clearRecent() {
        viewModelScope.launch { repo.clearRecent() }
    }

    fun clearSearches() {
        viewModelScope.launch { repo.clearSearchHistory() }
    }

    fun deleteSearch(q: String) {
        viewModelScope.launch { repo.deleteSearch(q) }
    }
}

data class TestState(val running: Boolean = false, val result: AiResult? = null)

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val keys: SecureKeyStore,
    private val ai: AiRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings?> =
        prefs.settings.map<AppSettings, AppSettings?> { it }.stateIn(viewModelScope, WhileUi, null)

    private val _maskedKey = MutableStateFlow(keys.masked())
    val maskedKey: StateFlow<String?> = _maskedKey.asStateFlow()

    val history: StateFlow<List<AiResponseEntity>> = ai.history.stateIn(viewModelScope, WhileUi, emptyList())

    private val _test = MutableStateFlow(TestState())
    val test: StateFlow<TestState> = _test.asStateFlow()

    private val _playground = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val playground: StateFlow<AiUiState> = _playground.asStateFlow()

    fun saveConfig(endpoint: String, model: String, provider: AiProvider) {
        viewModelScope.launch { prefs.setAiConfig(endpoint, model, provider) }
    }

    fun saveKey(key: String): Boolean {
        val ok = keys.save(key)
        _maskedKey.value = keys.masked()
        return ok
    }

    fun clearKey() {
        keys.clear()
        _maskedKey.value = null
    }

    fun runTest() {
        if (_test.value.running) return
        _test.value = TestState(running = true)
        viewModelScope.launch { _test.value = TestState(false, ai.testConnection()) }
    }

    fun ask(prompt: String) {
        val p = prompt.trim()
        if (p.isEmpty() || _playground.value is AiUiState.Loading) return
        _playground.value = AiUiState.Loading
        viewModelScope.launch {
            val r = ai.ask(p, null)
            _playground.value =
                if (r.ok) AiUiState.Success(r.text, prefs.settings.first().model, r.latencyMs)
                else AiUiState.Error(r.text)
        }
    }

    fun deleteResponse(id: Long) {
        viewModelScope.launch { ai.deleteResponse(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { ai.clearHistory() }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val repo: DesignRepository,
    private val ai: AiRepository,
    private val rtdb: RealtimeDbRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = prefs.settings.stateIn(viewModelScope, WhileUi, AppSettings())
    val realtimeState: StateFlow<RealtimeAdminState> = rtdb.state

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamic(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { prefs.setAiLanguage(language) }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repo.clearRecent()
            repo.clearSearchHistory()
            ai.clearHistory()
        }
    }

    fun verifyAdminLogin(password: String): Boolean = rtdb.verifyAdminLogin(password)
    fun claimCurrentDeviceAsAdmin(masterKey: String): Boolean = rtdb.claimCurrentDeviceAsAdmin(masterKey)
}

// ------------------------------------------------------------------ Screen designer & Preview/Edit Page

data class DesignerUi(
    val screenId: Long? = null,
    val name: String = "Untitled screen",
    val styleId: String = "glassmorphism",
    val elements: List<DesignElement> = emptyList(),
    val selectedId: String? = null,
    val layoutNote: String = "",
    val layoutLocked: Boolean = false,
    val mode: DesignerMode = DesignerMode.EDIT,
    val guideV: Boolean = false,
    val guideH: Boolean = false,
    val dirty: Boolean = false,
    val canUndo: Boolean = false,
    val resetZoomTick: Int = 0,
    val loading: Boolean = true
)

@HiltViewModel
class DesignerViewModel @Inject constructor(
    private val designs: DesignRepository,
    private val screens: ScreenRepository,
    private val ai: AiRepository,
    private val prefs: UserPreferences,
    private val rtdb: RealtimeDbRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    private val initialMode = if (savedState.get<String>("mode").equals("preview", ignoreCase = true)) {
        DesignerMode.PREVIEW
    } else {
        DesignerMode.EDIT
    }
    private val useSharedSession = (savedState.get<Int>("session") ?: 0) == 1

    private val _ui = MutableStateFlow(DesignerUi(mode = initialMode))
    val ui: StateFlow<DesignerUi> = _ui.asStateFlow()

    val allDesigns: StateFlow<List<DesignEntry>> = designs.designs

    val style: StateFlow<DesignEntry?> =
        combine(designs.designs, _ui.map { it.styleId }) { list, id -> list.firstOrNull { it.id == id } }
            .stateIn(viewModelScope, WhileUi, null)

    val aiConfigured: StateFlow<Boolean> =
        prefs.settings.map { it.endpoint.isNotBlank() && it.model.isNotBlank() }.stateIn(viewModelScope, WhileUi, false)

    val frame: StateFlow<FrameStyle> =
        prefs.settings.map { it.frameStyle }.stateIn(viewModelScope, WhileUi, FrameStyle.PUNCH_HOLE)

    fun setFrame(f: FrameStyle) {
        viewModelScope.launch { prefs.setFrameStyle(f) }
    }

    suspend fun verifyAndActivateLicenseKey(inputKey: String): Boolean =
        rtdb.verifyAndActivateLicenseKey(inputKey)

    fun clearActivatedLicenseKey() = rtdb.clearActivatedLicenseKey()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events = _events

    private val _ai = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _ai.asStateFlow()

    private val undoStack = ArrayDeque<List<DesignElement>>()
    private var lastPush = 0L
    private var rawX = 0f
    private var rawY = 0f
    private var rawRot = 0f
    private var gestureUndoPushed = false

    init {
        val id = savedState.get<Long>("id") ?: -1L
        val styleArg = savedState.get<String>("style").orEmpty()
        viewModelScope.launch {
            val existingDraft = screens.activeDraft.value
            if (useSharedSession && existingDraft != null) {
                _ui.value = DesignerUi(
                    screenId = existingDraft.screenId,
                    name = existingDraft.name,
                    styleId = existingDraft.styleId,
                    elements = existingDraft.elements,
                    selectedId = if (initialMode == DesignerMode.PREVIEW) null else existingDraft.selectedId,
                    layoutLocked = existingDraft.layoutLocked,
                    mode = initialMode,
                    dirty = existingDraft.dirty,
                    canUndo = undoStack.isNotEmpty(),
                    loading = false
                )
            } else {
                val loaded = if (id >= 0) screens.load(id) else null
                val initialUi = if (loaded != null) {
                    DesignerUi(
                        screenId = loaded.id,
                        name = loaded.name,
                        styleId = loaded.styleId,
                        elements = loaded.elements,
                        mode = initialMode,
                        loading = false
                    )
                } else {
                    DesignerUi(
                        styleId = styleArg.ifBlank { "glassmorphism" },
                        elements = DesignerDefaults.starter(),
                        mode = initialMode,
                        loading = false
                    )
                }
                _ui.value = initialUi
                syncDraftToRepo(initialUi)
            }

            screens.activeDraft.collect { draft ->
                if (draft != null && !_ui.value.loading) {
                    val cur = _ui.value
                    if (draft.elements != cur.elements || draft.styleId != cur.styleId ||
                        draft.name != cur.name || draft.screenId != cur.screenId ||
                        draft.dirty != cur.dirty || draft.layoutLocked != cur.layoutLocked
                    ) {
                        _ui.update {
                            it.copy(
                                screenId = draft.screenId,
                                name = draft.name,
                                styleId = draft.styleId,
                                elements = draft.elements,
                                layoutLocked = draft.layoutLocked,
                                dirty = draft.dirty
                            )
                        }
                    }
                }
            }
        }
    }

    private fun syncDraftToRepo(st: DesignerUi = _ui.value) {
        screens.setDraft(
            DraftSession(
                screenId = st.screenId,
                name = st.name,
                styleId = st.styleId,
                elements = st.elements,
                selectedId = st.selectedId,
                layoutLocked = st.layoutLocked,
                dirty = st.dirty
            )
        )
    }

    fun prepareSessionForNewPage() {
        syncDraftToRepo(_ui.value)
    }

    private fun pushUndo(coalesceMs: Long = 0): Boolean {
        val now = android.os.SystemClock.elapsedRealtime()
        if (coalesceMs > 0 && now - lastPush < coalesceMs) return false
        undoStack.addLast(_ui.value.elements)
        if (undoStack.size > 60) undoStack.removeFirst()
        lastPush = now
        _ui.update { it.copy(canUndo = true) }
        return true
    }

    private fun setElements(list: List<DesignElement>, selected: String? = _ui.value.selectedId) {
        _ui.update { it.copy(elements = list, selectedId = selected, dirty = true) }
        syncDraftToRepo()
    }

    private fun updateSelected(coalesceMs: Long = 800, allowWhenLocked: Boolean = false, f: (DesignElement) -> DesignElement) {
        if (_ui.value.layoutLocked && !allowWhenLocked) return
        val id = _ui.value.selectedId ?: return
        val target = _ui.value.elements.firstOrNull { it.id == id } ?: return
        if (target.locked && !allowWhenLocked) return
        pushUndo(coalesceMs)
        setElements(_ui.value.elements.map { if (it.id == id) f(it) else it })
    }

    fun select(id: String?) {
        _ui.update { it.copy(selectedId = id, guideV = false, guideH = false) }
        syncDraftToRepo()
    }

    fun finishElementEdit() {
        _ui.update { it.copy(selectedId = null, guideV = false, guideH = false, resetZoomTick = it.resetZoomTick + 1) }
        syncDraftToRepo()
    }

    // ---- Lock / Unlock Layout & Elements ----------------------------------

    fun toggleLayoutLock() {
        val nextLocked = !_ui.value.layoutLocked
        _ui.update {
            it.copy(
                layoutLocked = nextLocked,
                selectedId = if (nextLocked) null else it.selectedId,
                guideV = false,
                guideH = false
            )
        }
        syncDraftToRepo()
        viewModelScope.launch {
            _events.emit(if (nextLocked) "Layout locked 🔒" else "Layout unlocked 🔓")
        }
    }

    fun toggleSelectedElementLock() {
        val id = _ui.value.selectedId ?: return
        val cur = _ui.value.elements.firstOrNull { it.id == id } ?: return
        val nextLocked = !cur.locked
        pushUndo(0)
        setElements(_ui.value.elements.map { if (it.id == id) it.copy(locked = nextLocked) else it })
        viewModelScope.launch {
            _events.emit(if (nextLocked) "${cur.type.label} locked 🔒" else "${cur.type.label} unlocked 🔓")
        }
    }

    fun lockAllElements() {
        if (_ui.value.elements.isEmpty()) return
        pushUndo(0)
        _ui.update { st ->
            st.copy(
                elements = st.elements.map { it.copy(locked = true) },
                selectedId = null,
                dirty = true
            )
        }
        syncDraftToRepo()
        viewModelScope.launch { _events.emit("All elements locked 🔒") }
    }

    fun unlockAllElements() {
        if (_ui.value.elements.isEmpty()) return
        pushUndo(0)
        _ui.update { st ->
            st.copy(
                elements = st.elements.map { it.copy(locked = false) },
                layoutLocked = false,
                dirty = true
            )
        }
        syncDraftToRepo()
        viewModelScope.launch { _events.emit("All elements unlocked 🔓") }
    }

    fun setMode(mode: DesignerMode) {
        _ui.update {
            it.copy(
                mode = mode,
                selectedId = if (mode == DesignerMode.PREVIEW) null else it.selectedId,
                guideV = false,
                guideH = false,
                resetZoomTick = if (mode == DesignerMode.PREVIEW) it.resetZoomTick + 1 else it.resetZoomTick
            )
        }
    }

    fun toggleMode() {
        val next = if (_ui.value.mode == DesignerMode.EDIT) DesignerMode.PREVIEW else DesignerMode.EDIT
        setMode(next)
    }

    fun setName(n: String) {
        _ui.update { it.copy(name = n, dirty = true) }
        syncDraftToRepo()
    }

    fun setLayoutNote(note: String) {
        _ui.update { it.copy(layoutNote = note, dirty = true) }
    }

    fun setStyle(id: String) {
        _ui.update { it.copy(styleId = id, dirty = true) }
        syncDraftToRepo()
    }

    fun addElement(type: ElementType) {
        if (_ui.value.layoutLocked) {
            viewModelScope.launch { _events.emit("Unlock layout first to add elements") }
            return
        }
        val e = DesignerDefaults.create(type, _ui.value.elements.size)
        pushUndo()
        setElements(_ui.value.elements + e, e.id)
    }

    fun addExplainTextOnScreen(text: String) {
        if (_ui.value.layoutLocked) {
            viewModelScope.launch { _events.emit("Unlock layout first to add elements") }
            return
        }
        val clean = text.trim().ifBlank { "Section title or layout note" }
        val e = DesignerDefaults.create(ElementType.EXPLAIN_TEXT, _ui.value.elements.size).copy(text = clean)
        pushUndo()
        setElements(_ui.value.elements + e, e.id)
    }

    fun deleteSelected() {
        val id = _ui.value.selectedId ?: return
        val cur = _ui.value.elements.firstOrNull { it.id == id } ?: return
        if (cur.locked || _ui.value.layoutLocked) {
            viewModelScope.launch { _events.emit("Element is locked 🔒 — Unlock to delete") }
            return
        }
        pushUndo()
        setElements(_ui.value.elements.filterNot { it.id == id }, null)
    }

    fun duplicateSelected() {
        if (_ui.value.layoutLocked) return
        val e = _ui.value.elements.firstOrNull { it.id == _ui.value.selectedId } ?: return
        val copy = e.copy(id = DesignerDefaults.newId(), x = e.x + 14f, y = e.y + 14f, locked = false)
        pushUndo()
        setElements(_ui.value.elements + copy, copy.id)
    }

    fun bringToFront() {
        if (_ui.value.layoutLocked) return
        val e = _ui.value.elements.firstOrNull { it.id == _ui.value.selectedId } ?: return
        if (e.locked) return
        pushUndo()
        setElements(_ui.value.elements.filterNot { it.id == e.id } + e)
    }

    fun sendToBack() {
        if (_ui.value.layoutLocked) return
        val e = _ui.value.elements.firstOrNull { it.id == _ui.value.selectedId } ?: return
        if (e.locked) return
        pushUndo()
        setElements(listOf(e) + _ui.value.elements.filterNot { it.id == e.id })
    }

    fun resetStarterLayout() {
        if (_ui.value.layoutLocked) {
            viewModelScope.launch { _events.emit("Unlock layout first to reset") }
            return
        }
        pushUndo()
        _ui.update {
            it.copy(
                elements = DesignerDefaults.starter(),
                selectedId = null,
                dirty = true,
                resetZoomTick = it.resetZoomTick + 1
            )
        }
        syncDraftToRepo()
    }

    fun setText(t: String) = updateSelected(allowWhenLocked = true) { it.copy(text = t) }
    fun setFontSize(v: Float) = updateSelected(allowWhenLocked = true) { it.copy(fontSize = v.coerceIn(8f, 64f)) }
    fun setWidth(v: Float) = updateSelected { it.copy(w = v.coerceIn(DesignerDefaults.MIN_W, 360f)) }
    fun setHeight(v: Float) = updateSelected { it.copy(h = v.coerceIn(DesignerDefaults.MIN_H, 780f)) }
    fun setX(v: Float) = updateSelected { it.copy(x = v.coerceIn(-it.w + 24f, DesignerDefaults.W - 24f)) }
    fun setY(v: Float) = updateSelected { it.copy(y = v.coerceIn(-it.h + 24f, DesignerDefaults.H - 24f)) }
    fun setRotation(v: Float) = updateSelected { it.copy(rotation = v) }
    fun setIcon(key: String) = updateSelected(0, allowWhenLocked = true) { it.copy(icon = key) }
    fun setColorKey(key: String) = updateSelected(0, allowWhenLocked = true) { it.copy(colorKey = key) }

    /** Sets or clears a Gallery image path on the selected IMAGE element. */
    fun setImageUri(path: String) = updateSelected(0, allowWhenLocked = true) { it.copy(imageUri = path) }

    /** Toggles between CenterCrop fill (true) and Fit inside (false) for an uploaded Gallery image. */
    fun setImageCrop(crop: Boolean) = updateSelected(0, allowWhenLocked = true) { it.copy(imageCrop = crop) }

    fun setNavIconAt(slotIndex: Int, iconKey: String) = updateSelected(0, allowWhenLocked = true) { el ->
        when (el.type) {
            ElementType.BOTTOM_NAV -> {
                val list = DesignerDefaults.bottomNavIcons(el).toMutableList()
                if (slotIndex in list.indices) {
                    list[slotIndex] = iconKey
                }
                el.copy(icon = list.joinToString(","))
            }
            ElementType.TOP_BAR -> {
                val (left, right) = DesignerDefaults.topBarIcons(el)
                val updated = if (slotIndex == 0) "$iconKey,$right" else "$left,$iconKey"
                el.copy(icon = updated)
            }
            else -> el.copy(icon = iconKey)
        }
    }

    fun addBottomNavSlot(defaultIcon: String = "person") = updateSelected(0, allowWhenLocked = true) { el ->
        if (el.type != ElementType.BOTTOM_NAV) return@updateSelected el
        val list = DesignerDefaults.bottomNavIcons(el)
        if (list.size >= 5) el else el.copy(icon = (list + defaultIcon).joinToString(","))
    }

    fun removeBottomNavSlot() = updateSelected(0, allowWhenLocked = true) { el ->
        if (el.type != ElementType.BOTTOM_NAV) return@updateSelected el
        val list = DesignerDefaults.bottomNavIcons(el)
        if (list.size <= 2) el else el.copy(icon = list.dropLast(1).joinToString(","))
    }

    fun scaleSelected(factor: Float) = updateSelected(250) { DesignerDefaults.scaleElement(it, factor) }

    fun applySizePreset(preset: String) = updateSelected(0) { el ->
        val defaultEl = DesignerDefaults.create(el.type, 0)
        val cx = el.x + el.w / 2f
        val cy = el.y + el.h / 2f
        val (nw, nh) = when (preset) {
            "slim" -> el.w.coerceAtLeast(DesignerDefaults.MIN_W) to (defaultEl.h * 0.62f).coerceAtLeast(DesignerDefaults.MIN_H)
            "small" -> (defaultEl.w * 0.65f).coerceAtLeast(DesignerDefaults.MIN_W) to (defaultEl.h * 0.75f).coerceAtLeast(DesignerDefaults.MIN_H)
            "medium" -> defaultEl.w to defaultEl.h
            "large" -> (defaultEl.w * 1.2f).coerceAtMost(DesignerDefaults.W) to (defaultEl.h * 1.3f).coerceAtMost(DesignerDefaults.H)
            "full" -> (DesignerDefaults.W - 24f) to el.h
            else -> el.w to el.h
        }
        val nx = if (preset == "full") 12f else (cx - nw / 2f).coerceIn(0f, (DesignerDefaults.W - nw).coerceAtLeast(0f))
        val ny = (cy - nh / 2f).coerceIn(0f, (DesignerDefaults.H - nh).coerceAtLeast(0f))
        val nfs = if (preset == "slim" && el.type.hasText) (defaultEl.fontSize * 0.85f).coerceIn(8f, 64f) else el.fontSize
        el.copy(x = nx, y = ny, w = nw, h = nh, fontSize = nfs)
    }

    fun nudgeSelected(dx: Float, dy: Float) = updateSelected(250) { el ->
        val nx = (el.x + dx).coerceIn(-el.w + 24f, DesignerDefaults.W - 24f)
        val ny = (el.y + dy).coerceIn(-el.h + 24f, DesignerDefaults.H - 24f)
        el.copy(x = nx, y = ny)
    }

    fun alignSelected(alignment: ElementAlignment) = updateSelected(0) { el ->
        DesignerDefaults.alignElement(el, alignment)
    }

    fun addIcon(key: String) {
        if (_ui.value.layoutLocked) return
        val e = DesignerDefaults
            .create(ElementType.ICON, _ui.value.elements.size)
            .copy(icon = key)
        pushUndo()
        setElements(_ui.value.elements + e, e.id)
    }

    fun undo() {
        val prev = undoStack.removeLastOrNull() ?: return
        val sel = _ui.value.selectedId?.takeIf { id -> prev.any { it.id == id } }
        _ui.update { it.copy(elements = prev, selectedId = sel, dirty = true, canUndo = undoStack.isNotEmpty()) }
        syncDraftToRepo()
    }

    // ---- finger gestures -------------------------------------------------

    fun beginGesture(id: String) {
        if (_ui.value.layoutLocked) return
        val e = _ui.value.elements.firstOrNull { it.id == id } ?: return
        if (!e.locked) {
            gestureUndoPushed = pushUndo(coalesceMs = 300)
            rawX = e.x
            rawY = e.y
            rawRot = e.rotation
        }
        _ui.update { it.copy(selectedId = id) }
        syncDraftToRepo()
    }

    fun cancelGesture() {
        if (gestureUndoPushed && undoStack.isNotEmpty()) {
            val prev = undoStack.removeLast()
            _ui.update {
                it.copy(
                    elements = prev,
                    guideV = false,
                    guideH = false,
                    canUndo = undoStack.isNotEmpty()
                )
            }
            syncDraftToRepo()
        } else {
            _ui.update { it.copy(guideV = false, guideH = false) }
        }
        gestureUndoPushed = false
    }

    fun transform(id: String, panX: Float, panY: Float, zoom: Float, rotDeg: Float) {
        if (_ui.value.layoutLocked) return
        val dm = DesignerDefaults
        val e = _ui.value.elements.firstOrNull { it.id == id } ?: return
        if (e.locked) return
        val nw = (e.w * zoom).coerceIn(dm.MIN_W, 432f)
        val nh = (e.h * zoom).coerceIn(dm.MIN_H, 780f)
        rawX += panX + (e.w - nw) / 2f
        rawY += panY + (e.h - nh) / 2f
        rawRot += rotDeg
        var nx = rawX
        var ny = rawY
        val gv = abs(nx + nw / 2f - dm.W / 2f) < SNAP
        val gh = abs(ny + nh / 2f - dm.H / 2f) < SNAP
        if (gv) nx = dm.W / 2f - nw / 2f
        if (gh) ny = dm.H / 2f - nh / 2f
        nx = nx.coerceIn(-nw + 24f, dm.W - 24f)
        ny = ny.coerceIn(-nh + 24f, dm.H - 24f)
        val rot = dm.snapAngle(rawRot, 3f)
        _ui.update { st ->
            st.copy(
                elements = st.elements.map { if (it.id == id) it.copy(x = nx, y = ny, w = nw, h = nh, rotation = rot) else it },
                guideV = gv,
                guideH = gh,
                dirty = true
            )
        }
        syncDraftToRepo()
    }

    fun resize(id: String, dw: Float, dh: Float) {
        resizeCorner(id, ResizeHandle.BOTTOM_RIGHT, dw, dh)
    }

    fun resizeCorner(id: String, handle: ResizeHandle, localDx: Float, localDy: Float) {
        if (_ui.value.layoutLocked) return
        val dm = DesignerDefaults
        val current = _ui.value.elements.firstOrNull { it.id == id } ?: return
        if (current.locked) return
        val updated = dm.resizeFromHandle(current, handle, localDx, localDy)
        val gv = abs(updated.x + updated.w / 2f - dm.W / 2f) < SNAP
        val gh = abs(updated.y + updated.h / 2f - dm.H / 2f) < SNAP
        _ui.update { st ->
            st.copy(
                elements = st.elements.map { if (it.id == id) updated else it },
                guideV = gv,
                guideH = gh,
                dirty = true
            )
        }
        syncDraftToRepo()
    }

    fun rotateTo(id: String, angleDeg: Float) {
        if (_ui.value.layoutLocked) return
        val current = _ui.value.elements.firstOrNull { it.id == id } ?: return
        if (current.locked) return
        val snapped = DesignerDefaults.snapAngle(angleDeg, 4f)
        _ui.update { st ->
            st.copy(
                elements = st.elements.map { if (it.id == id) it.copy(rotation = snapped) else it },
                dirty = true
            )
        }
        syncDraftToRepo()
    }

    fun endGesture() {
        gestureUndoPushed = false
        _ui.update { it.copy(guideV = false, guideH = false) }
        syncDraftToRepo()
    }

    // ---- save / AI --------------------------------------------------------

    fun save(name: String? = null) {
        viewModelScope.launch {
            val st = _ui.value
            val finalName = (name ?: st.name).ifBlank { "Untitled screen" }
            val id = screens.save(st.screenId, finalName, st.styleId, st.elements)
            _ui.update {
                it.copy(
                    screenId = id,
                    name = finalName,
                    selectedId = null,
                    guideV = false,
                    guideH = false,
                    dirty = false,
                    resetZoomTick = it.resetZoomTick + 1
                )
            }
            syncDraftToRepo()
            _events.emit("Screen saved & layout set!")
        }
    }

    fun refinePrompt(prompt: String) {
        if (_ai.value is AiUiState.Loading) return
        _ai.value = AiUiState.Loading
        viewModelScope.launch {
            val r = ai.ask(PromptBuilder.refineRequest(prompt), null)
            _ai.value = if (r.ok) AiUiState.Success(r.text, prefs.settings.first().model, r.latencyMs) else AiUiState.Error(r.text)
        }
    }

    fun clearAi() { _ai.value = AiUiState.Idle }

    private companion object {
        const val SNAP = 5f
    }
}

@HiltViewModel
class ScreensViewModel @Inject constructor(
    private val screens: ScreenRepository,
    private val prefs: UserPreferences,
    private val rtdb: RealtimeDbRepository,
    designs: DesignRepository
) : ViewModel() {
    val saved: StateFlow<List<SavedScreen>> =
        screens.saved.stateIn(viewModelScope, WhileUi, emptyList())
    val designs: StateFlow<List<DesignEntry>> = designs.designs
    val frame: StateFlow<FrameStyle> =
        prefs.settings.map { it.frameStyle }.stateIn(viewModelScope, WhileUi, FrameStyle.PUNCH_HOLE)

    suspend fun verifyAndActivateLicenseKey(inputKey: String): Boolean =
        rtdb.verifyAndActivateLicenseKey(inputKey)

    fun clearActivatedLicenseKey() = rtdb.clearActivatedLicenseKey()

    fun clearActiveDraft() {
        screens.setDraft(null)
    }

    fun delete(id: Long) {
        viewModelScope.launch { screens.delete(id) }
    }

    fun duplicate(id: Long) {
        viewModelScope.launch { screens.duplicate(id) }
    }
}
