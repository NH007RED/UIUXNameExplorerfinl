package com.nh.uiuxexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.security.WatermarkGuard
import com.nh.uiuxexplorer.ui.screens.AppNav
import com.nh.uiuxexplorer.ui.theme.UiuxTheme
import com.nh.uiuxexplorer.ui.vm.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enforce FLAG_SECURE (blocks screenshots & screen recording) and verify APK integrity before rendering UI
        WatermarkGuard.enforceAndVerifySecureWindow(this)
        super.onCreate(savedInstanceState)
        WatermarkGuard.enforceAndVerifySecureWindow(this)
        enableEdgeToEdge()
        setContent {
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            UiuxTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
                AppNav()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        WatermarkGuard.enforceAndVerifySecureWindow(this)
    }

    override fun onResume() {
        super.onResume()
        WatermarkGuard.enforceAndVerifySecureWindow(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WatermarkGuard.enforceAndVerifySecureWindow(this)
        }
    }
}
