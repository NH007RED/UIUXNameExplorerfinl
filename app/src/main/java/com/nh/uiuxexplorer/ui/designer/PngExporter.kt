package com.nh.uiuxexplorer.ui.designer

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.text.font.FontFamily
import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.model.FrameStyle
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Renders a complete UI screen layout (360 x 780 design units) at 4x Ultra-HD resolution (1440 x 3120 px)
 * with tiny repeated semi-transparent watermarks ("chhoti chhoti transparent watermark har jagah")
 * into a lossless PNG in the device's Gallery (Pictures/UIUXExplorer) or shares it directly.
 */
object PngExporter {
    private const val SCALE = 4f // 360x780 -> 1440x3120 Ultra-HD
    private const val BEZEL_RATIO = 0.028f
    private const val OUTER_CORNER_RATIO = 0.115f

    suspend fun saveToGallery(
        context: Context,
        screenName: String,
        elements: List<DesignElement>,
        theme: StyleTheme,
        frameStyle: FrameStyle,
        includePhoneFrame: Boolean,
        shareAfterSave: Boolean = false
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            WatermarkGuard.verifyAppIntegrity(context)
            val bitmap = renderHighResBitmap(
                elements = elements,
                theme = theme,
                frameStyle = if (includePhoneFrame) frameStyle else FrameStyle.NONE
            )

            val safeName = screenName.trim()
                .replace(Regex("[^a-zA-Z0-9_\\- ]"), "")
                .replace(' ', '_')
                .ifBlank { "UI_Design" }
            val fileName = "${safeName}_${System.currentTimeMillis()}.png"

            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UIUXExplorer")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create MediaStore entry")

