package com.nh.uiuxexplorer.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.ui.common.parseHex

private val PreviewHeight = 152.dp

/** Renders a real Compose sample for a design style key. Samples are illustrative, not screenshots. */
@Composable
fun StylePreview(key: String, modifier: Modifier = Modifier, entry: DesignEntry? = null) {
    val m = modifier.fillMaxWidth().height(PreviewHeight).clip(RoundedCornerShape(16.dp))
    when (key) {
        "glass" -> GlassPreview(m)
        "neumorph" -> NeumorphPreview(m)
        "material" -> MaterialPreview(m)
        "liquid" -> LiquidPreview(m)
        "minimal" -> MinimalPreview(m)
        "cyber" -> CyberPreview(m)
        "bento" -> BentoPreview(m)
        "flat" -> FlatPreview(m)
        "brutal" -> BrutalPreview(m)
        "clay" -> ClayPreview(m)
        "gradient" -> GradientPreview(m)
        "aurora" -> AuroraPreview(m, "Aurora UI", Color(0xFF050B1A),
            listOf(Color(0xFF00FFA3), Color(0xFF7A5CFF), Color(0xFF2D9CFF)), Color.White)
        "holo" -> AuroraPreview(m, "Holographic", Color(0xFFF3E8FF),
            listOf(Color(0xFFFFB6F9), Color(0xFFB8F2FF), Color(0xFFFFF3B0)), Color(0xFF2A2A40))
        "vapor" -> VaporPreview(m)
        "retro" -> RetroPreview(m)
        "dark" -> DarkPreview(m)
        "skeuo" -> SkeuoPreview(m)
        "swiss" -> SwissPreview(m)
        else -> ConceptPreview(m, entry)
    }
}

// ---------- Glass ----------
@Composable
private fun GlassPreview(m: Modifier) {
    Box(m.background(Brush.linearGradient(listOf(Color(0xFF5B2EFF), Color(0xFF00C2D1), Color(0xFFFF4D9D))))) {
        Box(Modifier.size(84.dp).offset(12.dp, 4.dp).blur(20.dp).background(Color(0xCCFFD166), CircleShape))
        Box(
            Modifier.size(72.dp).align(Alignment.BottomEnd).offset((-8).dp, (-4).dp).blur(18.dp)
                .background(Color(0xCC7CF2FF), CircleShape)
        )
        val shape = RoundedCornerShape(16.dp)
        Column(
            Modifier.align(Alignment.Center).fillMaxWidth(0.84f).clip(shape)
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.1f))), shape)
                .padding(12.dp)
        ) {
            Text("Glass card", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Translucent layer, soft border, depth.", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassPill("Primary", 0.30f)
                GlassPill("Ghost", 0.08f)
            }
        }
    }
}

@Composable
private fun GlassPill(text: String, alpha: Float) {
    Box(
        Modifier.clip(CircleShape).background(Color.White.copy(alpha = alpha))
            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 11.dp, vertical = 4.dp)
    ) { Text(text, color = Color.White, fontSize = 11.sp) }
}

// ---------- Neumorphism ----------
private val NeuBase = Color(0xFFE0E5EC)

private fun Modifier.neu(radius: Dp = 16.dp, depth: Dp = 5.dp): Modifier = this.drawBehind {
    val r = radius.toPx()
    val d = depth.toPx()
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = NeuBase.toArgb()
    }
    drawIntoCanvas { canvas ->
        paint.setShadowLayer(d * 1.6f, -d, -d, Color.White.toArgb())
        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, paint)
        paint.setShadowLayer(d * 1.6f, d, d, Color(0xFFA3B1C6).toArgb())
        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, paint)
    }
}

