package com.nh.uiuxexplorer.domain.model

data class DesignEntry(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val aliases: List<String>,
    val keywords: List<String>,
    val characteristics: List<String>,
    val colors: String,
    val typography: String,
    val components: String,
    val layout: String,
    val animation: String,
    val useCases: List<String>,
    val advantages: List<String>,
    val disadvantages: List<String>,
    val related: List<String>,
    val example: String,
    val prompt: String,
    val preview: String,
    val palette: List<String>,
    val popular: Boolean,
    val trending: Boolean
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AiProvider(val label: String) {
    OPENAI_COMPATIBLE("OpenAI-compatible"),
    ANTHROPIC("Anthropic Messages")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val endpoint: String = "",
    val model: String = "",
    val provider: AiProvider = AiProvider.OPENAI_COMPATIBLE,
    val aiLanguage: String = "English",
    val frameStyle: FrameStyle = FrameStyle.PUNCH_HOLE
)

enum class SortMode(val label: String) {
    RELEVANCE("Relevance"),
    NAME_ASC("Name A-Z"),
    NAME_DESC("Name Z-A"),
    CATEGORY("Category")
}

object Categories {
    val names: List<String> = listOf(
        "UI Visual Styles", "UX Design Principles", "Design Systems", "Mobile UI Styles",
        "Web UI Styles", "Navigation Patterns", "Button Styles", "Card Styles",
        "Input Field Styles", "Form Styles", "Icon Styles", "Typography Styles",
        "Color Systems", "Layout Systems", "Grid Systems", "Animation Styles",
        "Transition Effects", "Dashboard Designs", "E-commerce UI", "Landing Page Designs",
        "Accessibility Patterns", "Interaction Patterns", "3D UI Styles", "Experimental UI Styles"
    )

    fun indexOf(name: String): Int = names.indexOf(name).let { if (it < 0) Int.MAX_VALUE else it }
}

val AI_LANGUAGES = listOf(
    "English", "Hindi (Roman script)", "Hindi (Devanagari)", "Spanish", "French",
    "German", "Portuguese", "Arabic", "Japanese"
)
