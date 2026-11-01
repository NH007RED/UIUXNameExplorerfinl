package com.nh.uiuxexplorer.ui.designer

import android.os.SystemClock
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.model.FrameStyle
import com.nh.uiuxexplorer.domain.model.ResizeHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MIN_PINCH_ZOOM = 0.55f
private const val MAX_PINCH_ZOOM = 3.5f
private const val DOUBLE_TAP_ZOOM = 2.0f
private const val ROTATE_HANDLE_OFFSET_UNITS = 28f

private sealed interface GestureTarget {
    data object Background : GestureTarget
    data class SelectLockedElement(val id: String) : GestureTarget
    data class MoveElement(val id: String) : GestureTarget
    data class HandleResize(val id: String, val handle: ResizeHandle) : GestureTarget
    data class RotateHandle(val id: String) : GestureTarget
}

private class AnimJobHolder {
    var job: Job? = null
}

@Composable
fun InteractivePhoneViewport(
    elements: List<DesignElement>,
    theme: StyleTheme,
    frame: FrameStyle,
    selectedId: String?,
    guideV: Boolean,
    guideH: Boolean,
    interactive: Boolean,
    layoutLocked: Boolean = false,
    hideHandles: Boolean = false,
    loading: Boolean = false,
    resetZoomTick: Int = 0,
    bottomPaddingDp: Dp = 6.dp,
    topPaddingDp: Dp = 4.dp,
    modifier: Modifier = Modifier,
    onGestureStart: (String) -> Unit = {},
    onCancelGesture: () -> Unit = {},
    onTransform: (id: String, panX: Float, panY: Float, zoom: Float, rotationDeg: Float) -> Unit = { _, _, _, _, _ -> },
    onCornerResize: (id: String, handle: ResizeHandle, localDx: Float, localDy: Float) -> Unit = { _, _, _, _ -> },
    onRotateTo: (id: String, angleDeg: Float) -> Unit = { _, _ -> },
    onGestureEnd: () -> Unit = {},
    onBackgroundTap: () -> Unit = {},
    onToggleLayoutLock: () -> Unit = {}
) {
    val density = LocalDensity.current
    val viewConfig = LocalViewConfiguration.current
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var pinchResizesItem by remember { mutableStateOf(false) }
    val animHolder = remember { AnimJobHolder() }

    val toggledSwitches = remember { mutableStateMapOf<String, Boolean>() }
    var activeNavIndex by remember { mutableIntStateOf(0) }

    val currentElements by rememberUpdatedState(elements)
    val currentSelectedId by rememberUpdatedState(selectedId)
    val currentInteractive by rememberUpdatedState(interactive)
    val currentLayoutLocked by rememberUpdatedState(layoutLocked)
    val currentPinchResizesItem by rememberUpdatedState(pinchResizesItem)
    val startCb by rememberUpdatedState(onGestureStart)
    val cancelCb by rememberUpdatedState(onCancelGesture)
    val transformCb by rememberUpdatedState(onTransform)
    val cornerResizeCb by rememberUpdatedState(onCornerResize)
    val rotateToCb by rememberUpdatedState(onRotateTo)
    val endCb by rememberUpdatedState(onGestureEnd)
    val bgTapCb by rememberUpdatedState(onBackgroundTap)

    Column(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds(),
            contentAlignment = Alignment.TopCenter
        ) {
            val availW = (maxWidth - 16.dp).coerceAtLeast(80.dp)
            val availH = (maxHeight - topPaddingDp - bottomPaddingDp).coerceAtLeast(100.dp)
            val baseW = fitCanvasWidth(availW, availH, frame)
            val basePx = with(density) { baseW.toPx() }.coerceAtLeast(1f)
            val frameWpx = with(density) { frameWidth(baseW, frame).toPx() }
            val frameHpx = with(density) { frameHeight(baseW, frame).toPx() }
            val vw = constraints.maxWidth.toFloat()
            val vh = constraints.maxHeight.toFloat()

            fun clampPan(p: Offset, z: Float): Offset {
                val scaledW = frameWpx * z
                val scaledH = frameHpx * z
                val mx = maxOf((scaledW - vw) / 2f + vw * 0.35f, scaledW * 0.45f, 36f)
                val my = maxOf((scaledH - vh) / 2f + vh * 0.35f, scaledH * 0.45f, 36f)
                return Offset(p.x.coerceIn(-mx, mx), p.y.coerceIn(-my, my))
            }

            fun animateViewportTo(targetZ: Float, targetPan: Offset) {
                animHolder.job?.cancel()
                val startZ = zoom
                val startX = panX
                val startY = panY
                val clamped = if (targetZ <= 1.001f && targetPan == Offset.Zero) Offset.Zero else clampPan(targetPan, targetZ)
                animHolder.job = scope.launch {
                    animate(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                    ) { frac, _ ->
                        zoom = startZ + (targetZ - startZ) * frac
                        panX = startX + (clamped.x - startX) * frac
                        panY = startY + (clamped.y - startY) * frac
                    }
                }
            }

            LaunchedEffect(resetZoomTick) {
                if (resetZoomTick > 0) {
                    animateViewportTo(1f, Offset.Zero)
                }
            }

            LaunchedEffect(hideHandles, selectedId) {
                if (hideHandles && selectedId != null) {
                    val el = currentElements.firstOrNull { it.id == selectedId }
                    if (el != null) {
                        val elCenterY = (el.y + el.h / 2f) - DesignerDefaults.H / 2f
                        val targetPanY = -elCenterY * (basePx / DesignerDefaults.W) * zoom
                        animateViewportTo(zoom, Offset(panX, targetPanY))
                    }
                } else if (!hideHandles && zoom <= 1.02f && (panX != 0f || panY != 0f)) {
                    animateViewportTo(1f, Offset.Zero)
                }
            }

            fun screenToDesign(pt: Offset): Offset {
                val cx = vw / 2f + panX
                val cy = vh / 2f + panY
                val relX = (pt.x - cx) / zoom
                val relY = (pt.y - cy) / zoom
                val ux = (relX / basePx + 0.5f) * DesignerDefaults.W
                val uy = (relY / basePx) * DesignerDefaults.W + DesignerDefaults.H / 2f
                return Offset(ux, uy)
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(baseW, frame) {
                        val touchSlop = viewConfig.touchSlop
                        var lastTapTime = 0L
                        var lastTapPos = Offset.Zero

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            animHolder.job?.cancel()

                            val downTime = SystemClock.uptimeMillis()
                            val downPos = down.position
                            var totalMove = 0f
                            var multiTouchActive = false

                            val designPt = screenToDesign(downPos)
                            val ux = designPt.x
                            val uy = designPt.y

                            var target: GestureTarget = GestureTarget.Background
                            if (currentInteractive && !currentLayoutLocked) {
                                val sel = currentElements.firstOrNull { it.id == currentSelectedId }
                                if (sel != null && !sel.locked) {
                                    val (lx, ly) = DesignerDefaults.toElementLocal(ux, uy, sel)
                                    val hitRadius = (26f / zoom.coerceIn(0.8f, 3f)).coerceAtLeast(12f)
                                    val rotOffset = ROTATE_HANDLE_OFFSET_UNITS / zoom.coerceIn(0.8f, 2.5f)
                                    val rotX = sel.x + sel.w / 2f
                                    val rotY = sel.y - rotOffset

                                    if (hypot(lx - rotX, ly - rotY) <= hitRadius) {
                                        target = GestureTarget.RotateHandle(sel.id)
                                        startCb(sel.id)
                                    } else {
                                        val handles = listOf(
                                            ResizeHandle.BOTTOM_RIGHT to Offset(sel.x + sel.w, sel.y + sel.h),
                                            ResizeHandle.TOP_LEFT to Offset(sel.x, sel.y),
                                            ResizeHandle.TOP_RIGHT to Offset(sel.x + sel.w, sel.y),
                                            ResizeHandle.BOTTOM_LEFT to Offset(sel.x, sel.y + sel.h),
                                            ResizeHandle.RIGHT to Offset(sel.x + sel.w, sel.y + sel.h / 2f),
                                            ResizeHandle.LEFT to Offset(sel.x, sel.y + sel.h / 2f),
                                            ResizeHandle.BOTTOM to Offset(sel.x + sel.w / 2f, sel.y + sel.h),
                                            ResizeHandle.TOP to Offset(sel.x + sel.w / 2f, sel.y)
                                        )
                                        val hitHandle = handles.firstOrNull { (_, pt) ->
                                            hypot(lx - pt.x, ly - pt.y) <= hitRadius
                                        }
                                        if (hitHandle != null) {
                                            target = GestureTarget.HandleResize(sel.id, hitHandle.first)
                                            startCb(sel.id)
                                        }
                                    }
                                }
                                if (target == GestureTarget.Background) {
                                    val hitUnlocked = currentElements.asReversed().firstOrNull {
                                        !it.locked && DesignerDefaults.hitTestElement(ux, uy, it, slopUnits = 4f)
                                    }
                                    if (hitUnlocked != null) {
                                        target = GestureTarget.MoveElement(hitUnlocked.id)
                                        startCb(hitUnlocked.id)
                                    } else {
                                        val hitLocked = currentElements.asReversed().firstOrNull {
                                            it.locked && DesignerDefaults.hitTestElement(ux, uy, it, slopUnits = 2f)
                                        }
                                        if (hitLocked != null) {
                                            target = GestureTarget.SelectLockedElement(hitLocked.id)
                                        }
                                    }
                                }
                            }

                            do {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.filter { it.pressed }

                                if (pressed.size >= 2) {
                                    val activeSel = currentElements.firstOrNull { it.id == currentSelectedId }
                                    val shouldPinchElement = currentInteractive && !currentLayoutLocked &&
                                        currentPinchResizesItem && activeSel != null && !activeSel.locked

                                    if (!multiTouchActive) {
                                        multiTouchActive = true
                                        if (shouldPinchElement) {
                                            startCb(activeSel!!.id)
                                        } else if (target != GestureTarget.Background && target !is GestureTarget.SelectLockedElement) {
                                            if (SystemClock.uptimeMillis() - downTime < 220L && totalMove < touchSlop * 2f) {
                                                cancelCb()
                                            } else {
                                                endCb()
                                            }
                                            target = GestureTarget.Background
                                        }
                                    }

                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val rotChange = event.calculateRotation()
                                    val centroid = event.calculateCentroid(useCurrent = true)

                                    if (shouldPinchElement && activeSel != null) {
                                        val duX = panChange.x * DesignerDefaults.W / (basePx * zoom)
                                        val duY = panChange.y * DesignerDefaults.W / (basePx * zoom)
                                        transformCb(activeSel.id, duX, duY, zoomChange, rotChange)
                                    } else if (zoomChange != 1f || panChange != Offset.Zero) {
                                        val oldZ = zoom
                                        val newZ = (oldZ * zoomChange).coerceIn(MIN_PINCH_ZOOM, MAX_PINCH_ZOOM)
                                        val vpCenter = Offset(vw / 2f, vh / 2f)
                                        val c = if (centroid != Offset.Unspecified) centroid - vpCenter else Offset.Zero
                                        val curPan = Offset(panX, panY)
                                        val nextPan = c - (c - curPan) * (newZ / oldZ) + panChange
                                        val clamped = clampPan(nextPan, newZ)
                                        zoom = newZ
                                        panX = clamped.x
                                        panY = clamped.y
                                    }
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                } else if (pressed.size == 1 && !multiTouchActive) {
                                    val change = pressed.first()
                                    val delta = change.positionChange()
                                    if (delta != Offset.Zero) {
                                        totalMove += delta.getDistance()
                                        val duX = delta.x * DesignerDefaults.W / (basePx * zoom)
                                        val duY = delta.y * DesignerDefaults.W / (basePx * zoom)

                                        when (val t = target) {
                                            is GestureTarget.MoveElement -> {
                                                transformCb(t.id, duX, duY, 1f, 0f)
                                            }
                                            is GestureTarget.HandleResize -> {
                                                val el = currentElements.firstOrNull { it.id == t.id }
                                                val rot = el?.rotation ?: 0f
                                                val rad = Math.toRadians(rot.toDouble())
                                                val c = cos(rad).toFloat()
                                                val s = sin(rad).toFloat()
                                                val localDx = duX * c + duY * s
                                                val localDy = -duX * s + duY * c
                                                cornerResizeCb(t.id, t.handle, localDx, localDy)
                                            }
                                            is GestureTarget.RotateHandle -> {
                                                val el = currentElements.firstOrNull { it.id == t.id }
                                                if (el != null) {
                                                    val curDesign = screenToDesign(change.position)
                                                    val cx = el.x + el.w / 2f
                                                    val cy = el.y + el.h / 2f
                                                    val deg = Math.toDegrees(
                                                        atan2((curDesign.y - cy).toDouble(), (curDesign.x - cx).toDouble())
                                                    ).toFloat() + 90f
                                                    rotateToCb(t.id, deg)
                                                }
                                            }
                                            is GestureTarget.SelectLockedElement, GestureTarget.Background -> {
                                                val clamped = clampPan(Offset(panX + delta.x, panY + delta.y), zoom)
                                                panX = clamped.x
                                                panY = clamped.y
                                            }
                                        }
                                        change.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (target is GestureTarget.MoveElement || target is GestureTarget.HandleResize ||
                                target is GestureTarget.RotateHandle || (multiTouchActive && currentPinchResizesItem)
                            ) {
                                endCb()
                            } else if (!multiTouchActive && totalMove < touchSlop) {
                                val lockedTarget = target as? GestureTarget.SelectLockedElement
                                if (lockedTarget != null) {
                                    startCb(lockedTarget.id)
                                } else {
                                    val now = SystemClock.uptimeMillis()
                                    val isDoubleTap = (now - lastTapTime < 300L) &&
                                        ((downPos - lastTapPos).getDistance() < touchSlop * 3.5f)
                                    if (isDoubleTap) {
                                        lastTapTime = 0L
                                        if (zoom != 1f || hypot(panX, panY) > 8f) {
                                            animateViewportTo(1f, Offset.Zero)
                                        } else {
                                            val vpCenter = Offset(vw / 2f, vh / 2f)
                                            val c = downPos - vpCenter
                                            val targetPan = c * (1f - DOUBLE_TAP_ZOOM)
                                            animateViewportTo(DOUBLE_TAP_ZOOM, targetPan)
                                        }
                                    } else {
                                        lastTapTime = now
                                        lastTapPos = downPos
                                        if (currentInteractive) {
                                            bgTapCb()
                                        } else {
                                            val hitEl = currentElements.asReversed().firstOrNull {
                                                DesignerDefaults.hitTestElement(ux, uy, it, slopUnits = 2f)
                                            }
                                            if (hitEl != null) {
                                                when (hitEl.type) {
                                                    ElementType.SWITCH, ElementType.CHIP, ElementType.BUTTON -> {
                                                        toggledSwitches[hitEl.id] = !(toggledSwitches[hitEl.id] ?: true)
                                                    }
                                                    ElementType.BOTTOM_NAV -> {
                                                        val slotCount = DesignerDefaults.bottomNavIcons(hitEl).size.coerceAtLeast(1)
                                                        val relX = ((ux - hitEl.x) / hitEl.w.coerceAtLeast(1f)).coerceIn(0f, 0.99f)
                                                        activeNavIndex = (relX * slotCount).toInt().coerceIn(0, slotCount - 1)
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .requiredSize(frameWidth(baseW, frame), frameHeight(baseW, frame))
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = panX
                                translationY = panY
                            }
                    ) {
                        PhoneFrame(baseW, frame, theme) { corner, overlay ->
                            ScreenCanvas(
                                elements = elements,
                                theme = theme,
                                widthDp = baseW,
                                selectedId = if (interactive && !layoutLocked) selectedId else null,
                                guideV = interactive && !layoutLocked && guideV,
                                guideH = interactive && !layoutLocked && guideH,
                                interactive = interactive && !layoutLocked,
                                hideHandles = hideHandles,
                                viewportZoom = zoom,
                                cornerDp = corner,
                                toggledStates = toggledSwitches,
                                activeNavTab = activeNavIndex,
                                overlay = overlay
                            )
                        }
                    }
                }
            }
        }

        if (!hideHandles) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (interactive && layoutLocked) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFB300).copy(alpha = 0.90f),
                        modifier = Modifier.clickable { onToggleLayoutLock() }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(13.dp), tint = Color(0xFF1F1600))
                            Text(
                                "Layout Locked · Tap to Unlock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1600)
                            )
                        }
                    }
                } else if (interactive && selectedId != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (pinchResizesItem) cs.primaryContainer else cs.surfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.clickable { pinchResizesItem = !pinchResizesItem }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (pinchResizesItem) Icons.Default.OpenInFull else Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (pinchResizesItem) cs.onPrimaryContainer else cs.primary
                            )
                            Text(
                                text = if (pinchResizesItem) "2-Finger: Resize Item" else "2-Finger: Zoom Phone",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (pinchResizesItem) cs.onPrimaryContainer else cs.onSurface
                            )
                        }
                    }
                } else {
                    Text(
                        "Pinch phone to zoom · Drag element to move",
                        fontSize = 11.sp,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = cs.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable {
                                    zoom = (zoom / 1.2f).coerceIn(MIN_PINCH_ZOOM, MAX_PINCH_ZOOM)
                                    if (zoom <= 1f) { panX = 0f; panY = 0f }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, "Zoom out", modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "${(zoom * 100).roundToInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .width(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { zoom = 1f; panX = 0f; panY = 0f }
                                .padding(vertical = 2.dp)
                        )
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable {
                                    zoom = (zoom * 1.2f).coerceIn(MIN_PINCH_ZOOM, MAX_PINCH_ZOOM)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, "Zoom in", modifier = Modifier.size(14.dp))
                        }
                        if (zoom != 1f || panX != 0f || panY != 0f) {
                            Spacer(Modifier.width(2.dp))
                            Box(
                                Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable { zoom = 1f; panX = 0f; panY = 0f },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FitScreen, "Fit 100%", modifier = Modifier.size(14.dp), tint = cs.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenCanvas(
    elements: List<DesignElement>,
    theme: StyleTheme,
    widthDp: Dp,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    guideV: Boolean = false,
    guideH: Boolean = false,
    interactive: Boolean = false,
    hideHandles: Boolean = false,
    viewportZoom: Float = 1f,
    cornerDp: Dp = 22.dp,
    toggledStates: Map<String, Boolean> = emptyMap(),
    activeNavTab: Int = 0,
    onGestureStart: (String) -> Unit = {},
    onTransform: (id: String, panX: Float, panY: Float, zoom: Float, rotationDeg: Float) -> Unit = { _, _, _, _, _ -> },
    onResize: (id: String, dw: Float, dh: Float) -> Unit = { _, _, _ -> },
    onGestureEnd: () -> Unit = {},
    onBackgroundTap: () -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val scale = widthDp.value / DesignerDefaults.W
    val density = LocalDensity.current.density
    val guideColor = Color(0xFFFF4081)
    val selectionColor = Color(0xFF00E5FF)
    val lockedColor = Color(0xFFFFB300)

    Box(
        modifier
            .width(widthDp)
            .height((DesignerDefaults.H * scale).dp)
            .clip(RoundedCornerShape(cornerDp))
            .background(theme.background)
    ) {
        elements.forEach { el ->
            key(el.id) {
                val selected = interactive && el.id == selectedId
                val exactPxX = el.x * scale * density
                val exactPxY = el.y * scale * density
                val intPxX = exactPxX.toInt()
                val intPxY = exactPxY.toInt()
                val fracPxX = exactPxX - intPxX
                val fracPxY = exactPxY - intPxY

                Box(
                    Modifier
                        .offset { IntOffset(intPxX, intPxY) }
                        .size((el.w * scale).dp, (el.h * scale).dp)
                        .graphicsLayer {
                            translationX = fracPxX
                            translationY = fracPxY
                            rotationZ = el.rotation
                        }
                ) {
                    val isToggled = toggledStates[el.id] ?: true
                    ElementContent(el, theme, scale, isToggled, activeNavTab)

                    if (interactive && el.locked) {
                        val invZoom = (1f / viewportZoom.coerceIn(0.8f, 3f))
                        val badgeDp = (13f * invZoom).dp.coerceAtLeast(7.dp)
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = badgeDp / 3f, y = -badgeDp / 3f)
                                .size(badgeDp)
                                .background(lockedColor, CircleShape)
                                .border((1f * invZoom).dp.coerceAtLeast(0.5.dp), Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF1F1600),
                                modifier = Modifier.size(badgeDp * 0.68f)
                            )
                        }
                    }

                    if (selected) {
                        val invZoom = (1f / viewportZoom.coerceIn(0.8f, 3f))
                        val outlineCol = if (el.locked) lockedColor else selectionColor

                        Box(
                            Modifier.fillMaxSize().drawBehind {
                                val strokePx = (1.8.dp.toPx() * invZoom).coerceAtLeast(1f)
                                drawRoundRect(
                                    outlineCol,
                                    cornerRadius = CornerRadius(6.dp.toPx()),
                                    style = Stroke(
                                        width = strokePx,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f * invZoom, 7f * invZoom))
                                    )
                                )
                            }
                        )

                        if (!el.locked && !hideHandles) {
                            val handleDp = (12f * invZoom).dp.coerceAtLeast(5.5.dp)
                            val edgeHandleDp = (9f * invZoom).dp.coerceAtLeast(4.5.dp)
                            val halfHandle = handleDp / 2f
                            val halfEdge = edgeHandleDp / 2f
                            val rotOffsetDp = ((ROTATE_HANDLE_OFFSET_UNITS * scale) * invZoom).dp

                            Box(
                                Modifier.fillMaxSize().drawBehind {
                                    val strokePx = (1.8.dp.toPx() * invZoom).coerceAtLeast(1f)
                                    drawLine(
                                        color = selectionColor,
                                        start = Offset(size.width / 2f, 0f),
                                        end = Offset(size.width / 2f, -rotOffsetDp.toPx()),
                                        strokeWidth = strokePx
                                    )
                                }
                            )

                            Box(
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = -rotOffsetDp - halfHandle)
                                    .size(handleDp * 1.25f)
                                    .background(selectionColor, CircleShape)
                                    .border((1.5f * invZoom).dp.coerceAtLeast(0.75.dp), Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF062026),
                                    modifier = Modifier.size(handleDp * 0.85f)
                                )
                            }

                            HandleDot(Alignment.TopStart, -halfHandle, -halfHandle, handleDp, selectionColor, invZoom)
                            HandleDot(Alignment.TopEnd, halfHandle, -halfHandle, handleDp, selectionColor, invZoom)
                            HandleDot(Alignment.BottomStart, -halfHandle, halfHandle, handleDp, selectionColor, invZoom)
                            HandleDot(Alignment.BottomEnd, halfHandle, halfHandle, handleDp, selectionColor, invZoom)

                            if (el.w >= 64f && el.h >= 38f) {
                                HandleDot(Alignment.TopCenter, 0.dp, -halfEdge, edgeHandleDp, selectionColor, invZoom)
                                HandleDot(Alignment.BottomCenter, 0.dp, halfEdge, edgeHandleDp, selectionColor, invZoom)
                                HandleDot(Alignment.CenterStart, -halfEdge, 0.dp, edgeHandleDp, selectionColor, invZoom)
                                HandleDot(Alignment.CenterEnd, halfEdge, 0.dp, edgeHandleDp, selectionColor, invZoom)
                            }
                        }
                    }
                }
            }
        }
        overlay()
        if (interactive && (guideV || guideH)) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = (1.8f / viewportZoom.coerceIn(1f, 3f)).coerceAtLeast(0.8f)
                if (guideV) drawLine(guideColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), stroke)
                if (guideH) drawLine(guideColor, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), stroke)
            }
        }
    }
}

