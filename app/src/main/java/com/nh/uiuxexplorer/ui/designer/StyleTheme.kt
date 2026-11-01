package com.nh.uiuxexplorer.ui.designer

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.ui.common.parseHex

enum class ShadowKind { NONE, SOFT, HARD, NEU }

data class ColorSwatchItem(val key: String, val label: String, val previewColor: Color?)

val DESIGNER_COLOR_SWATCHES: List<ColorSwatchItem> = listOf(
    ColorSwatchItem("default", "Auto", null),
    ColorSwatchItem("theme_accent", "Accent", null),
    ColorSwatchItem("text", "Text", null),
    ColorSwatchItem("muted", "Muted", null),
    ColorSwatchItem("#E50914", "Red", Color(0xFFE50914)),
    ColorSwatchItem("#FF2E63", "Crimson", Color(0xFFFF2E63)),
    ColorSwatchItem("#FF6B00", "Orange", Color(0xFFFF6B00)),
    ColorSwatchItem("#FFB800", "Gold", Color(0xFFFFB800)),
    ColorSwatchItem("#00C853", "Green", Color(0xFF00C853)),
    ColorSwatchItem("#00BFA5", "Teal", Color(0xFF00BFA5)),
    ColorSwatchItem("#00E5FF", "Cyan", Color(0xFF00E5FF)),
    ColorSwatchItem("#2979FF", "Blue", Color(0xFF2979FF)),
    ColorSwatchItem("#651FFF", "Indigo", Color(0xFF651FFF)),
    ColorSwatchItem("#AA00FF", "Purple", Color(0xFFAA00FF)),
    ColorSwatchItem("#FF4081", "Pink", Color(0xFFFF4081)),
    ColorSwatchItem("white", "White", Color.White),
    ColorSwatchItem("#B0BEC5", "Silver", Color(0xFFB0BEC5)),
    ColorSwatchItem("black", "Black", Color.Black)
)

/** Visual rules used to draw designer elements in the chosen library style. Illustrative, not a pixel-perfect spec. */
class StyleTheme(
    val background: Brush,
    val surface: Color,
    val onSurface: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val radius: Float,
    val borderColor: Color? = null,
    val borderWidth: Float = 0f,
    val shadow: ShadowKind = ShadowKind.NONE,
    val shadowColor: Color = Color(0x33000000),
    val font: FontFamily = FontFamily.SansSerif,
    val heavy: Boolean = false,
    val accentBrush: Brush? = null,
    val backgroundColors: List<Color> = listOf(Color(0xFFF7F8FC), Color(0xFFF7F8FC))
) {
    /** True when the screen background is dark, so status-bar content should be white. */
    val lightContent: Boolean get() = onSurface.luminance() > 0.5f

    /** An accent color that stays visible on this style's surfaces (glass/neumorphic accents are not). */
    val iconAccent: Color
        get() = when {
            accent == surface -> onAccent
            accent.alpha > 0.6f -> accent
            else -> onSurface
        }

    fun tintFor(key: String): Color = when {
        key == "text" -> onSurface
        key == "muted" -> muted
        key == "white" -> Color.White
        key == "black" -> Color.Black
        key == "theme_accent" || key == "accent" || key.isBlank() || key == "default" -> iconAccent
        key.startsWith("#") -> parseHex(key)
        else -> iconAccent
    }

    /** Resolves custom text or accent color for any element while keeping [defaultColor] when set to Auto ("default"/"accent"). */
    fun resolveTextColor(key: String, defaultColor: Color): Color = when {
        key.isBlank() || key == "default" || key == "accent" -> defaultColor
        key == "theme_accent" -> iconAccent
        key == "text" -> onSurface
        key == "muted" -> muted
        key == "white" -> Color.White
        key == "black" -> Color.Black
        key.startsWith("#") -> parseHex(key)
        else -> defaultColor
    }
}

private fun solidList(c: Long) = listOf(Color(c), Color(c))
private fun gradList(vararg c: Long) = c.map { Color(it) }
private fun solid(c: Long) = Brush.verticalGradient(solidList(c))
private fun grad(vararg c: Long) = Brush.linearGradient(gradList(*c))

