package com.whitipet.droidisland

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
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

	private var _islandView: IslandView? = null
	private val islandView: IslandView?
		get() {
			val currentWindowMetrics = wm.currentWindowMetrics
			if (wm.isDisplayCutoutSuitable()) {
				if (this._islandView == null) _islandView = IslandView(this, wm)
				_islandView?.let { view ->
					if (view.parent != null) return view

					val statusBarInsets = wm.currentWindowMetrics.windowInsets.getInsets(WindowInsets.Type.statusBars())
					val layoutParamsOverlay = WindowManager.LayoutParams(
						currentWindowMetrics.bounds.width(),
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
						wm.addView(view, layoutParamsOverlay)
						return view
					} catch (e: Exception) {
						Log.d("OverlayService", "getIslandView() addView Exception: $e")
					}
				}
			}
			_islandView = null
			return null
		}

	fun onNotificationPosted(sbn: StatusBarNotification? = null) {
		Log.d("OverlayService", "onNotificationPosted() called with: sbn = ${sbn?.notification}")
		islandView?.expand(sbn?.notification)
	}

	fun onNotificationRemoved(sbn: StatusBarNotification? = null) {
		Log.d("OverlayService", "onNotificationRemoved() called with: sbn = ${sbn?.notification}")
		islandView?.collapse(sbn?.notification)
	}
}