@Composable
private fun BoxScope.HandleDot(
    alignment: Alignment,
    offsetX: Dp,
    offsetY: Dp,
    sizeDp: Dp,
    color: Color,
    invZoom: Float
) {
    Box(
        Modifier
            .align(alignment)
            .offset(offsetX, offsetY)
            .size(sizeDp)
            .background(Color.White, CircleShape)
            .border((2f * invZoom).dp.coerceAtLeast(1.dp), color, CircleShape)
    )
}

// ---------------------------------------------------------------- styling helpers

private fun Modifier.styled(
    t: StyleTheme, s: Float, fill: Color = t.surface, brush: Brush? = null, radiusUnits: Float = t.radius
): Modifier {
    val shape = RoundedCornerShape((radiusUnits * s).dp)
    var m: Modifier = this
    when (t.shadow) {
        ShadowKind.SOFT -> m = m.shadow((5 * s).dp, shape, ambientColor = t.shadowColor, spotColor = t.shadowColor)
        ShadowKind.HARD -> m = m.drawBehind {
            val o = 3.dp.toPx() * s * 2f
            drawRoundRect(t.shadowColor, Offset(o, o), size, CornerRadius(radiusUnits * s * density))
        }
        ShadowKind.NEU -> m = m.drawBehind {
            val r = radiusUnits * s * density
            val d = 6.dp.toPx() * s * 1.6f
            val paint = android.graphics.Paint().apply { isAntiAlias = true; color = t.surface.toArgb() }
            drawIntoCanvas { c ->
                paint.setShadowLayer(d * 1.6f, -d, -d, Color.White.toArgb())
                c.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, paint)
                paint.setShadowLayer(d * 1.6f, d, d, Color(0xFFA3B1C6).toArgb())
                c.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, paint)
            }
        }
        ShadowKind.NONE -> {}
    }
    m = m.clip(shape)
    m = if (brush != null) m.background(brush) else m.background(fill)
    val bc = t.borderColor
    if (bc != null && t.borderWidth > 0f) m = m.border((t.borderWidth * s).dp.coerceAtLeast(0.5.dp), bc, shape)
    return m
}

