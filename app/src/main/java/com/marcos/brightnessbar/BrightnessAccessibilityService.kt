package com.marcos.brightnessbar

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class BrightnessAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var prefs: AppPreferences
    private var lastSetBrightness: Int = -1
    private var prefsListener: android.content.SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = AppPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        if (prefs.isEnabled) {
            setupOverlay()
        }

        prefsListener = prefs.registerListener { key ->
            if (key == "is_enabled") {
                if (prefs.isEnabled) {
                    setupOverlay()
                } else {
                    removeOverlay()
                }
            }
        }
    }

    private fun setupOverlay() {
        if (overlayView != null) return

        val statusBarHeight = getStatusBarHeight()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            statusBarHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        overlayView = View(this).apply {
            setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0f
                private var initialBrightness = 0

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = event.rawX
                            initialBrightness = getCurrentBrightness()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = event.rawX - initialX
                            val screenWidth = resources.displayMetrics.widthPixels
                            val brightnessDelta = (deltaX / screenWidth * 255).toInt()
                            val newBrightness = (initialBrightness + brightnessDelta).coerceIn(0, 255)
                            
                            if (newBrightness != lastSetBrightness) {
                                updateBrightness(newBrightness)
                                lastSetBrightness = newBrightness
                            }
                        }
                    }
                    return true
                }
            })
        }
        windowManager?.addView(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    private fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
    }

    private fun updateBrightness(value: Int) {
        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 100
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {
        removeOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        prefsListener?.let { prefs.unregisterListener(it) }
        removeOverlay()
    }
}
