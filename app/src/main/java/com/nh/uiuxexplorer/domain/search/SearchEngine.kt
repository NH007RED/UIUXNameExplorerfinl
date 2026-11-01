package com.nh.uiuxexplorer.domain.search

import com.nh.uiuxexplorer.domain.model.Categories
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.domain.model.SortMode

/** Pure-Kotlin local search: exact, partial, alias, keyword, category and description matching. */
object SearchEngine {
    private val weakTokens = setOf("ui", "ux", "design", "designs", "style", "styles")
    private val nonAlnum = Regex("[^a-z0-9]+")

    fun norm(s: String): String = s.lowercase().replace(nonAlnum, " ").trim()

    fun search(
        all: List<DesignEntry>,
        query: String,
        category: String?,
        sort: SortMode
    ): List<DesignEntry> {
        val pool = if (category == null) all else all.filter { it.category == category }
        val q = norm(query)
        if (q.isEmpty()) return sortPlain(pool, sort)

        val tokens = q.split(" ").filter { it.isNotEmpty() }
        val strong = tokens.filter { it !in weakTokens }.ifEmpty { tokens }
        val weak = tokens - strong.toSet()

        val scored = pool.mapNotNull { e ->
            val s = score(e, q, strong, weak)
            if (s > 0) e to s else null
        }
        return when (sort) {
            SortMode.RELEVANCE -> scored.sortedWith(
                compareByDescending<Pair<DesignEntry, Int>> { it.second }.thenBy { it.first.name }
            ).map { it.first }
            else -> sortPlain(scored.map { it.first }, sort)
        }
    }

    private fun sortPlain(list: List<DesignEntry>, sort: SortMode): List<DesignEntry> = when (sort) {
        SortMode.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
        SortMode.CATEGORY -> list.sortedWith(
            compareBy<DesignEntry> { Categories.indexOf(it.category) }.thenBy { it.name.lowercase() }
        )
        else -> list.sortedBy { it.name.lowercase() }
    }

    private fun score(e: DesignEntry, phrase: String, strong: List<String>, weak: List<String>): Int {
        val name = norm(e.name)
        val aliases = e.aliases.map { norm(it) }
        val keywords = e.keywords.map { norm(it) }
        val category = norm(e.category)
        val description = norm(e.description)

        var total = 0
        for (t in strong) {
            val s = tokenScore(t, name, aliases, keywords, category, description)
            if (s == 0) return 0
            total += s
        }
        for (t in weak) total += tokenScore(t, name, aliases, keywords, category, description).coerceAtMost(10)

        if (name == phrase) total += 200
        else if (name.contains(phrase)) total += 80
        if (aliases.any { it == phrase }) total += 120
        else if (aliases.any { it.contains(phrase) }) total += 60
        if (keywords.any { it.contains(phrase) }) total += 30
        return total
    }

    private fun tokenScore(
        t: String, name: String, aliases: List<String>, keywords: List<String>,
        category: String, description: String
    ): Int {
        var best = field(t, name, 100, 80, 60)
        for (a in aliases) best = maxOf(best, field(t, a, 70, 55, 45))
        for (k in keywords) best = maxOf(best, field(t, k, 35, 30, 20))
        best = maxOf(best, field(t, category, 15, 12, 10))
        best = maxOf(best, field(t, description, 0, 5, 3))
        return best
    }

    private fun field(t: String, text: String, exact: Int, starts: Int, contains: Int): Int {
        if (text.isEmpty()) return 0
        return when {
            text == t -> exact
            text.startsWith(t) -> starts
            text.split(" ").any { it.startsWith(t) } -> (starts * 0.8).toInt()
            text.contains(t) -> contains
            else -> 0
        }
    }
}
