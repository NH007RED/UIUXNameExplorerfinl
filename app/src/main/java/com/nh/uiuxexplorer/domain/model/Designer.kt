package com.nh.uiuxexplorer.domain.model

import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class ElementType(val label: String, val hasText: Boolean) {
    TOP_BAR("Top bar", true),
    HEADLINE("Headline", true),
    PARAGRAPH("Paragraph", true),
    EXPLAIN_TEXT("Explain text", true),
    BUTTON("Button", true),
    CARD("Card", true),
    IMAGE("Image", false),
    INPUT("Input", true),
    CHIP("Chip", true),
    SWITCH("Switch", false),
    FAB("FAB", false),
    BOTTOM_NAV("Bottom nav", false),
    ICON("Icon", false)
}

enum class FrameStyle(val label: String) {
    NONE("No frame"),
    PUNCH_HOLE("Punch-hole phone"),
    NOTCH("Notch phone")
}

enum class DesignerMode(val label: String) {
    EDIT("Edit Layout"),
    PREVIEW("Preview")
}

enum class ResizeHandle {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    RIGHT,
    BOTTOM_RIGHT,
    BOTTOM,
    BOTTOM_LEFT,
    LEFT
}

typealias CornerHandle = ResizeHandle

enum class ElementAlignment(val label: String) {
    LEFT("Left"),
    CENTER_H("Center H"),
    RIGHT("Right"),
    TOP("Top"),
    CENTER_V("Center V"),
    BOTTOM("Bottom"),
    FULL_WIDTH("Full Width"),
    HALF_LEFT("1/2 Left"),
    HALF_RIGHT("1/2 Right"),
    COL_1("Col 1/3"),
    COL_2("Col 2/3"),
    COL_3("Col 3/3")
}

/** Position and size are in design units: the canvas is 360 x 780 units (like dp on a phone). */
data class DesignElement(
    val id: String,
    val type: ElementType,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val rotation: Float = 0f,
    val text: String = "",
    val fontSize: Float = 15f,
    val icon: String = "",
    val colorKey: String = "accent",
    val locked: Boolean = false,
    val imageUri: String = "",
    val imageCrop: Boolean = true
)

data class SavedScreen(
    val id: Long,
    val name: String,
    val styleId: String,
    val elements: List<DesignElement>,
    val createdAt: Long,
    val updatedAt: Long
)

object DesignerDefaults {
    const val W = 360f
    const val H = 780f
    const val MIN_W = 20f
    const val MIN_H = 14f

    val DEFAULT_BOTTOM_NAV_ICONS = listOf("home", "search", "favorite", "settings")
    val DEFAULT_TOP_BAR_ICONS = listOf("menu", "search")

    fun newId(): String = UUID.randomUUID().toString()

    fun create(type: ElementType, index: Int): DesignElement {
        val cascade = (index % 6) * 12f
        fun centered(w: Float, h: Float, text: String = "", fs: Float = 14f, icon: String = "") =
            DesignElement(newId(), type, (W - w) / 2f, 190f + cascade, w, h, 0f, text, fs, icon)
        return when (type) {
            ElementType.TOP_BAR -> DesignElement(newId(), type, 0f, 0f, W, 48f, 0f, "Home", 16f, "menu,search")
            ElementType.HEADLINE -> centered(296f, 36f, "Welcome back", 22f)
            ElementType.PARAGRAPH -> centered(296f, 52f, "Short supporting text that explains this screen.", 13f)
            ElementType.EXPLAIN_TEXT -> centered(
                312f,
                46f,
                "Section title or layout note",
                13f,
                "info"
            )
            ElementType.BUTTON -> centered(296f, 44f, "Continue", 15f)
            ElementType.CARD -> centered(296f, 102f, "Card title", 15f)
            ElementType.IMAGE -> centered(296f, 138f, icon = "image")
            ElementType.INPUT -> centered(296f, 44f, "Search...", 14f, "search")
            ElementType.CHIP -> centered(88f, 30f, "Chip", 12f)
            ElementType.SWITCH -> centered(48f, 26f)
            ElementType.FAB -> DesignElement(newId(), type, W - 48f - 18f, 650f, 48f, 48f, icon = "add")
            ElementType.BOTTOM_NAV -> DesignElement(newId(), type, 0f, H - 54f, W, 54f, icon = "home,search,favorite,settings")
            ElementType.ICON -> centered(40f, 40f, icon = "star")
        }
    }