fun styleThemeFor(entry: DesignEntry?): StyleTheme = when (entry?.preview) {
    "glass" -> StyleTheme(
        grad(0xFF5B2EFF, 0xFF00C2D1, 0xFFFF4D9D), Color.White.copy(alpha = 0.18f), Color.White,
        Color.White.copy(alpha = 0.78f), Color.White.copy(alpha = 0.30f), Color.White, 20f,
        Color.White.copy(alpha = 0.45f), 1f,
        backgroundColors = gradList(0xFF5B2EFF, 0xFF00C2D1, 0xFFFF4D9D)
    )
    "liquid" -> StyleTheme(
        grad(0xFF0B1B3A, 0xFF3A1C71, 0xFF0A84FF), Color.White.copy(alpha = 0.14f), Color.White,
        Color.White.copy(alpha = 0.78f), Color.White.copy(alpha = 0.28f), Color.White, 28f,
        Color.White.copy(alpha = 0.5f), 1f,
        backgroundColors = gradList(0xFF0B1B3A, 0xFF3A1C71, 0xFF0A84FF)
    )
    "neumorph" -> StyleTheme(
        solid(0xFFE0E5EC), Color(0xFFE0E5EC), Color(0xFF4A5568), Color(0xFF7B8AA3),
        Color(0xFFE0E5EC), Color(0xFF3F6FD6), 22f, shadow = ShadowKind.NEU,
        backgroundColors = solidList(0xFFE0E5EC)
    )
    "material" -> StyleTheme(
        solid(0xFFFEF7FF), Color(0xFFF3EDF7), Color(0xFF1D1B20), Color(0xFF49454F),
        Color(0xFF6750A4), Color.White, 16f,
        backgroundColors = solidList(0xFFFEF7FF)
    )
    "minimal" -> StyleTheme(
        solid(0xFFFFFFFF), Color.White, Color(0xFF111111), Color(0xFF666666),
        Color(0xFF111111), Color.White, 2f, Color(0xFF111111), 1f,
        backgroundColors = solidList(0xFFFFFFFF)
    )
    "cyber" -> StyleTheme(
        solid(0xFF0B0014), Color(0xFF00F0FF).copy(alpha = 0.12f), Color(0xFF00F0FF), Color(0xFFFF00E5),
        Color(0xFFFF00E5), Color(0xFF0B0014), 4f, Color(0xFF00F0FF), 1.5f, font = FontFamily.Monospace, heavy = true,
        backgroundColors = solidList(0xFF0B0014)
    )
    "bento" -> StyleTheme(
        solid(0xFFF2F2F7), Color.White, Color(0xFF1A1A1A), Color(0xFF666666),
        Color(0xFF7C4DFF), Color.White, 20f, shadow = ShadowKind.SOFT,
        backgroundColors = solidList(0xFFF2F2F7)
    )
    "flat" -> StyleTheme(
        solid(0xFFECF0F1), Color.White, Color(0xFF2C3E50), Color(0xFF7F8C8D),
        Color(0xFF3498DB), Color.White, 4f,
        backgroundColors = solidList(0xFFECF0F1)
    )
    "brutal" -> StyleTheme(
        solid(0xFFFFF4CC), Color.White, Color.Black, Color(0xFF333333),
        Color(0xFFFF90E8), Color.Black, 0f, Color.Black, 2f, ShadowKind.HARD, Color.Black, heavy = true,
        backgroundColors = solidList(0xFFFFF4CC)
    )
    "clay" -> StyleTheme(
        solid(0xFFFFE8F0), Color.White, Color(0xFF7A2E4D), Color(0xFFA2506F),
        Color(0xFF7FA3FF), Color.White, 30f, shadow = ShadowKind.SOFT, shadowColor = Color(0xFFE57AA0), heavy = true,
        backgroundColors = solidList(0xFFFFE8F0)
    )
    "gradient" -> StyleTheme(
        grad(0xFFFF6B6B, 0xFFFFB86B, 0xFF845EF7), Color.White.copy(alpha = 0.92f), Color(0xFF2A2A40),
        Color(0xFF5A5A75), Color.White, Color(0xFF845EF7), 18f, shadow = ShadowKind.SOFT, heavy = true,
        backgroundColors = gradList(0xFFFF6B6B, 0xFFFFB86B, 0xFF845EF7)
    )
    "aurora" -> StyleTheme(
        grad(0xFF050B1A, 0xFF0B3D4A, 0xFF2A1B5E), Color.White.copy(alpha = 0.10f), Color.White,
        Color.White.copy(alpha = 0.75f), Color(0xFF00FFA3), Color(0xFF050B1A), 20f, Color.White.copy(alpha = 0.3f), 1f,
        backgroundColors = gradList(0xFF050B1A, 0xFF0B3D4A, 0xFF2A1B5E)
    )
    "holo" -> StyleTheme(
        grad(0xFFFFB6F9, 0xFFB8F2FF, 0xFFE0C3FC), Color.White.copy(alpha = 0.55f), Color(0xFF2A2A40),
        Color(0xFF55557A), Color(0xFF7A5CFF), Color.White, 22f, Color.White.copy(alpha = 0.7f), 1f,
        backgroundColors = gradList(0xFFFFB6F9, 0xFFB8F2FF, 0xFFE0C3FC)
    )
    "vapor" -> StyleTheme(
        grad(0xFF2B0A4E, 0xFFB0289A, 0xFFFF3CAC), Color(0xFF1A0533).copy(alpha = 0.6f), Color(0xFF7CF2FF),
        Color(0xFFFFB0E0), Color(0xFFFF3CAC), Color.White, 6f, Color(0xFFFF3CAC), 1f,
        backgroundColors = gradList(0xFF2B0A4E, 0xFFB0289A, 0xFFFF3CAC)
    )
    "retro" -> StyleTheme(
        solid(0xFF008080), Color(0xFFC0C0C0), Color.Black, Color(0xFF333333),
        Color(0xFF000080), Color.White, 0f, Color.Black, 2f, font = FontFamily.Monospace,
        backgroundColors = solidList(0xFF008080)
    )
    "dark" -> StyleTheme(
        background = grad(0xFF060A14, 0xFF0F172A, 0xFF161F38),
        surface = Color(0xFF18223C),
        onSurface = Color(0xFFF8FAFC),
        muted = Color(0xFF94A3B8),
        accent = Color(0xFF38BDF8),
        onAccent = Color.White,
        radius = 18f,
        borderColor = Color(0xFF33497A),
        borderWidth = 1.2f,
        shadow = ShadowKind.SOFT,
        shadowColor = Color(0x66000000),
        accentBrush = grad(0xFF6366F1, 0xFF06B6D4),
        backgroundColors = gradList(0xFF060A14, 0xFF0F172A, 0xFF161F38)
    )
    "skeuo" -> StyleTheme(
        Brush.verticalGradient(listOf(Color(0xFF6B4A2B), Color(0xFF4A3119))), Color(0xFF7A5230), Color(0xFFF7E7C0),
        Color(0xFFD9C08F), Color(0xFFC9A15A), Color(0xFF3B2412), 14f, Color(0xFFE8D3A8), 1f, ShadowKind.SOFT, Color(0xFF000000),
        backgroundColors = gradList(0xFF6B4A2B, 0xFF4A3119)
    )
    "swiss" -> StyleTheme(
        solid(0xFFFFFFFF), Color.White, Color(0xFF212121), Color(0xFF757575),
        Color(0xFFD32F2F), Color.White, 0f, Color(0xFF212121), 1f, heavy = true,
        backgroundColors = solidList(0xFFFFFFFF)
    )
    else -> {
        val accent = entry?.palette?.firstOrNull()?.let { parseHex(it) } ?: Color(0xFF3D5AFE)
        StyleTheme(
            solid(0xFFF7F8FC), Color.White, Color(0xFF14141C), Color(0xFF4A4C5A),
            accent, Color.White, 14f, Color(0xFFD5D8E4), 1f,
            backgroundColors = solidList(0xFFF7F8FC)
        )
    }
}

val THEMED_KEYS = setOf(
    "glass", "liquid", "neumorph", "material", "minimal", "cyber", "bento", "flat", "brutal",
    "clay", "gradient", "aurora", "holo", "vapor", "retro", "dark", "skeuo", "swiss"
)