@Composable
private fun NeumorphPreview(m: Modifier) {
    Box(m.background(NeuBase), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).neu(18.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Favorite, null, tint = Color(0xFF7B8AA3), modifier = Modifier.size(20.dp))
            }
            Box(Modifier.width(92.dp).height(42.dp).neu(21.dp, 4.dp), contentAlignment = Alignment.Center) {
                Text("Toggle", color = Color(0xFF5B6B82), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Box(Modifier.size(44.dp).neu(22.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Settings, null, tint = Color(0xFF7B8AA3), modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ---------- Material 3 ----------
@Composable
private fun MaterialPreview(m: Modifier) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6750A4), onPrimary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8), onSecondaryContainer = Color(0xFF1D192B),
            surface = Color(0xFFFEF7FF), onSurface = Color(0xFF1D1B20)
        )
    ) {
        var on by remember { mutableStateOf(true) }
        var sel by remember { mutableIntStateOf(0) }
        Surface(m, color = Color(0xFFFEF7FF), contentColor = Color(0xFF1D1B20)) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Text("Material 3 card", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Tonal surfaces, rounded shapes, dynamic color.", fontSize = 10.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {},
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) { Text("Filled", fontSize = 11.sp) }
                    FilledTonalButton(
                        onClick = {},
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) { Text("Tonal", fontSize = 11.sp) }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) { Text("Outline", fontSize = 11.sp) }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = on,
                        onCheckedChange = { on = it },
                        modifier = Modifier.scale(0.75f).height(26.dp)
                    )
                    FilterChip(
                        selected = sel == 0,
                        onClick = { sel = if (sel == 0) 1 else 0 },
                        label = { Text("Chip", fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
        }
    }
}