    fun starter(): List<DesignElement> = listOf(
        DesignElement(newId(), ElementType.TOP_BAR, 0f, 0f, W, 48f, 0f, "Home", 16f, "menu,search"),
        DesignElement(newId(), ElementType.HEADLINE, 32f, 72f, 296f, 36f, 0f, "Welcome back", 22f),
        DesignElement(newId(), ElementType.IMAGE, 32f, 122f, 296f, 138f, icon = "image"),
        DesignElement(newId(), ElementType.PARAGRAPH, 32f, 274f, 296f, 52f, 0f, "Short supporting text that explains this screen.", 13f),
        DesignElement(newId(), ElementType.BUTTON, 32f, 342f, 296f, 44f, 0f, "Continue", 15f),
        DesignElement(newId(), ElementType.BOTTOM_NAV, 0f, H - 54f, W, 54f, icon = "home,search,favorite,settings")
    )

    fun bottomNavIcons(el: DesignElement): List<String> {
        val parsed = el.icon.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return if (parsed.size >= 2) parsed.take(5) else DEFAULT_BOTTOM_NAV_ICONS
    }

    fun topBarIcons(el: DesignElement): Pair<String, String> {
        val parts = el.icon.split(',').map { it.trim() }
        val left = parts.getOrNull(0)?.takeIf { it.isNotEmpty() } ?: DEFAULT_TOP_BAR_ICONS[0]
        val right = parts.getOrNull(1)?.takeIf { it.isNotEmpty() } ?: DEFAULT_TOP_BAR_ICONS[1]
        return left to right
    }

