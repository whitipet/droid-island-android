package com.whitipet.droidisland

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.content.Intent
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import android.view.accessibility.AccessibilityEvent

class OverlayService : AccessibilityService() {

	//region OverlayService
	companion object {
		var instance: OverlayService? = null
	}

	override fun onServiceConnected() {
		instance = this
	}

	override fun onUnbind(intent: Intent?): Boolean {
		instance = null
		return super.onUnbind(intent)
	}

	override fun onDestroy() {
		instance = null
		super.onDestroy()
	}

	override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//		Log.d("OverlayService", "onAccessibilityEvent() called with: event = $event")
	}

	override fun onInterrupt() {
//		Log.d("OverlayService", "onInterrupt() called")
	}
	//endregion OverlayService

	private val wm: WindowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
	private var islandView: IslandView? = null

	private fun getIslandView(): IslandView? {
		if (islandView == null) islandView = IslandView(this, wm)
		islandView?.let {
			if (it.parent == null) {
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
					wm.addView(it, layoutParamsOverlay)
				} catch (e: Exception) {
					Log.d("OverlayService", "getIslandView() addView Exception: $e")
				}
			}
			return it
		}
		return null
	}

	fun showNotification(notification: Notification? = null) {
		Log.d("OverlayService", "showNotification() called with: notification = $notification")
		getIslandView()?.expand()
	}

	fun hideNotification(notification: Notification? = null) {
		Log.d("OverlayService", "hideNotification() called with: notification = $notification")
		getIslandView()?.collapse()
	}
}