package com.nh.uiuxexplorer

import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.domain.model.SortMode
import com.nh.uiuxexplorer.domain.search.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {
    private fun e(name: String, cat: String, aliases: List<String> = emptyList(), kw: List<String> = emptyList()) =
        DesignEntry(
            id = name.lowercase(), name = name, category = cat, description = "desc of $name", aliases = aliases,
            keywords = kw, characteristics = emptyList(), colors = "", typography = "", components = "", layout = "",
            animation = "", useCases = emptyList(), advantages = emptyList(), disadvantages = emptyList(),
            related = emptyList(), example = "", prompt = "", preview = "", palette = emptyList(),
            popular = false, trending = false
        )

    private val all = listOf(
        e("Glassmorphism", "UI Visual Styles", listOf("Frosted Glass UI", "Blur UI", "Transparent UI")),
        e("Liquid Glass", "UI Visual Styles", listOf("Apple Liquid Glass")),
        e("3D Design", "3D UI Styles", listOf("Depth UI")),
        e("Analytics Dashboard", "Dashboard Designs", kw = listOf("modern dashboard"))
    )

    @Test fun partialNameMatches() {
        assertEquals("Glassmorphism", SearchEngine.search(all, "Glassm", null, SortMode.RELEVANCE).first().name)
    }

    @Test fun aliasMatches() {
        assertEquals("Glassmorphism", SearchEngine.search(all, "Blur UI", null, SortMode.RELEVANCE).first().name)
        assertEquals("Glassmorphism", SearchEngine.search(all, "Transparent UI", null, SortMode.RELEVANCE).first().name)
    }

    @Test fun glassFindsBoth() {
        val names = SearchEngine.search(all, "Glass", null, SortMode.RELEVANCE).map { it.name }
        assertTrue("Glassmorphism" in names && "Liquid Glass" in names)
    }

    @Test fun keywordAndCategoryFilter() {
        assertEquals("Analytics Dashboard", SearchEngine.search(all, "Modern Dashboard", null, SortMode.RELEVANCE).first().name)
        assertEquals(1, SearchEngine.search(all, "", "Dashboard Designs", SortMode.NAME_ASC).size)
        assertEquals("3D Design", SearchEngine.search(all, "3D Design", null, SortMode.RELEVANCE).first().name)
    }

    @Test fun noMatchIsEmpty() {
        assertTrue(SearchEngine.search(all, "zzzzqq", null, SortMode.RELEVANCE).isEmpty())
    }
}
