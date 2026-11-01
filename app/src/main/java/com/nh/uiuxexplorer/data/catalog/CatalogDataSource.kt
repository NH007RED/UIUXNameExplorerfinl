package com.nh.uiuxexplorer.data.catalog

import android.content.Context
import com.nh.uiuxexplorer.domain.model.DesignEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Loads the bundled, offline design catalog from assets/design_catalog.json. */
@Singleton
class CatalogDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun load(): List<DesignEntry> = withContext(Dispatchers.IO) {
        val text = context.assets.open("design_catalog.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(text).getJSONArray("entries")
        List(arr.length()) { arr.getJSONObject(it).toEntry() }
    }

    private fun JSONArray?.strings(): List<String> =
        if (this == null) emptyList() else List(length()) { optString(it) }

    private fun JSONObject.toEntry() = DesignEntry(
        id = getString("id"),
        name = getString("name"),
        category = getString("category"),
        description = getString("description"),
        aliases = optJSONArray("aliases").strings(),
        keywords = optJSONArray("keywords").strings(),
        characteristics = optJSONArray("characteristics").strings(),
        colors = optString("colors"),
        typography = optString("typography"),
        components = optString("components"),
        layout = optString("layout"),
        animation = optString("animation"),
        useCases = optJSONArray("useCases").strings(),
        advantages = optJSONArray("advantages").strings(),
        disadvantages = optJSONArray("disadvantages").strings(),
        related = optJSONArray("related").strings(),
        example = optString("example"),
        prompt = optString("prompt"),
        preview = optString("preview"),
        palette = optJSONArray("palette").strings(),
        popular = optBoolean("popular"),
        trending = optBoolean("trending")
    )
}
