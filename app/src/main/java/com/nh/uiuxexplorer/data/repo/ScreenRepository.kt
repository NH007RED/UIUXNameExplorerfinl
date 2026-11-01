package com.nh.uiuxexplorer.data.repo

import com.nh.uiuxexplorer.data.local.SavedScreenDao
import com.nh.uiuxexplorer.data.local.SavedScreenEntity
import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.model.SavedScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

object ScreenCodec {
    fun encode(elements: List<DesignElement>): String {
        val arr = JSONArray()
        elements.forEach { e ->
            arr.put(
                JSONObject()
                    .put("id", e.id).put("type", e.type.name)
                    .put("x", e.x.toDouble()).put("y", e.y.toDouble())
                    .put("w", e.w.toDouble()).put("h", e.h.toDouble())
                    .put("rot", e.rotation.toDouble()).put("text", e.text)
                    .put("fs", e.fontSize.toDouble())
                    .put("icon", e.icon).put("ck", e.colorKey)
                    .put("lk", e.locked)
                    .put("img", e.imageUri)
                    .put("crop", e.imageCrop)
            )
        }
        return arr.toString()
    }

    fun decode(json: String): List<DesignElement> {
        val arr = try { JSONArray(json) } catch (e: Exception) { return emptyList() }
        val out = ArrayList<DesignElement>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val type = runCatching { ElementType.valueOf(o.getString("type")) }.getOrNull() ?: continue
            out += DesignElement(
                id = o.optString("id").ifBlank { DesignerDefaults.newId() },
                type = type,
                x = o.optDouble("x", 0.0).toFloat(), y = o.optDouble("y", 0.0).toFloat(),
                w = o.optDouble("w", 100.0).toFloat(), h = o.optDouble("h", 40.0).toFloat(),
                rotation = o.optDouble("rot", 0.0).toFloat(),
                text = o.optString("text", ""),
                fontSize = o.optDouble("fs", 16.0).toFloat(),
                icon = o.optString("icon", ""),
                colorKey = o.optString("ck", "accent").ifBlank { "accent" },
                locked = o.optBoolean("lk", false),
                imageUri = o.optString("img", ""),
                imageCrop = o.optBoolean("crop", true)
            )
        }
        return out
    }
}

/** Shared in-memory draft so navigating between DesignerScreen and PreviewEditScreen keeps live edits seamlessly. */
data class DraftSession(
    val screenId: Long? = null,
    val name: String = "Untitled screen",
    val styleId: String = "glassmorphism",
    val elements: List<DesignElement> = emptyList(),
    val selectedId: String? = null,
    val layoutLocked: Boolean = false,
    val dirty: Boolean = false
)

@Singleton
class ScreenRepository @Inject constructor(private val dao: SavedScreenDao) {

    val saved: Flow<List<SavedScreen>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }.flowOn(Dispatchers.Default)

    private val _activeDraft = MutableStateFlow<DraftSession?>(null)
    val activeDraft: StateFlow<DraftSession?> = _activeDraft.asStateFlow()

    fun setDraft(draft: DraftSession?) {
        _activeDraft.value = draft
    }

    suspend fun load(id: Long): SavedScreen? = dao.get(id)?.toDomain()

    suspend fun save(id: Long?, name: String, styleId: String, elements: List<DesignElement>): Long {
        val now = System.currentTimeMillis()
        val existing = id?.let { dao.get(it) }
        val savedId = dao.upsert(
            SavedScreenEntity(
                id = existing?.id ?: 0,
                name = name.ifBlank { "Untitled screen" },
                styleId = styleId,
                elementsJson = ScreenCodec.encode(elements),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
        _activeDraft.value = _activeDraft.value?.copy(
            screenId = savedId,
            name = name.ifBlank { "Untitled screen" },
            styleId = styleId,
            elements = elements,
            dirty = false
        )
        return savedId
    }

    suspend fun duplicate(id: Long) {
        val s = dao.get(id) ?: return
        val now = System.currentTimeMillis()
        dao.upsert(s.copy(id = 0, name = s.name + " copy", createdAt = now, updatedAt = now))
    }

    suspend fun delete(id: Long) = dao.delete(id)

    private fun SavedScreenEntity.toDomain() =
        SavedScreen(id, name, styleId, ScreenCodec.decode(elementsJson), createdAt, updatedAt)
}
