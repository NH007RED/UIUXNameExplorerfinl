package com.nh.uiuxexplorer.ui.designer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.nh.uiuxexplorer.domain.model.FrameStyle

private const val BEZEL = 0.028f
private const val CORNER = 0.115f

fun frameBezel(canvasWidth: Dp, style: FrameStyle): Dp = if (style == FrameStyle.NONE) 0.dp else canvasWidth * BEZEL
fun frameWidth(canvasWidth: Dp, style: FrameStyle): Dp = canvasWidth + frameBezel(canvasWidth, style) * 2f
fun frameHeight(canvasWidth: Dp, style: FrameStyle): Dp = canvasWidth * (780f / 360f) + frameBezel(canvasWidth, style) * 2f

/**
 * A slim modern phone body (bezel, rounded corners) around the screen canvas. [content] receives the corner radius the
 * canvas should use and an overlay slot that draws the status bar and camera cut-out on top of the screen.
 */
@Composable
fun PhoneFrame(
    canvasWidth: Dp,
    style: FrameStyle,
    theme: StyleTheme,
    modifier: Modifier = Modifier,
    content: @Composable (cornerDp: Dp, overlay: @Composable BoxScope.() -> Unit) -> Unit
) {
    val bezel = frameBezel(canvasWidth, style)
    val outer = canvasWidth * CORNER
    val inner = if (style == FrameStyle.NONE) 18.dp else (outer - bezel).coerceAtLeast(4.dp)
    val body = if (style == FrameStyle.NONE) Modifier else
        Modifier
            .background(Color(0xFF0E0E12), RoundedCornerShape(outer))
            .border(1.dp, Color(0xFF3A3A44), RoundedCornerShape(outer))
    Box(
        modifier.size(frameWidth(canvasWidth, style), frameHeight(canvasWidth, style)).then(body).padding(bezel)
    ) {
        content(inner) { StatusOverlay(canvasWidth, style, theme) }
    }
}

@Composable
private fun BoxScope.StatusOverlay(canvasWidth: Dp, style: FrameStyle, theme: StyleTheme) {
    if (style == FrameStyle.NONE) return
    val s = canvasWidth.value / 360f
    val ink = if (theme.lightContent) Color.White else Color.Black
    val fontSize = with(LocalDensity.current) { (10.5f * s).dp.toSp() }
    Row(
        Modifier.align(Alignment.TopCenter).fillMaxWidth().height((24f * s).dp).padding(horizontal = (18f * s).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("9:41", color = ink, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.Wifi, null, tint = ink, modifier = Modifier.size((12f * s).dp))
        Spacer(Modifier.width((4f * s).dp))
        Box(
            Modifier.size((17f * s).dp, (9f * s).dp)
                .border((1f * s).dp.coerceAtLeast(0.5.dp), ink, RoundedCornerShape((2.2f * s).dp))
                .padding((1.3f * s).dp)
        ) { Box(Modifier.fillMaxHeight().fillMaxWidth(0.8f).background(ink, RoundedCornerShape((1f * s).dp))) }
    }
    when (style) {
        FrameStyle.PUNCH_HOLE -> Box(
            Modifier.align(Alignment.TopCenter).padding(top = (8f * s).dp).size((7.5f * s).dp).background(Color.Black, CircleShape)
        )
        FrameStyle.NOTCH -> Box(
            Modifier.align(Alignment.TopCenter).size((102f * s).dp, (19f * s).dp)
                .background(Color.Black, RoundedCornerShape(0.dp, 0.dp, (12f * s).dp, (12f * s).dp))
        )
        FrameStyle.NONE -> {}
    }
}

/** Slim fitted canvas width whose whole phone frame fits inside the given area with clean margins. */
fun fitCanvasWidth(availW: Dp, availH: Dp, style: FrameStyle): Dp {
    val b = if (style == FrameStyle.NONE) 0f else BEZEL * 2f
    val rawFit = minOf(availW / (1f + b), availH / (780f / 360f + b))
    return rawFit * 0.96f
}