// ---------- Liquid glass ----------
@Composable
private fun LiquidPreview(m: Modifier) {
    Box(m.background(Brush.linearGradient(listOf(Color(0xFF0B1B3A), Color(0xFF3A1C71), Color(0xFF0A84FF))))) {
        Box(Modifier.size(96.dp).offset(180.dp, (-8).dp).blur(24.dp).background(Color(0xAAFF5EAE), CircleShape))
        Box(Modifier.size(80.dp).offset(8.dp, 68.dp).blur(22.dp).background(Color(0xAA64D2FF), CircleShape))
        val shape = RoundedCornerShape(22.dp)
        Column(
            Modifier.align(Alignment.TopCenter).padding(top = 12.dp).fillMaxWidth(0.86f).clip(shape)
                .drawBehind {
                    drawRect(Color.White.copy(alpha = 0.10f))
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(0f, 0f), radius = size.width * 0.7f
                        )
                    )
                }
                .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.4f))), shape)
                .padding(11.dp)
        ) {
            Text("Liquid Glass", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Fluid, content-aware translucency with specular edges.", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
        val pill = RoundedCornerShape(50)
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp).clip(pill)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.1f))), pill)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.clip(pill).background(Color.White.copy(alpha = 0.30f)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            }
            Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Icon(Icons.Default.Settings, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ---------- Minimal ----------
@Composable
private fun MinimalPreview(m: Modifier) {
    Box(m.background(Color(0xFFFFFFFF))) {
        Column(Modifier.padding(16.dp)) {
            Text("Minimal", color = Color(0xFF111111), fontSize = 22.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = Color(0xFF111111), thickness = 1.dp, modifier = Modifier.width(38.dp))
            Spacer(Modifier.height(6.dp))
            Text("White space, one accent, restrained type.", color = Color(0xFF666666), fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .border(BorderStroke(1.dp, Color(0xFF111111)), RoundedCornerShape(2.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text("EXPLORE", color = Color(0xFF111111), fontSize = 10.sp, letterSpacing = 1.8.sp)
            }
        }
    }
}

// ---------- Cyberpunk ----------
private val CutShape = GenericShape { size, _ ->
    val c = 18f
    moveTo(c, 0f); lineTo(size.width, 0f); lineTo(size.width, size.height - c)
    lineTo(size.width - c, size.height); lineTo(0f, size.height); lineTo(0f, c); close()
}

@Composable
private fun CyberPreview(m: Modifier) {
    Box(
        m.background(Color(0xFF0B0014)).drawBehind {
            val step = 24.dp.toPx()
            var x = 0f
            while (x < size.width) { drawLine(Color(0x2200F0FF), Offset(x, 0f), Offset(x, size.height)); x += step }
            var y = 0f
            while (y < size.height) { drawLine(Color(0x2200F0FF), Offset(0f, y), Offset(size.width, y)); y += step }
        }
    ) {
        Column(
            Modifier.align(Alignment.Center).fillMaxWidth(0.84f)
                .background(Color(0x3300F0FF), CutShape).border(1.5.dp, Color(0xFF00F0FF), CutShape).padding(12.dp)
        ) {
            Text("SYSTEM//ONLINE", color = Color(0xFF00F0FF), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("NEURAL LINK 87%", color = Color(0xFFFF00E5), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(10) { i -> Box(Modifier.width(12.dp).height(6.dp).background(if (i < 8) Color(0xFFFF00E5) else Color(0x33FF00E5))) }
            }
        }
    }
}

// ---------- Bento ----------
@Composable
private fun ColumnScope.Tile(color: Color, label: String, weight: Float = 1f, dark: Boolean = false) {
    Box(
        Modifier.weight(weight).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color).padding(8.dp)
    ) { Text(label, color = if (dark) Color.White else Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 11.sp) }
}

@Composable
private fun BentoPreview(m: Modifier) {
    Row(m.background(Color(0xFFF2F2F7)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Tile(Color(0xFF7C4DFF), "Revenue", 1.4f, dark = true)
            Tile(Color(0xFFFFD166), "Users")
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Tile(Color(0xFF9CF0FA), "Speed")
            Tile(Color(0xFFFFB3C7), "Mood")
            Tile(Color(0xFFB9F6CA), "Goals")
        }
    }
}

// ---------- Flat ----------
@Composable
private fun FlatPreview(m: Modifier) {
    Column(m.background(Color(0xFFECF0F1)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Flat Design", color = Color(0xFF2C3E50), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Color(0xFF2ECC71) to Icons.Default.Home, Color(0xFF3498DB) to Icons.Default.Search, Color(0xFFE74C3C) to Icons.Default.Favorite)
                .forEach { (c, ic) ->
                    Box(Modifier.weight(1f).height(50.dp).background(c), contentAlignment = Alignment.Center) {
                        Icon(ic, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
        }
        Box(Modifier.fillMaxWidth().background(Color(0xFF34495E)).padding(8.dp), contentAlignment = Alignment.Center) {
            Text("SOLID BUTTON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

// ---------- Neo-brutalism ----------
@Composable
private fun BrutalPreview(m: Modifier) {
    Box(m.background(Color(0xFFFFF4CC))) {
        Box(Modifier.align(Alignment.Center).fillMaxWidth(0.82f)) {
            Box(Modifier.matchParentSize().offset(5.dp, 5.dp).background(Color.Black))
            Column(
                Modifier.fillMaxWidth().background(Color(0xFFFF90E8)).border(2.5.dp, Color.Black).padding(11.dp)
            ) {
                Text("NEO-BRUTAL", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("Thick borders. Hard shadows.", color = Color.Black, fontSize = 11.sp)
                Spacer(Modifier.height(7.dp))
                Box(Modifier.background(Color(0xFF23A094)).border(2.dp, Color.Black).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text("CLICK", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------- Claymorphism ----------
@Composable
private fun ClayPreview(m: Modifier) {
    Box(m.background(Color(0xFFFFE8F0)), contentAlignment = Alignment.Center) {
        val shape = RoundedCornerShape(26.dp)
        Column(
            Modifier.fillMaxWidth(0.78f)
                .shadow(14.dp, shape, ambientColor = Color(0xFFE57AA0), spotColor = Color(0xFFE57AA0))
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFFC8DC))), shape)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Clay card", color = Color(0xFF7A2E4D), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text("Puffy, soft and playful", color = Color(0xFFA2506F), fontSize = 11.sp)
            Spacer(Modifier.height(7.dp))
            val pill = RoundedCornerShape(50)
            Box(
                Modifier.shadow(8.dp, pill, ambientColor = Color(0xFF6C8FFF), spotColor = Color(0xFF6C8FFF))
                    .background(Brush.verticalGradient(listOf(Color(0xFFB8D0FF), Color(0xFF7FA3FF))), pill)
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) { Text("Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        }
    }
}

// ---------- Gradient / Aurora / Vapor ----------
@Composable
private fun GradientPreview(m: Modifier) {
    Box(m.background(Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFB86B), Color(0xFF845EF7)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Gradient UI", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("Smooth color transitions add energy", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.clip(CircleShape).background(Color.White).padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text("Get started", color = Color(0xFF845EF7), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AuroraPreview(m: Modifier, title: String, bg: Color, blobs: List<Color>, text: Color) {
    Box(m.background(bg)) {
        Box(Modifier.size(100.dp).offset((-16).dp, (-16).dp).blur(30.dp).background(blobs[0].copy(alpha = 0.8f), CircleShape))
        Box(Modifier.size(110.dp).align(Alignment.CenterEnd).offset(16.dp, 8.dp).blur(32.dp).background(blobs[1].copy(alpha = 0.8f), CircleShape))
        Box(Modifier.size(96.dp).align(Alignment.BottomStart).offset(24.dp, 24.dp).blur(28.dp).background(blobs[2].copy(alpha = 0.8f), CircleShape))
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = text, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("Soft flowing color behind simple UI", color = text.copy(alpha = 0.85f), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.clip(CircleShape).background(text.copy(alpha = 0.16f))
                    .border(1.dp, text.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text("Join", color = text, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun VaporPreview(m: Modifier) {
    Box(
        m.background(Brush.verticalGradient(listOf(Color(0xFF2B0A4E), Color(0xFFB0289A), Color(0xFFFF3CAC)))).drawBehind {
            val horizon = size.height * 0.62f
            drawCircle(
                Brush.verticalGradient(listOf(Color(0xFFFFE259), Color(0xFFFF3CAC)), startY = horizon - 90f, endY = horizon),
                radius = 42.dp.toPx(), center = Offset(size.width / 2, horizon - 14.dp.toPx())
            )
            drawRect(Color(0xFF1A0533), Offset(0f, horizon), Size(size.width, size.height - horizon))
            var y = horizon
            var gap = 7f
            while (y < size.height) { drawLine(Color(0x99FF3CAC), Offset(0f, y), Offset(size.width, y), 1.8f); y += gap; gap *= 1.35f }
            for (i in -6..6) drawLine(Color(0x66FF3CAC), Offset(size.width / 2, horizon), Offset(size.width / 2 + i * size.width / 6, size.height), 1.8f)
        }
    ) {
        Text(
            "VAPORWAVE",
            color = Color(0xFF7CF2FF),
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
        )
    }
}

// ---------- Retro ----------
@Composable
private fun RetroPreview(m: Modifier) {
    Box(m.background(Color(0xFF008080))) {
        Column(Modifier.align(Alignment.Center).fillMaxWidth(0.82f).background(Color(0xFFC0C0C0)).border(2.dp, Color.Black).padding(2.dp)) {
            Row(
                Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF000080), Color(0xFF1084D0)))).padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Retro.exe", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Column(Modifier.padding(9.dp)) {
                Text("Hello, 1998.", color = Color.Black, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(Modifier.height(7.dp))
                Box(Modifier.background(Color(0xFFC0C0C0)).border(1.5.dp, Color.Black).padding(horizontal = 12.dp, vertical = 3.dp)) {
                    Text("OK", color = Color.Black, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------- Dark (Upgraded Luxury Midnight Obsidian & Electric Indigo-Cyan) ----------
@Composable
private fun DarkPreview(m: Modifier) {
    Box(
        m.background(
            Brush.linearGradient(
                listOf(Color(0xFF060A14), Color(0xFF0F172A), Color(0xFF17203D))
            )
        ).padding(10.dp)
    ) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF16203A), Color(0xFF0F1629))),
                    shape
                )
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.55f), Color(0xFF6366F1).copy(alpha = 0.25f))),
                    shape
                )
                .padding(11.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dark UI Analytics", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    Text("$48,920", color = Color(0xFFF8FAFC), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF06B6D4)))
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text("+28.4%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Canvas(Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp)) {
                val pts = listOf(0.75f, 0.56f, 0.64f, 0.36f, 0.44f, 0.18f, 0.22f)
                val linePath = Path()
                val fillPath = Path()
                pts.forEachIndexed { i, v ->
                    val x = size.width * i / (pts.size - 1)
                    val y = size.height * v
                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, size.height)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                    if (i == pts.lastIndex) {
                        fillPath.lineTo(x, size.height)
                        fillPath.close()
                    }
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF38BDF8).copy(alpha = 0.32f), Color(0xFF6366F1).copy(alpha = 0.02f))
                    )
                )
                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(listOf(Color(0xFFA78BFA), Color(0xFF38BDF8), Color(0xFF00FFA3))),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
                pts.forEachIndexed { i, v ->
                    if (i == 3 || i == pts.lastIndex) {
                        val cx = size.width * i / (pts.size - 1)
                        val cy = size.height * v
                        drawCircle(Color(0xFF38BDF8).copy(alpha = 0.35f), radius = 6.dp.toPx(), center = Offset(cx, cy))
                        drawCircle(Color.White, radius = 2.8.dp.toPx(), center = Offset(cx, cy))
                    }
                }
            }
        }
    }
}

// ---------- Skeuomorphism ----------
@Composable
private fun SkeuoPreview(m: Modifier) {
    Box(m.background(Brush.verticalGradient(listOf(Color(0xFF6B4A2B), Color(0xFF4A3119)))), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxWidth(0.8f).height(104.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF8B5E34), Color(0xFF5E3B1E))))
                .drawBehind {
                    drawRoundRect(
                        Color(0xFFE8D3A8), Offset(8f, 8f), Size(size.width - 16f, size.height - 16f), CornerRadius(16f),
                        style = Stroke(1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val pill = RoundedCornerShape(10.dp)
            Box(
                Modifier.background(Brush.verticalGradient(listOf(Color(0xFFF7E7C0), Color(0xFFC9A15A))), pill)
                    .border(1.dp, Color(0xFF3B2412), pill).padding(horizontal = 20.dp, vertical = 8.dp)
            ) { Text("Notes", color = Color(0xFF3B2412), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        }
    }
}

// ---------- Swiss ----------
@Composable
private fun SwissPreview(m: Modifier) {
    Row(m.background(Color.White).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1.2f)) {
            Text("Aa", color = Color(0xFF212121), fontSize = 52.sp, fontWeight = FontWeight.Bold, lineHeight = 52.sp)
            HorizontalDivider(color = Color(0xFF212121), thickness = 2.5.dp)
            Spacer(Modifier.height(4.dp))
            Text("Grid. Asymmetry. Sans-serif.", color = Color(0xFF212121), fontSize = 11.sp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.fillMaxWidth().height(54.dp).background(Color(0xFFD32F2F)))
            Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFF212121)))
            Box(Modifier.fillMaxWidth(0.6f).height(5.dp).background(Color(0xFF9E9E9E)))
            Box(Modifier.fillMaxWidth(0.8f).height(5.dp).background(Color(0xFF9E9E9E)))
        }
    }
}

// ---------- Concept card (no visual sample exists for principles/patterns) ----------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConceptPreview(m: Modifier, entry: DesignEntry?) {
    val colors = entry?.palette?.map { parseHex(it) }?.takeIf { it.size >= 2 }
        ?: listOf(Color(0xFF3D5AFE), Color(0xFF00C2D1))
    Box(m.background(Brush.linearGradient(colors.map { it.copy(alpha = 0.92f) }))) {
        Column(Modifier.padding(14.dp).align(Alignment.CenterStart)) {
            Text(entry?.category ?: "Design", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            Text(entry?.name ?: "Concept", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            Spacer(Modifier.height(7.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                (entry?.characteristics ?: emptyList()).take(3).forEach { c ->
                    Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(c, color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