@Composable
private fun sp(units: Float, s: Float): TextUnit = with(LocalDensity.current) { (units * s).dp.toSp() }

private val NoFontPaddingStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

// ---------------------------------------------------------------- element content

@Composable
private fun ElementContent(
    el: DesignElement,
    t: StyleTheme,
    s: Float,
    isToggled: Boolean = true,
    activeNavTab: Int = 0
) {
    val weight = if (t.heavy) FontWeight.ExtraBold else FontWeight.SemiBold
    when (el.type) {
        ElementType.HEADLINE -> {
            val fitFs = minOf(el.fontSize, (el.h * 0.72f).coerceAtLeast(8f))
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Text(
                    el.text,
                    color = t.resolveTextColor(el.colorKey, t.onSurface),
                    fontSize = sp(fitFs, s),
                    fontFamily = t.font,
                    fontWeight = if (t.heavy) FontWeight.Black else FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Visible,
                    lineHeight = sp(fitFs * 1.15f, s),
                    style = NoFontPaddingStyle
                )
            }
        }
        ElementType.PARAGRAPH -> {
            val fitFs = if (el.h < 28f) minOf(el.fontSize, (el.h * 0.68f).coerceAtLeast(8f)) else el.fontSize
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = if (el.h < 32f) Alignment.CenterStart else Alignment.TopStart
            ) {
                Text(
                    el.text,
                    color = t.resolveTextColor(el.colorKey, t.muted),
                    fontSize = sp(fitFs, s),
                    fontFamily = t.font,
                    lineHeight = sp(fitFs * 1.28f, s),
                    overflow = TextOverflow.Clip,
                    style = NoFontPaddingStyle
                )
            }
        }
        ElementType.EXPLAIN_TEXT -> {
            val accentCol = t.resolveTextColor(el.colorKey, t.iconAccent)
            val textCol = t.resolveTextColor(el.colorKey, t.onSurface)
            val compact = el.h < 42f
            val fitFs = if (compact) minOf(el.fontSize, (el.h * 0.56f).coerceAtLeast(8f)) else el.fontSize
            val vPad = if (compact) (1.5f * s).dp else (5f * s).dp
            val hPad = if (compact) (6f * s).dp else (8f * s).dp
            val iconUnits = if (compact) minOf(13f, (el.h * 0.5f).coerceAtLeast(9f)) else 15f

            Row(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape((10f * s).dp))
                    .background(t.surface.copy(alpha = 0.94f))
                    .border((1.2f * s).dp.coerceAtLeast(0.5.dp), accentCol.copy(alpha = 0.6f), RoundedCornerShape((10f * s).dp)),
                verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width((4f * s).dp.coerceAtLeast(2.dp))
                        .background(accentCol)
                )
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = hPad, vertical = vPad),
                    verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top
                ) {
                    Icon(
                        IconCatalog.get(el.icon.ifBlank { "info" }),
                        contentDescription = null,
                        tint = accentCol,
                        modifier = Modifier
                            .padding(top = if (compact) 0.dp else (1.5f * s).dp)
                            .size((iconUnits * s).dp)
                    )
                    Spacer(Modifier.width((5 * s).dp))
                    Text(
                        el.text,
                        color = textCol,
                        fontSize = sp(fitFs, s),
                        fontFamily = t.font,
                        fontWeight = if (compact) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = sp(fitFs * (if (compact) 1.08f else 1.25f), s),
                        maxLines = if (compact) 1 else 6,
                        overflow = TextOverflow.Clip,
                        style = NoFontPaddingStyle
                    )
                }
            }
        }
        ElementType.BUTTON -> {
            val fitFs = minOf(el.fontSize, (el.h * 0.55f).coerceAtLeast(8f))
            Box(
                Modifier.fillMaxSize().styled(t, s, fill = t.accent, brush = t.accentBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    el.text,
                    color = t.resolveTextColor(el.colorKey, t.onAccent),
                    fontSize = sp(fitFs, s),
                    fontFamily = t.font,
                    fontWeight = weight,
                    maxLines = 1,
                    style = NoFontPaddingStyle
                )
            }
        }
        ElementType.CARD -> {
            val titleColor = t.resolveTextColor(el.colorKey, t.onSurface)
            val fitFs = minOf(el.fontSize, (el.h * 0.45f).coerceAtLeast(8f))
            Box(Modifier.fillMaxSize().styled(t, s)) {
                Column(
                    Modifier.padding((minOf(12f, el.h * 0.14f) * s).dp),
                    verticalArrangement = Arrangement.spacedBy((6 * s).dp)
                ) {
                    Text(
                        el.text,
                        color = titleColor,
                        fontSize = sp(fitFs, s),
                        fontFamily = t.font,
                        fontWeight = weight,
                        maxLines = 2,
                        style = NoFontPaddingStyle
                    )
                    if (el.h >= 54f) {
                        Box(Modifier.fillMaxWidth().height((7 * s).dp).clip(CircleShape).background(t.muted.copy(alpha = 0.35f)))
                    }
                    if (el.h >= 76f) {
                        Box(Modifier.fillMaxWidth(0.7f).height((7 * s).dp).clip(CircleShape).background(t.muted.copy(alpha = 0.35f)))
                    }
                }
            }
        }
        ElementType.IMAGE -> {
            val customImage = remember(el.imageUri) { ImageStorage.loadImageBitmap(el.imageUri) }
            if (customImage != null) {
                Box(
                    Modifier.fillMaxSize().styled(t, s, fill = t.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = customImage,
                        contentDescription = null,
                        contentScale = if (el.imageCrop) ContentScale.Crop else ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                val baseGradientColor = t.resolveTextColor(el.colorKey, t.accent)
                val iconDp = (minOf(40f, minOf(el.w, el.h) * 0.45f) * s).dp
                Box(
                    Modifier.fillMaxSize().styled(
                        t, s,
                        brush = Brush.linearGradient(listOf(baseGradientColor, baseGradientColor.copy(alpha = 0.35f)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        IconCatalog.get(el.icon.ifBlank { "image" }),
                        null,
                        tint = t.onAccent.copy(alpha = 0.85f),
                        modifier = Modifier.size(iconDp)
                    )
                }
            }
        }
        ElementType.INPUT -> {
            val inputInk = t.resolveTextColor(el.colorKey, t.muted)
            val fitFs = minOf(el.fontSize, (el.h * 0.55f).coerceAtLeast(8f))
            Box(Modifier.fillMaxSize().styled(t, s), contentAlignment = Alignment.CenterStart) {
                Row(
                    Modifier.padding(horizontal = (10 * s).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        IconCatalog.get(el.icon.ifBlank { "search" }),
                        null,
                        tint = inputInk,
                        modifier = Modifier.size((minOf(18f, el.h * 0.5f) * s).dp)
                    )
                    Spacer(Modifier.width((6 * s).dp))
                    Text(
                        el.text,
                        color = inputInk,
                        fontSize = sp(fitFs, s),
                        fontFamily = t.font,
                        maxLines = 1,
                        style = NoFontPaddingStyle
                    )
                }
            }
        }
        ElementType.CHIP -> {
            val fitFs = minOf(el.fontSize, (el.h * 0.58f).coerceAtLeast(8f))
            Box(
                Modifier.fillMaxSize().styled(
                    t, s,
                    fill = if (isToggled) t.surface else t.accent.copy(alpha = 0.25f),
                    radiusUnits = 999f
                ).padding(horizontal = (4 * s).dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    el.text,
                    color = t.resolveTextColor(el.colorKey, t.onSurface),
                    fontSize = sp(fitFs, s),
                    fontFamily = t.font,
                    maxLines = 1,
                    style = NoFontPaddingStyle
                )
            }
        }
        ElementType.SWITCH -> {
            val activeFill = t.resolveTextColor(el.colorKey, t.accent)
            Box(
                Modifier.fillMaxSize().styled(
                    t, s,
                    fill = if (isToggled) activeFill else t.muted.copy(alpha = 0.35f),
                    brush = if (isToggled && (el.colorKey.isBlank() || el.colorKey == "default" || el.colorKey == "accent")) t.accentBrush else null,
                    radiusUnits = 999f
                ).padding((3 * s).dp),
                contentAlignment = if (isToggled) Alignment.CenterEnd else Alignment.CenterStart
            ) { Box(Modifier.fillMaxSize().padding(0.dp).aspectKnob(isToggled)) }
        }
        ElementType.FAB -> Box(
            Modifier.fillMaxSize().styled(t, s, fill = t.accent, brush = t.accentBrush, radiusUnits = 999f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                IconCatalog.get(el.icon.ifBlank { "add" }),
                null,
                tint = t.resolveTextColor(el.colorKey, t.onAccent),
                modifier = Modifier.size((minOf(26f, minOf(el.w, el.h) * 0.5f) * s).dp)
            )
        }
        ElementType.TOP_BAR -> {
            val (leftIconKey, rightIconKey) = DesignerDefaults.topBarIcons(el)
            val barInk = t.resolveTextColor(el.colorKey, t.onSurface)
            val fitFs = minOf(el.fontSize, (el.h * 0.52f).coerceAtLeast(8f))
            Box(Modifier.fillMaxSize().styled(t, s, radiusUnits = 0f), contentAlignment = Alignment.CenterStart) {
                Row(
                    Modifier.padding(horizontal = (12 * s).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(IconCatalog.get(leftIconKey), null, tint = barInk, modifier = Modifier.size((minOf(22f, el.h * 0.5f) * s).dp))
                    Spacer(Modifier.width((12 * s).dp))
                    Text(
                        el.text,
                        color = barInk,
                        fontSize = sp(fitFs, s),
                        fontFamily = t.font,
                        fontWeight = weight,
                        maxLines = 1,
                        style = NoFontPaddingStyle,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(IconCatalog.get(rightIconKey), null, tint = barInk, modifier = Modifier.size((minOf(22f, el.h * 0.5f) * s).dp))
                }
            }
        }
        ElementType.ICON -> Icon(
            IconCatalog.get(el.icon),
            contentDescription = el.icon,
            tint = t.tintFor(el.colorKey),
            modifier = Modifier.fillMaxSize()
        )
        ElementType.BOTTOM_NAV -> {
            val navIconKeys = DesignerDefaults.bottomNavIcons(el)
            val activeTint = t.resolveTextColor(el.colorKey, t.iconAccent)
            val safeActiveTab = activeNavTab.coerceIn(0, (navIconKeys.size - 1).coerceAtLeast(0))
            Box(Modifier.fillMaxSize().styled(t, s, radiusUnits = 0f), contentAlignment = Alignment.Center) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = (10 * s).dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navIconKeys.forEachIndexed { i, key ->
                        Icon(
                            IconCatalog.get(key),
                            contentDescription = key,
                            tint = if (i == safeActiveTab) activeTint else t.muted,
                            modifier = Modifier.size((minOf(24f, el.h * 0.5f) * s).dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.aspectKnob(onRight: Boolean = true): Modifier = this.then(Modifier.drawBehind {
    val r = size.height / 2f
    val cx = if (onRight) size.width - r else r
    drawCircle(Color.White, radius = r, center = Offset(cx, size.height / 2f))
})