            resolver.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    error("Failed to encode PNG")
                }
            } ?: error("Could not open output stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val doneValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
            }

            if (shareAfterSave) {
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, screenName)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share UI Design PNG"))
                }
            }
            uri
        }
    }

    fun renderHighResBitmap(
        elements: List<DesignElement>,
        theme: StyleTheme,
        frameStyle: FrameStyle
    ): Bitmap {
        val screenW = DesignerDefaults.W * SCALE
        val screenH = DesignerDefaults.H * SCALE
        val bezel = if (frameStyle == FrameStyle.NONE) 0f else screenW * BEZEL_RATIO
        val totalW = (screenW + bezel * 2f).toInt()
        val totalH = (screenH + bezel * 2f).toInt()

        val bitmap = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val outerRadius = screenW * OUTER_CORNER_RATIO
        val innerRadius = if (frameStyle == FrameStyle.NONE) 18f * SCALE else max(4f * SCALE, outerRadius - bezel)

        // 1. Optional phone body bezel
        if (frameStyle != FrameStyle.NONE) {
            val bodyRect = RectF(0f, 0f, totalW.toFloat(), totalH.toFloat())
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.FILL
                this.color = 0xFF0E0E12.toInt()
            }
            canvas.drawRoundRect(bodyRect, outerRadius, outerRadius, bodyPaint)

            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                this.strokeWidth = 1.5f * SCALE
                this.color = 0xFF3A3A44.toInt()
            }
            val inset = rimPaint.strokeWidth / 2f
            canvas.drawRoundRect(
                RectF(inset, inset, totalW - inset, totalH - inset),
                outerRadius,
                outerRadius,
                rimPaint
            )
        }

        // 2. Clip to inner screen canvas
        canvas.save()
        canvas.translate(bezel, bezel)
        val screenRect = RectF(0f, 0f, screenW, screenH)
        val clipPath = Path().apply {
            addRoundRect(screenRect, innerRadius, innerRadius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        // 3. Draw style background gradient / solid fill
        val bgColors = theme.backgroundColors.map { it.toArgb() }.toIntArray()
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            this.shader = if (bgColors.size >= 2) {
                LinearGradient(0f, 0f, screenW, screenH, bgColors, null, Shader.TileMode.CLAMP)
            } else {
                null
            }
            if (bgColors.size == 1) this.color = bgColors[0]
        }
        canvas.drawRect(screenRect, bgPaint)

        // 4. Draw each element in z-order
        elements.forEach { el ->
            drawElement(canvas, el, theme, SCALE)
        }

        // 5. Draw phone status bar & camera cut-out when frameStyle != NONE
        if (frameStyle != FrameStyle.NONE) {
            drawStatusOverlay(canvas, screenW, frameStyle, theme, SCALE)
        }

        // 6. Draw tiny repeated transparent watermarks across the entire screen ("chhoti chhoti transparent watermark har jagah")
        drawTiledMicroWatermark(canvas, screenW, screenH, theme.lightContent, SCALE)

        canvas.restore()
        return bitmap
    }

    /**
     * Draws a diagonal grid of tiny, semi-transparent watermarks across the entire canvas
     * plus a subtle bottom-right micro-badge so the watermark is present everywhere without obscuring the UI design.
     */
    fun drawTiledMicroWatermark(
        canvas: Canvas,
        widthPx: Float,
        heightPx: Float,
        lightContent: Boolean,
        scaleFactor: Float
    ) {
        val wmText = WatermarkGuard.resolvePngWatermark()
        if (wmText.isBlank()) return

        val opacity = WatermarkGuard.resolvePngOpacity()
        val densityLevel = WatermarkGuard.resolvePngDensity() // 1=Sparse, 2=Standard, 3=Dense
        val primaryAlpha = (opacity * 255f).roundToInt().coerceIn(18, 120)
        val shadowAlpha = (opacity * 140f).roundToInt().coerceIn(10, 80)

        val fgColor = if (lightContent) {
            android.graphics.Color.argb(primaryAlpha, 255, 255, 255)
        } else {
            android.graphics.Color.argb(primaryAlpha, 15, 20, 32)
        }
        val haloColor = if (lightContent) {
            android.graphics.Color.argb(shadowAlpha, 0, 0, 0)
        } else {
            android.graphics.Color.argb(shadowAlpha, 255, 255, 255)
        }

        val fontSizePx = 7.5f * scaleFactor
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = fgColor
            this.textSize = fontSizePx
            this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            this.letterSpacing = 0.05f
            setShadowLayer(1.2f * scaleFactor, 0.5f * scaleFactor, 0.5f * scaleFactor, haloColor)
        }

        val textWidth = textPaint.measureText(wmText).coerceAtLeast(40f * scaleFactor)
        val gapX = when (densityLevel) {
            1 -> textWidth + 72f * scaleFactor
            3 -> textWidth + 26f * scaleFactor
            else -> textWidth + 44f * scaleFactor
        }
        val gapY = when (densityLevel) {
            1 -> 90f * scaleFactor
            3 -> 44f * scaleFactor
            else -> 62f * scaleFactor
        }

        val diag = hypot(widthPx.toDouble(), heightPx.toDouble()).toFloat()
        canvas.save()
        canvas.rotate(-24f, widthPx / 2f, heightPx / 2f)

        val startX = (widthPx / 2f) - diag
        val endX = (widthPx / 2f) + diag
        val startY = (heightPx / 2f) - diag
        val endY = (heightPx / 2f) + diag

        var rowIdx = 0
        var y = startY
        while (y <= endY) {
            val rowOffset = if (rowIdx % 2 == 1) gapX * 0.5f else 0f
            var x = startX + rowOffset
            while (x <= endX) {
                canvas.drawText(wmText, x, y, textPaint)
                x += gapX
            }
            y += gapY
            rowIdx++
        }
        canvas.restore()

        // Bottom-right subtle corner stamp
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = fgColor
            this.textSize = 6.8f * scaleFactor
            this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(1f * scaleFactor, 0f, 0.5f * scaleFactor, haloColor)
        }
        val fw = footerPaint.measureText(wmText)
        canvas.drawText(
            wmText,
            (widthPx - fw - 12f * scaleFactor).coerceAtLeast(6f * scaleFactor),
            heightPx - 8f * scaleFactor,
            footerPaint
        )
    }

    private fun drawElement(canvas: Canvas, el: DesignElement, t: StyleTheme, s: Float) {
        val left = el.x * s
        val top = el.y * s
        val width = el.w * s
        val height = el.h * s
        val cx = left + width / 2f
        val cy = top + height / 2f

        canvas.save()
        if (el.rotation != 0f) {
            canvas.rotate(el.rotation, cx, cy)
        }
        canvas.translate(left, top)

        val rect = RectF(0f, 0f, width, height)
        val tf = typefaceFor(t.font, t.heavy || el.type == ElementType.HEADLINE || el.type == ElementType.BUTTON || el.type == ElementType.CARD || el.type == ElementType.TOP_BAR)

        when (el.type) {
            ElementType.HEADLINE -> {
                val fitFs = min(el.fontSize, max(8f, el.h * 0.72f)) * s
                val textColor = t.resolveTextColor(el.colorKey, t.onSurface).toArgb()
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = width,
                    height = height,
                    textSizePx = fitFs,
                    color = textColor,
                    typeface = Typeface.create(tf, Typeface.BOLD),
                    maxLines = 2,
                    centerVertically = true,
                    centerHorizontally = false
                )
            }
            ElementType.PARAGRAPH -> {
                val fitFs = (if (el.h < 28f) min(el.fontSize, max(8f, el.h * 0.68f)) else el.fontSize) * s
                val textColor = t.resolveTextColor(el.colorKey, t.muted).toArgb()
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = width,
                    height = height,
                    textSizePx = fitFs,
                    color = textColor,
                    typeface = Typeface.create(tf, Typeface.NORMAL),
                    maxLines = 8,
                    centerVertically = el.h < 32f,
                    centerHorizontally = false
                )
            }
            ElementType.EXPLAIN_TEXT -> {
                val accentCol = t.resolveTextColor(el.colorKey, t.iconAccent)
                val textCol = t.resolveTextColor(el.colorKey, t.onSurface)
                val compact = el.h < 42f
                val cornerR = 10f * s
                val boxPath = Path().apply { addRoundRect(rect, cornerR, cornerR, Path.Direction.CW) }

                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = t.surface.copy(alpha = 0.94f).toArgb()
                }
                canvas.drawPath(boxPath, fillPaint)

                canvas.save()
                canvas.clipPath(boxPath)
                val stripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = accentCol.toArgb()
                }
                val stripeW = max(2f * s, 4f * s)
                canvas.drawRect(0f, 0f, stripeW, height, stripePaint)

                val iconUnits = if (compact) min(13f, max(9f, el.h * 0.5f)) else 15f
                val iconSize = iconUnits * s
                val hPad = (if (compact) 6f else 8f) * s
                val vPad = (if (compact) 1.5f else 5f) * s
                val iconX = stripeW + hPad
                val iconY = if (compact) (height - iconSize) / 2f else vPad + 1.5f * s
                drawCatalogIcon(canvas, el.icon.ifBlank { "info" }, iconX, iconY, iconSize, accentCol.toArgb())

                val textLeft = iconX + iconSize + 5f * s
                val textW = max(10f, width - textLeft - hPad)
                val fitFs = (if (compact) min(el.fontSize, max(8f, el.h * 0.56f)) else el.fontSize) * s
                canvas.save()
                canvas.translate(textLeft, 0f)
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = textW,
                    height = height,
                    textSizePx = fitFs,
                    color = textCol.toArgb(),
                    typeface = Typeface.create(tf, if (compact) Typeface.BOLD else Typeface.NORMAL),
                    maxLines = if (compact) 1 else 6,
                    centerVertically = compact,
                    centerHorizontally = false,
                    topPadPx = if (compact) 0f else vPad
                )
                canvas.restore()
                canvas.restore()

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = max(1f, 1.2f * s)
                    this.color = accentCol.copy(alpha = 0.6f).toArgb()
                }
                canvas.drawRoundRect(rect, cornerR, cornerR, borderPaint)
            }
            ElementType.BUTTON -> {
                drawStyledBox(canvas, rect, t, s, fill = t.accent, radiusUnits = t.radius)
                val fitFs = min(el.fontSize, max(8f, el.h * 0.55f)) * s
                val textColor = t.resolveTextColor(el.colorKey, t.onAccent).toArgb()
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = width,
                    height = height,
                    textSizePx = fitFs,
                    color = textColor,
                    typeface = Typeface.create(tf, Typeface.BOLD),
                    maxLines = 1,
                    centerVertically = true,
                    centerHorizontally = true
                )
            }
            ElementType.CARD -> {
                drawStyledBox(canvas, rect, t, s, fill = t.surface, radiusUnits = t.radius)
                val pad = min(12f, el.h * 0.14f) * s
                val fitFs = min(el.fontSize, max(8f, el.h * 0.45f)) * s
                val titleColor = t.resolveTextColor(el.colorKey, t.onSurface).toArgb()
                canvas.save()
                canvas.translate(pad, pad)
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = max(10f, width - pad * 2f),
                    height = max(10f, height - pad * 2f),
                    textSizePx = fitFs,
                    color = titleColor,
                    typeface = Typeface.create(tf, Typeface.BOLD),
                    maxLines = 2,
                    centerVertically = false,
                    centerHorizontally = false
                )
                val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = t.muted.copy(alpha = 0.35f).toArgb()
                }
                val barH = 7f * s
                if (el.h >= 54f) {
                    val y1 = fitFs * 1.6f
                    canvas.drawRoundRect(RectF(0f, y1, width - pad * 2f, y1 + barH), barH / 2f, barH / 2f, barPaint)
                }
                if (el.h >= 76f) {
                    val y2 = fitFs * 1.6f + barH + 6f * s
                    canvas.drawRoundRect(RectF(0f, y2, (width - pad * 2f) * 0.7f, y2 + barH), barH / 2f, barH / 2f, barPaint)
                }
                canvas.restore()
            }
            ElementType.IMAGE -> {
                val customBmp = ImageStorage.loadBitmap(el.imageUri)
                val rPx = t.radius * s
                if (customBmp != null) {
                    drawStyledShadowAndBorderOnly(canvas, rect, t, s, rPx)
                    val clip = Path().apply { addRoundRect(rect, rPx, rPx, Path.Direction.CW) }
                    canvas.save()
                    canvas.clipPath(clip)
                    if (!el.imageCrop) {
                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            this.style = Paint.Style.FILL
                            this.color = t.surface.toArgb()
                        }
                        canvas.drawRect(rect, bgPaint)
                    }
                    val srcW = customBmp.width.toFloat().coerceAtLeast(1f)
                    val srcH = customBmp.height.toFloat().coerceAtLeast(1f)
                    val scaleFactor = if (el.imageCrop) max(width / srcW, height / srcH) else min(width / srcW, height / srcH)
                    val dx = (width - srcW * scaleFactor) / 2f
                    val dy = (height - srcH * scaleFactor) / 2f
                    val m = Matrix().apply {
                        postScale(scaleFactor, scaleFactor)
                        postTranslate(dx, dy)
                    }
                    val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(customBmp, m, bmpPaint)
                    canvas.restore()
                    drawStyledBorder(canvas, rect, t, s, rPx)
                } else {
                    val baseCol = t.resolveTextColor(el.colorKey, t.accent)
                    drawStyledBox(
                        canvas = canvas,
                        rect = rect,
                        t = t,
                        s = s,
                        fill = baseCol,
                        gradientEnd = baseCol.copy(alpha = 0.35f),
                        radiusUnits = t.radius
                    )
                    val iconSize = min(40f, min(el.w, el.h) * 0.45f) * s
                    drawCatalogIcon(
                        canvas = canvas,
                        key = el.icon.ifBlank { "image" },
                        x = (width - iconSize) / 2f,
                        y = (height - iconSize) / 2f,
                        size = iconSize,
                        colorArgb = t.onAccent.copy(alpha = 0.85f).toArgb()
                    )
                }
            }
            ElementType.INPUT -> {
                drawStyledBox(canvas, rect, t, s, fill = t.surface, radiusUnits = t.radius)
                val inputInk = t.resolveTextColor(el.colorKey, t.muted).toArgb()
                val padX = 10f * s
                val iconSize = min(18f, el.h * 0.5f) * s
                drawCatalogIcon(
                    canvas = canvas,
                    key = el.icon.ifBlank { "search" },
                    x = padX,
                    y = (height - iconSize) / 2f,
                    size = iconSize,
                    colorArgb = inputInk
                )
                val textLeft = padX + iconSize + 6f * s
                val fitFs = min(el.fontSize, max(8f, el.h * 0.55f)) * s
                canvas.save()
                canvas.translate(textLeft, 0f)
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = max(10f, width - textLeft - padX),
                    height = height,
                    textSizePx = fitFs,
                    color = inputInk,
                    typeface = tf,
                    maxLines = 1,
                    centerVertically = true,
                    centerHorizontally = false
                )
                canvas.restore()
            }
            ElementType.CHIP -> {
                drawStyledBox(canvas, rect, t, s, fill = t.surface, radiusUnits = el.h / 2f)
                val fitFs = min(el.fontSize, max(8f, el.h * 0.58f)) * s
                val textColor = t.resolveTextColor(el.colorKey, t.onSurface).toArgb()
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = width,
                    height = height,
                    textSizePx = fitFs,
                    color = textColor,
                    typeface = tf,
                    maxLines = 1,
                    centerVertically = true,
                    centerHorizontally = true
                )
            }
            ElementType.SWITCH -> {
                val activeFill = t.resolveTextColor(el.colorKey, t.accent)
                drawStyledBox(canvas, rect, t, s, fill = activeFill, radiusUnits = el.h / 2f)
                val pad = 3f * s
                val r = (height - pad * 2f) / 2f
                val knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = android.graphics.Color.WHITE
                }
                canvas.drawCircle(width - pad - r, height / 2f, r, knobPaint)
            }
            ElementType.FAB -> {
                drawStyledBox(canvas, rect, t, s, fill = t.accent, radiusUnits = min(el.w, el.h) / 2f)
                val iconSize = min(26f, min(el.w, el.h) * 0.5f) * s
                drawCatalogIcon(
                    canvas = canvas,
                    key = el.icon.ifBlank { "add" },
                    x = (width - iconSize) / 2f,
                    y = (height - iconSize) / 2f,
                    size = iconSize,
                    colorArgb = t.resolveTextColor(el.colorKey, t.onAccent).toArgb()
                )
            }
            ElementType.TOP_BAR -> {
                drawStyledBox(canvas, rect, t, s, fill = t.surface, radiusUnits = 0f)
                val (leftKey, rightKey) = DesignerDefaults.topBarIcons(el)
                val barInk = t.resolveTextColor(el.colorKey, t.onSurface).toArgb()
                val padX = 12f * s
                val iconSize = min(22f, el.h * 0.5f) * s
                val iconY = (height - iconSize) / 2f
                drawCatalogIcon(canvas, leftKey, padX, iconY, iconSize, barInk)
                drawCatalogIcon(canvas, rightKey, width - padX - iconSize, iconY, iconSize, barInk)
                val titleLeft = padX + iconSize + 12f * s
                val titleW = max(10f, width - titleLeft - padX - iconSize - 8f * s)
                val fitFs = min(el.fontSize, max(8f, el.h * 0.52f)) * s
                canvas.save()
                canvas.translate(titleLeft, 0f)
                drawMultilineText(
                    canvas = canvas,
                    text = el.text,
                    width = titleW,
                    height = height,
                    textSizePx = fitFs,
                    color = barInk,
                    typeface = Typeface.create(tf, Typeface.BOLD),
                    maxLines = 1,
                    centerVertically = true,
                    centerHorizontally = false
                )
                canvas.restore()
            }
            ElementType.ICON -> {
                val iconSize = min(width, height)
                drawCatalogIcon(
                    canvas = canvas,
                    key = el.icon.ifBlank { "star" },
                    x = (width - iconSize) / 2f,
                    y = (height - iconSize) / 2f,
                    size = iconSize,
                    colorArgb = t.tintFor(el.colorKey).toArgb()
                )
            }
            ElementType.BOTTOM_NAV -> {
                drawStyledBox(canvas, rect, t, s, fill = t.surface, radiusUnits = 0f)
                val navKeys = DesignerDefaults.bottomNavIcons(el)
                val activeTint = t.resolveTextColor(el.colorKey, t.iconAccent).toArgb()
                val mutedTint = t.muted.toArgb()
                val iconSize = min(24f, el.h * 0.5f) * s
                val slotW = width / navKeys.size.coerceAtLeast(1)
                navKeys.forEachIndexed { idx, key ->
                    val ix = slotW * idx + (slotW - iconSize) / 2f
                    val iy = (height - iconSize) / 2f
                    drawCatalogIcon(canvas, key, ix, iy, iconSize, if (idx == 0) activeTint else mutedTint)
                }
            }
        }

        canvas.restore()
    }

    private fun drawStyledBox(
        canvas: Canvas,
        rect: RectF,
        t: StyleTheme,
        s: Float,
        fill: Color,
        gradientEnd: Color? = null,
        radiusUnits: Float = t.radius
    ) {
        val rPx = min(radiusUnits * s, min(rect.width(), rect.height()) / 2f)
        drawStyledShadowAndBorderOnly(canvas, rect, t, s, rPx)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            if (gradientEnd != null) {
                this.shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    fill.toArgb(), gradientEnd.toArgb(), Shader.TileMode.CLAMP
                )
            } else {
                this.color = fill.toArgb()
            }
        }
        canvas.drawRoundRect(rect, rPx, rPx, fillPaint)
        drawStyledBorder(canvas, rect, t, s, rPx)
    }

    private fun drawStyledShadowAndBorderOnly(canvas: Canvas, rect: RectF, t: StyleTheme, s: Float, rPx: Float) {
        when (t.shadow) {
            ShadowKind.HARD -> {
                val o = 6f * s
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = t.shadowColor.toArgb()
                }
                canvas.drawRoundRect(RectF(rect.left + o, rect.top + o, rect.right + o, rect.bottom + o), rPx, rPx, shadowPaint)
            }
            ShadowKind.SOFT -> {
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = t.surface.toArgb()
                    setShadowLayer(8f * s, 0f, 3f * s, t.shadowColor.toArgb())
                }
                canvas.drawRoundRect(rect, rPx, rPx, shadowPaint)
            }
            ShadowKind.NEU -> {
                val d = 5f * s
                val p1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = t.surface.toArgb()
                    setShadowLayer(d * 1.5f, -d, -d, android.graphics.Color.WHITE)
                }
                canvas.drawRoundRect(rect, rPx, rPx, p1)
                val p2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.FILL
                    this.color = t.surface.toArgb()
                    setShadowLayer(d * 1.5f, d, d, 0xFFA3B1C6.toInt())
                }
                canvas.drawRoundRect(rect, rPx, rPx, p2)
            }
            ShadowKind.NONE -> {}
        }
    }

    private fun drawStyledBorder(canvas: Canvas, rect: RectF, t: StyleTheme, s: Float, rPx: Float) {
        val bc = t.borderColor
        if (bc != null && t.borderWidth > 0f) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                this.strokeWidth = max(1f, t.borderWidth * s)
                this.color = bc.toArgb()
            }
            canvas.drawRoundRect(rect, rPx, rPx, borderPaint)
        }
    }

    private fun drawStatusOverlay(canvas: Canvas, screenW: Float, frameStyle: FrameStyle, theme: StyleTheme, s: Float) {
        val ink = if (theme.lightContent) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        val timePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = ink
            this.textSize = 10.5f * s
            this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val barH = 24f * s
        val fm = timePaint.fontMetrics
        val textY = (barH - (fm.descent - fm.ascent)) / 2f - fm.ascent
        canvas.drawText("9:41", 18f * s, textY, timePaint)

        // Battery icon
        val battW = 17f * s
        val battH = 9f * s
        val battX = screenW - 18f * s - battW
        val battY = (barH - battH) / 2f
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.STROKE
            this.strokeWidth = 1f * s
            this.color = ink
        }
        canvas.drawRoundRect(RectF(battX, battY, battX + battW, battY + battH), 2.2f * s, 2.2f * s, strokePaint)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            this.color = ink
        }
        val pad = 1.5f * s
        canvas.drawRoundRect(
            RectF(battX + pad, battY + pad, battX + pad + (battW - pad * 2f) * 0.8f, battY + battH - pad),
            1f * s, 1f * s, fillPaint
        )

        // Wi-Fi icon
        val wifiSize = 12f * s
        drawCatalogIcon(canvas, "wifi", battX - 4f * s - wifiSize, (barH - wifiSize) / 2f, wifiSize, ink)

        // Camera punch-hole or notch
        val cutoutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            this.color = android.graphics.Color.BLACK
        }
        when (frameStyle) {
            FrameStyle.PUNCH_HOLE -> {
                val r = 3.8f * s
                canvas.drawCircle(screenW / 2f, 8f * s + r, r, cutoutPaint)
            }
            FrameStyle.NOTCH -> {
                val nw = 102f * s
                val nh = 19f * s
                val nr = 11f * s
                canvas.drawRoundRect(RectF((screenW - nw) / 2f, -nr, (screenW + nw) / 2f, nh), nr, nr, cutoutPaint)
            }
            FrameStyle.NONE -> {}
        }
    }

    private fun drawCatalogIcon(canvas: Canvas, key: String, x: Float, y: Float, size: Float, colorArgb: Int) {
        val iv: ImageVector = IconCatalog.get(key)
        val vpW = iv.viewportWidth.takeIf { it > 0f } ?: 24f
        val vpH = iv.viewportHeight.takeIf { it > 0f } ?: 24f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
            this.color = colorArgb
        }
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(size / vpW, size / vpH)
        drawVectorNode(iv.root, canvas, paint)
        canvas.restore()
    }

    private fun drawVectorNode(node: VectorNode, canvas: Canvas, paint: Paint) {
        when (node) {
            is VectorPath -> {
                val composePath = node.pathData.toPath()
                val androidPath = composePath.asAndroidPath()
                androidPath.fillType = if (node.pathFillType.toString().contains("EvenOdd", ignoreCase = true)) {
                    Path.FillType.EVEN_ODD
                } else {
                    Path.FillType.WINDING
                }
                canvas.drawPath(androidPath, paint)
            }
            is VectorGroup -> {
                canvas.save()
                canvas.translate(node.translationX, node.translationY)
                canvas.rotate(node.rotation, node.pivotX, node.pivotY)
                canvas.scale(node.scaleX, node.scaleY, node.pivotX, node.pivotY)
                for (child in node) {
                    drawVectorNode(child, canvas, paint)
                }
                canvas.restore()
            }
        }
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        width: Float,
        height: Float,
        textSizePx: Float,
        color: Int,
        typeface: Typeface,
        maxLines: Int,
        centerVertically: Boolean,
        centerHorizontally: Boolean,
        topPadPx: Float = 0f
    ) {
        if (text.isBlank() || width <= 2f || height <= 2f) return
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSizePx
            this.typeface = typeface
        }
        val align = if (centerHorizontally) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, tp, width.toInt().coerceAtLeast(1))
            .setAlignment(align)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .build()

        val dy = if (centerVertically) {
            ((height - layout.height) / 2f).coerceAtLeast(0f)
        } else {
            topPadPx
        }
        canvas.save()
        canvas.translate(0f, dy)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun typefaceFor(font: FontFamily, bold: Boolean): Typeface {
        val base = when (font) {
            FontFamily.Monospace -> Typeface.MONOSPACE
            FontFamily.Serif -> Typeface.SERIF
            else -> Typeface.SANS_SERIF
        }
        return Typeface.create(base, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
}