    fun toElementLocal(ux: Float, uy: Float, el: DesignElement): Pair<Float, Float> {
        val cx = el.x + el.w / 2f
        val cy = el.y + el.h / 2f
        val dx = ux - cx
        val dy = uy - cy
        if (el.rotation == 0f) return ux to uy
        val rad = Math.toRadians(-el.rotation.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        return (cx + dx * c - dy * s) to (cy + dx * s + dy * c)
    }

    fun hitTestElement(ux: Float, uy: Float, el: DesignElement, slopUnits: Float = 2f): Boolean {
        val (lx, ly) = toElementLocal(ux, uy, el)
        return lx in (el.x - slopUnits)..(el.x + el.w + slopUnits) &&
            ly in (el.y - slopUnits)..(el.y + el.h + slopUnits)
    }

    fun resizeFromCorner(el: DesignElement, handle: ResizeHandle, localDx: Float, localDy: Float): DesignElement =
        resizeFromHandle(el, handle, localDx, localDy)

    fun resizeFromHandle(el: DesignElement, handle: ResizeHandle, localDx: Float, localDy: Float): DesignElement {
        if (el.locked) return el
        var nx = el.x
        var ny = el.y
        var nw = el.w
        var nh = el.h
        when (handle) {
            ResizeHandle.BOTTOM_RIGHT -> {
                nw = (el.w + localDx).coerceIn(MIN_W, 432f)
                nh = (el.h + localDy).coerceIn(MIN_H, H)
            }
            ResizeHandle.BOTTOM -> {
                nh = (el.h + localDy).coerceIn(MIN_H, H)
            }
            ResizeHandle.RIGHT -> {
                nw = (el.w + localDx).coerceIn(MIN_W, 432f)
            }
            ResizeHandle.BOTTOM_LEFT -> {
                val targetW = (el.w - localDx).coerceIn(MIN_W, 432f)
                nx = el.x + (el.w - targetW)
                nw = targetW
                nh = (el.h + localDy).coerceIn(MIN_H, H)
            }
            ResizeHandle.LEFT -> {
                val targetW = (el.w - localDx).coerceIn(MIN_W, 432f)
                nx = el.x + (el.w - targetW)
                nw = targetW
            }
            ResizeHandle.TOP_RIGHT -> {
                nw = (el.w + localDx).coerceIn(MIN_W, 432f)
                val targetH = (el.h - localDy).coerceIn(MIN_H, H)
                ny = el.y + (el.h - targetH)
                nh = targetH
            }
            ResizeHandle.TOP -> {
                val targetH = (el.h - localDy).coerceIn(MIN_H, H)
                ny = el.y + (el.h - targetH)
                nh = targetH
            }
            ResizeHandle.TOP_LEFT -> {
                val targetW = (el.w - localDx).coerceIn(MIN_W, 432f)
                val targetH = (el.h - localDy).coerceIn(MIN_H, H)
                nx = el.x + (el.w - targetW)
                ny = el.y + (el.h - targetH)
                nw = targetW
                nh = targetH
            }
        }
        nx = nx.coerceIn(-nw + 24f, W - 24f)
        ny = ny.coerceIn(-nh + 24f, H - 24f)
        return el.copy(x = nx, y = ny, w = nw, h = nh)
    }

    fun scaleElement(el: DesignElement, factor: Float): DesignElement {
        if (el.locked) return el
        val cx = el.x + el.w / 2f
        val cy = el.y + el.h / 2f
        val nw = (el.w * factor).coerceIn(MIN_W, W)
        val nh = (el.h * factor).coerceIn(MIN_H, H)
        val nx = (cx - nw / 2f).coerceIn(0f, (W - nw).coerceAtLeast(0f))
        val ny = (cy - nh / 2f).coerceIn(0f, (H - nh).coerceAtLeast(0f))
        val nfs = if (el.type.hasText) (el.fontSize * factor).coerceIn(8f, 64f) else el.fontSize
        return el.copy(x = nx, y = ny, w = nw, h = nh, fontSize = nfs)
    }

    fun alignElement(el: DesignElement, alignment: ElementAlignment): DesignElement {
        if (el.locked) return el
        return when (alignment) {
            ElementAlignment.LEFT -> el.copy(x = 16f)
            ElementAlignment.CENTER_H -> el.copy(x = (W - el.w) / 2f)
            ElementAlignment.RIGHT -> el.copy(x = (W - el.w - 16f).coerceAtLeast(0f))
            ElementAlignment.TOP -> el.copy(y = 48f)
            ElementAlignment.CENTER_V -> el.copy(y = (H - el.h) / 2f)
            ElementAlignment.BOTTOM -> el.copy(y = (H - el.h - 54f).coerceAtLeast(0f))
            ElementAlignment.FULL_WIDTH -> el.copy(x = 16f, w = W - 32f)
            ElementAlignment.HALF_LEFT -> el.copy(x = 16f, w = 158f)
            ElementAlignment.HALF_RIGHT -> el.copy(x = 186f, w = 158f)
            ElementAlignment.COL_1 -> el.copy(x = 16f, w = 102f)
            ElementAlignment.COL_2 -> el.copy(x = 129f, w = 102f)
            ElementAlignment.COL_3 -> el.copy(x = 242f, w = 102f)
        }
    }

    fun snapAngle(rawDeg: Float, snapDeg: Float = 4f): Float {
        var norm = ((rawDeg + 180f) % 360f + 360f) % 360f - 180f
        val nearest = (norm / 45f).roundToInt() * 45f
        if (abs(norm - nearest) <= snapDeg) {
            norm = if (nearest == 180f || nearest == -180f) 180f else nearest
        }
        return norm.coerceIn(-180f, 180f)
    }
}
