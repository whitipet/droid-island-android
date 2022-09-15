package com.whitipet.droidisland

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

class OverlayService : AccessibilityService() {

	//region OverlayService
	companion object {
		var instance: OverlayService? = null
	}

	override fun onServiceConnected() {
		instance = this

		init()
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

	private val lpIslandView: WindowManager.LayoutParams by lazy {
		WindowManager.LayoutParams(
			WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
			PixelFormat.TRANSPARENT
		)
	}

	private fun init() {
		if (islandView == null) {
			if (!wm.isDisplayCutoutSuitable()) return

			islandView = IslandView(this, wm)
			updateIslandViewLayout()
			addIslandView()
		} else {
			updateIslandViewLayout()
		}
	}

	private fun addIslandView() = islandView?.let { view ->
		try {
			wm.addView(view, lpIslandView)
		} catch (e: Exception) {
			Log.d("OverlayService", "getIslandView() addView Exception: $e")
		}
	}

	private fun updateIslandViewLayout() = islandView?.let { view ->
		val displayCutoutSide: DisplayCutoutSide = wm.getDisplayCutoutSide()
		when (displayCutoutSide.side) {
			Side.TOP -> {
				lpIslandView.x = 0
				lpIslandView.y = -displayCutoutSide.size
			}
			Side.LEFT -> {
				lpIslandView.x = -displayCutoutSide.size
				lpIslandView.y = 0
			}
			Side.RIGHT -> {
				lpIslandView.x = displayCutoutSide.size
				lpIslandView.y = 0
			}
			Side.BOTTOM -> {
				lpIslandView.x = 0
				lpIslandView.y = displayCutoutSide.size
			}
			Side.UNDEFINED -> {
				// TODO
			}
		}

		try {
			wm.updateViewLayout(view, lpIslandView)
		} catch (exception: Exception) {
			Log.d("OverlayService", "updateIslandViewLayout() updateViewLayout Exception: $exception")
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		updateIslandViewLayout()
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