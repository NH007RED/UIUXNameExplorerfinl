package com.nh.uiuxexplorer.domain.designer

import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import kotlin.math.abs
import kotlin.math.roundToInt

/** Turns a screen layout plus a library style into a prompt or human-readable explanation. Pure Kotlin. */
object PromptBuilder {
    private const val W = DesignerDefaults.W
    private const val H = DesignerDefaults.H

    fun build(
        name: String,
        style: DesignEntry?,
        elements: List<DesignElement>,
        customExplanation: String = "",
        includeWatermark: Boolean = true
    ): String {
        val sb = StringBuilder()
        val title = name.ifBlank { "Untitled screen" }
        sb.append("Design a mobile app screen named \"").append(title).append("\" in the ")
        sb.append(style?.name ?: "clean, modern").append(" UI style")
        if (style != null && style.aliases.isNotEmpty()) {
            sb.append(" (also called: ").append(style.aliases.take(3).joinToString(", ")).append(")")
        }
        sb.append(".\n")
        if (style != null) {
            sb.append("Style traits: ").append(style.characteristics.joinToString("; ")).append(".\n")
            sb.append("Colors: ").append(style.colors.trimEnd('.')).append(". Palette: ")
                .append(style.palette.joinToString(", ") { "#" + it.uppercase() }).append(".\n")
            sb.append("Typography: ").append(style.typography.trimEnd('.')).append(".\n")
        }
        if (customExplanation.isNotBlank()) {
            sb.append("Layout explanation & notes: ").append(customExplanation.trim()).append("\n")
        }
        sb.append("Canvas: portrait phone screen, 360 x 780 dp.\n")
        if (elements.isEmpty()) {
            sb.append("No components have been placed yet; propose a sensible layout.\n")
        } else {
            sb.append("Layout, top to bottom (percentages are of screen width and height):\n")
            val sorted = elements.sortedWith(compareBy({ (it.y / 4f).roundToInt() }, { it.x }))
            sorted.forEachIndexed { i, e -> sb.append(i + 1).append(". ").append(describe(e)).append("\n") }
        }
        sb.append(
            "Requirements: follow the layout above exactly, apply the style consistently to every component, " +
                "show default, pressed and disabled states, keep text contrast at WCAG AA or better, " +
                "and use touch targets of at least 48 dp."
        )
        if (includeWatermark) {
            val sig = WatermarkGuard.resolvePromptSignature()
            if (sig.isNotBlank()) {
                sb.append("\n\n").append(sig)
            }
        }
        return sb.toString()
    }

    /** Generates a clear, structured explanation of the current layout for the Explain Layout sheet. */
    fun explainLayout(
        name: String,
        style: DesignEntry?,
        elements: List<DesignElement>,
        customExplanation: String = "",
        includeWatermark: Boolean = true
    ): String {
        val sb = StringBuilder()
        val title = name.ifBlank { "Untitled screen" }
        sb.append("Screen: ").append(title).append("\n")
        sb.append("UI Style: ").append(style?.name ?: "Default Modern").append(" (")
            .append(style?.category ?: "Custom").append(")\n")
        if (customExplanation.isNotBlank()) {
            sb.append("\nUser Layout Notes:\n").append(customExplanation.trim()).append("\n")
        }
        sb.append("\nStructure Breakdown (").append(elements.size).append(" elements, top to bottom):\n")
        val sorted = elements.sortedWith(compareBy({ (it.y / 4f).roundToInt() }, { it.x }))
        sorted.forEachIndexed { i, e ->
            sb.append(i + 1).append(". [").append(e.type.label).append("] ").append(describe(e)).append("\n")
        }
        if (includeWatermark) {
            val sig = WatermarkGuard.resolvePromptSignature()
            if (sig.isNotBlank()) {
                sb.append("\n").append(sig)
            }
        }
        return sb.toString()
    }

    /** Request text for asking a connected AI model to polish a generated prompt. */
    fun refineRequest(prompt: String): String =
        "Improve the following UI design prompt so an AI design tool can follow it precisely. " +
            "Keep every layout fact, position, size and style detail, and preserve any watermark attribution footer at the end. " +
            "Return only the improved prompt.\n\n$prompt"

    private fun colorName(key: String) = when {
        key == "text" -> "text color"
        key == "muted" -> "muted color"
        key == "white" -> "white"
        key == "black" -> "black"
        key == "theme_accent" || key == "accent" -> "accent color"
        key.startsWith("#") -> "color $key"
        else -> "accent color"
    }

    private fun pct(v: Float) = (v * 100f).roundToInt().coerceAtLeast(0)

    private fun kind(e: DesignElement) = when (e.type) {
        ElementType.TOP_BAR -> {
            val (left, right) = DesignerDefaults.topBarIcons(e)
            "Top app bar (left icon: ${left.replace('_', ' ')}, right icon: ${right.replace('_', ' ')})"
        }
        ElementType.HEADLINE -> "Headline text"
        ElementType.PARAGRAPH -> "Body paragraph text"
        ElementType.EXPLAIN_TEXT -> "Explanation / annotation note box"
        ElementType.BUTTON -> "Primary button"
        ElementType.CARD -> "Card with title and two lines of supporting content"
        ElementType.IMAGE -> "Image or media placeholder"
        ElementType.INPUT -> "Text input field"
        ElementType.CHIP -> "Chip"
        ElementType.SWITCH -> "Toggle switch"
        ElementType.FAB -> "Floating action button"
        ElementType.BOTTOM_NAV -> {
            val navIcons = DesignerDefaults.bottomNavIcons(e).joinToString(", ") { it.replace('_', ' ') }
            "Bottom navigation bar with icons [$navIcons]"
        }
        ElementType.ICON -> "Icon"
    }

    private fun describe(e: DesignElement): String {
        val cx = (e.x + e.w / 2f) / W
        val cy = (e.y + e.h / 2f) / H
        val v = when {
            cy < 0.2f -> "top"
            cy < 0.45f -> "upper area"
            cy < 0.7f -> "middle"
            cy < 0.88f -> "lower area"
            else -> "bottom"
        }
        val hz = when {
            e.w >= W * 0.9f -> "full width"
            cx < 0.35f -> "left"
            cx > 0.65f -> "right"
            else -> "centered"
        }
        val sb = StringBuilder(kind(e))
        if (e.type == ElementType.ICON) {
            val name = e.icon.replace('_', ' ').ifBlank { "star" }
            sb.append(" \"").append(name).append("\" in the ").append(colorName(e.colorKey))
        } else if (e.colorKey.isNotBlank() && e.colorKey != "default" && e.colorKey != "accent") {
            sb.append(" in ").append(colorName(e.colorKey))
        }
        if (e.type.hasText && e.text.isNotBlank()) sb.append(" with text \"").append(e.text.trim()).append("\"")
        sb.append(" - ").append(v).append(", ").append(hz)
        sb.append("; position x ").append(pct(e.x / W)).append("% y ").append(pct(e.y / H)).append("%")
        sb.append("; size ").append(pct(e.w / W)).append("% x ").append(pct(e.h / H)).append("%")
        if (abs(e.rotation) > 2f) sb.append("; rotated ").append(e.rotation.roundToInt()).append(" degrees")
        return sb.toString()
    }
}
