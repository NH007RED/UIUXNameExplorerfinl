package com.nh.uiuxexplorer

import com.nh.uiuxexplorer.data.repo.ScreenCodec
import com.nh.uiuxexplorer.domain.designer.PromptBuilder
import com.nh.uiuxexplorer.domain.model.CornerHandle
import com.nh.uiuxexplorer.domain.model.DesignElement
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.domain.model.DesignerDefaults
import com.nh.uiuxexplorer.domain.model.ElementAlignment
import com.nh.uiuxexplorer.domain.model.ElementType
import com.nh.uiuxexplorer.domain.model.ResizeHandle
import com.nh.uiuxexplorer.domain.security.LicenseKeyItem
import com.nh.uiuxexplorer.domain.security.WatermarkAndAboutConfig
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignerTest {
    private val style = DesignEntry(
        id = "glassmorphism", name = "Glassmorphism", category = "UI Visual Styles", description = "d",
        aliases = listOf("Glass UI", "Blur UI"), keywords = emptyList(),
        characteristics = listOf("Transparent surfaces", "Background blur"), colors = "Vivid gradient.",
        typography = "Clean sans-serif.", components = "", layout = "", animation = "", useCases = emptyList(),
        advantages = emptyList(), disadvantages = emptyList(), related = emptyList(), example = "", prompt = "",
        preview = "glass", palette = listOf("5B2EFF", "00C2D1"), popular = true, trending = false
    )

    @Test fun promptMentionsStyleLayoutAndWatermark() {
        WatermarkGuard.updateConfig(WatermarkAndAboutConfig())
        val els = listOf(
            DesignElement("1", ElementType.BUTTON, 32f, 600f, 296f, 52f, 0f, "Buy now", 16f),
            DesignElement("2", ElementType.TOP_BAR, 0f, 0f, 360f, 56f, 0f, "Shop", 18f)
        )
        val p = PromptBuilder.build("Checkout", style, els)
        assertTrue(p.contains("\"Checkout\""))
        assertTrue(p.contains("Glassmorphism"))
        assertTrue(p.contains("#5B2EFF"))
        assertTrue(p.indexOf("Top app bar") < p.indexOf("Primary button"))
        assertTrue(p.contains("\"Buy now\""))
        assertTrue(p.contains("full width"))
        assertTrue(p.contains("Watermark:"))
        assertTrue(p.contains("NH Developer"))
    }

    @Test fun obfuscatedWatermarkGuardDecodesCleanly() {
        WatermarkGuard.updateConfig(WatermarkAndAboutConfig())
        assertEquals("UI/UX Name Explorer • NH Developer", WatermarkGuard.defaultWatermarkText())
        assertEquals("NH Developer", WatermarkGuard.defaultDeveloperName())
        assertEquals(0x2000, WatermarkGuard.resolveSecureWindowFlag())
        assertEquals(64, WatermarkGuard.expectedCertSha256().length)
        assertTrue(WatermarkGuard.resolvePngWatermark().contains("NH Developer"))
        assertTrue(WatermarkGuard.resolvePromptSignature().contains("UI/UX Name Explorer"))
    }

    @Test fun nhLicenseKeyRemovesWatermarkWhenActive() {
        val genKey = WatermarkGuard.generateNewNhLicenseKey()
        assertTrue(genKey.startsWith("NH"))
        val norm = WatermarkGuard.normalizeNhKey("vip2026")
        assertTrue(norm.startsWith("NH"))

        WatermarkGuard.updateConfig(
            WatermarkAndAboutConfig(
                licenseKeys = listOf(LicenseKeyItem(key = norm, label = "VIP")),
                activatedLicenseKey = norm
            )
        )
        assertTrue(WatermarkGuard.config.value.isLicenseValid)
        assertEquals("", WatermarkGuard.resolvePngWatermark())
        assertEquals("", WatermarkGuard.resolvePromptSignature())

        // Reset to default for subsequent tests
        WatermarkGuard.updateConfig(WatermarkAndAboutConfig())
        assertFalse(WatermarkGuard.config.value.isLicenseValid)
    }

    @Test fun promptWorksWithNoStyleOrElements() {
        val p = PromptBuilder.build("", null, emptyList())
        assertTrue(p.contains("Untitled screen"))
        assertTrue(p.contains("No components"))
    }

    @Test fun rotationIsReported() {
        val p = PromptBuilder.build("x", null, listOf(DesignElement("1", ElementType.CHIP, 10f, 10f, 96f, 36f, 30f, "A", 13f)))
        assertTrue(p.contains("rotated 30 degrees"))
    }

    @Test fun codecRoundTrips() {
        val list = DesignerDefaults.starter()
        val back = ScreenCodec.decode(ScreenCodec.encode(list))
        assertEquals(list.size, back.size)
        assertEquals(list[1].text, back[1].text)
        assertEquals(list[2].type, back[2].type)
        assertEquals(list[4].w, back[4].w, 0.001f)
    }

    @Test fun iconIsDescribedAndSurvivesCodec() {
        val icon = DesignElement("i", ElementType.ICON, 10f, 10f, 48f, 48f, 0f, "", 16f, icon = "shopping_cart", colorKey = "white")
        val p = PromptBuilder.build("x", null, listOf(icon))
        assertTrue(p.contains("\"shopping cart\" in the white"))
        val back = ScreenCodec.decode(ScreenCodec.encode(listOf(icon))).first()
        assertEquals("shopping_cart", back.icon)
        assertEquals("white", back.colorKey)
    }

    @Test fun oldSavedScreensWithoutIconFieldStillLoad() {
        val old = "[{\"id\":\"a\",\"type\":\"BUTTON\",\"x\":1,\"y\":2,\"w\":100,\"h\":40,\"rot\":0,\"text\":\"Go\",\"fs\":16}]"
        val e = ScreenCodec.decode(old).single()
        assertEquals("accent", e.colorKey)
        assertEquals("", e.icon)
        assertFalse(e.locked)
        assertEquals("", e.imageUri)
        assertTrue(e.imageCrop)
    }

    @Test fun imageUriAndCropSurviveCodec() {
        val imgEl = DesignElement(
            id = "img1",
            type = ElementType.IMAGE,
            x = 24f,
            y = 140f,
            w = 180f,
            h = 220f,
            text = "Gallery Cover",
            imageUri = "/data/user/0/com.nh.uiuxexplorer/files/designer_images/img_test.png",
            imageCrop = false
        )
        val decoded = ScreenCodec.decode(ScreenCodec.encode(listOf(imgEl))).single()
        assertEquals(imgEl.imageUri, decoded.imageUri)
        assertFalse(decoded.imageCrop)
    }

    @Test fun codecSurvivesGarbage() {
        assertTrue(ScreenCodec.decode("not json").isEmpty())
    }

    @Test fun hitTestHandlesRotation() {
        val el = DesignElement("r", ElementType.BUTTON, 100f, 180f, 160f, 40f, rotation = 90f)
        assertTrue(DesignerDefaults.hitTestElement(180f, 135f, el, slopUnits = 0f))
        assertFalse(DesignerDefaults.hitTestElement(110f, 200f, el, slopUnits = 0f))
    }

    @Test fun cornerAndEdgeResizeWorks() {
        val el = DesignElement("c", ElementType.CARD, 40f, 100f, 200f, 120f)
        val br = DesignerDefaults.resizeFromCorner(el, CornerHandle.BOTTOM_RIGHT, 30f, -20f)
        assertEquals(40f, br.x, 0.001f)
        assertEquals(100f, br.y, 0.001f)
        assertEquals(230f, br.w, 0.001f)
        assertEquals(100f, br.h, 0.001f)

        val right = DesignerDefaults.resizeFromHandle(el, ResizeHandle.RIGHT, 40f, 99f)
        assertEquals(240f, right.w, 0.001f)
        assertEquals(120f, right.h, 0.001f)
    }

    @Test fun lockedElementIgnoresResizeAndSurvivesCodec() {
        val lockedEl = DesignElement("lk", ElementType.IMAGE, 20f, 140f, 100f, 150f, locked = true)
        val afterResize = DesignerDefaults.resizeFromHandle(lockedEl, ResizeHandle.BOTTOM_RIGHT, 50f, 50f)
        assertEquals(100f, afterResize.w, 0.001f)
        assertEquals(150f, afterResize.h, 0.001f)

        val decoded = ScreenCodec.decode(ScreenCodec.encode(listOf(lockedEl))).single()
        assertTrue(decoded.locked)
    }

    @Test fun scaleAndAlignElementWork() {
        val el = DesignElement("b", ElementType.BUTTON, 80f, 200f, 200f, 50f, fontSize = 20f)
        val scaled = DesignerDefaults.scaleElement(el, 0.5f)
        assertEquals(100f, scaled.w, 0.001f)
        assertEquals(25f, scaled.h, 0.001f)
        assertEquals(130f, scaled.x, 0.001f)

        val col2 = DesignerDefaults.alignElement(scaled, ElementAlignment.COL_2)
        assertEquals(129f, col2.x, 0.001f)
        assertEquals(102f, col2.w, 0.001f)
    }

    @Test fun navIconsAndExplainTextRoundTrip() {
        WatermarkGuard.updateConfig(WatermarkAndAboutConfig())
        val nav = DesignElement("n", ElementType.BOTTOM_NAV, 0f, 716f, 360f, 64f, icon = "home,movie,favorite,person")
        assertEquals(listOf("home", "movie", "favorite", "person"), DesignerDefaults.bottomNavIcons(nav))

        val explain = DesignElement("ex", ElementType.EXPLAIN_TEXT, 16f, 400f, 312f, 78f, text = "3-column movie cards", colorKey = "#E50914")
        val breakdown = PromptBuilder.explainLayout("Netflix", style, listOf(nav, explain), "Streaming home screen")
        assertTrue(breakdown.contains("Streaming home screen"))
        assertTrue(breakdown.contains("3-column movie cards"))
        assertTrue(breakdown.contains("#E50914"))
        assertTrue(breakdown.contains("Watermark:"))
    }
}
