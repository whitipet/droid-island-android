package com.whitipet.droidisland

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import android.view.accessibility.AccessibilityEvent

class OverlayService : AccessibilityService() {

	private val wm: WindowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

	override fun onServiceConnected() {
		val viewOverlay = IslandView(this, wm)

		val windowMetrics: WindowMetrics = wm.currentWindowMetrics
		val statusBarInsets = windowMetrics.windowInsets.getInsets(WindowInsets.Type.statusBars())

		val layoutParamsOverlay = WindowManager.LayoutParams(
			windowMetrics.bounds.width(),
			statusBarInsets.top,
			WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
			PixelFormat.TRANSPARENT
		).apply {
			gravity = Gravity.TOP
			y = -statusBarInsets.top
		}

		try {
			wm.addView(viewOverlay, layoutParamsOverlay)
		} catch (e: Exception) {
			Log.d("OverlayService", "wm addView Exception: $e")
		}

		viewOverlay.expand()
	}

	override fun onAccessibilityEvent(event: AccessibilityEvent?) {
		Log.d("OverlayService", "onAccessibilityEvent() called with: event = $event")
	}

	override fun onInterrupt() {
		Log.d("OverlayService", "onInterrupt() called")
	}
}