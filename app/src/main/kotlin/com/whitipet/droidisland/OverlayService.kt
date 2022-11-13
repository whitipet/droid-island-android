package com.whitipet.droidisland

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.Gravity
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
		destroy()
		super.onDestroy()
	}

	override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//		Log.d("OverlayService", "onAccessibilityEvent() called with: event = $event")
	}

	override fun onInterrupt() {
//		Log.d("OverlayService", "onInterrupt() called")
	}
	//endregion OverlayService

	private fun init() {
		initIslandView()
		initMediaSessionsListener()
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		updateIslandViewLayout()
	}

	private fun destroy() {
		Log.d("OverlayService", "destroy() called")
		removeIslandView()
		removeMediaSessionsChangedListener()
	}

	//region IslandView
	private val wm: WindowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

	private val lpIslandView: WindowManager.LayoutParams by lazy {
		WindowManager.LayoutParams(
			WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
			PixelFormat.TRANSPARENT
		)
	}

	private var islandView: IslandView? = null

	private fun initIslandView() {
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
			Log.d("OverlayService", "addIslandView() addView Exception: $e")
		}
	}

	private fun removeIslandView() = islandView?.let { view ->
		try {
			wm.removeView(view)
		} catch (e: Exception) {
			Log.d("OverlayService", "removeIslandView() removeView Exception: $e")
		}
	}

	@SuppressLint("RtlHardcoded")
	private fun updateIslandViewLayout() = islandView?.let { view ->
		lpIslandView.width = wm.currentWindowMetrics.bounds.width()
		lpIslandView.height = wm.currentWindowMetrics.bounds.height()

		val displayCutoutSide: DisplayCutoutSide = wm.getDisplayCutoutSide()
		when (displayCutoutSide.side) {
			Side.TOP -> {
				lpIslandView.gravity = Gravity.TOP

				lpIslandView.x = 0
				lpIslandView.y = -displayCutoutSide.size
			}
			Side.LEFT -> {
				lpIslandView.gravity = Gravity.LEFT

				lpIslandView.x = -displayCutoutSide.size
				lpIslandView.y = 0
			}
			Side.RIGHT -> {
				lpIslandView.gravity = Gravity.RIGHT

				lpIslandView.x = -displayCutoutSide.size
				lpIslandView.y = 0
			}
			Side.BOTTOM -> {
				lpIslandView.gravity = Gravity.BOTTOM

				lpIslandView.x = 0
				lpIslandView.y = -displayCutoutSide.size
			}
			Side.UNDEFINED -> Log.d("OverlayService", "updateIslandViewLayout Side.UNDEFINED")
		}

		try {
			wm.updateViewLayout(view, lpIslandView)
		} catch (exception: Exception) {
			Log.d("OverlayService", "updateIslandViewLayout() updateViewLayout Exception: $exception")
		}
	}
	//endregion IslandView

	//region MediaSessionsListener
	private val mediaSessionManager: MediaSessionManager by lazy { getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager }
	private val componentNotificationListener: ComponentName by lazy {
		ComponentName(this, NotificationListenerService::class.java)
	}

	private val onActiveMediaSessionsChangedListener =
		MediaSessionManager.OnActiveSessionsChangedListener { onActiveMediaSessionsChanged(it) }

	private fun initMediaSessionsListener() {
		addMediaSessionsChangedListener()
		onActiveMediaSessionsChanged(mediaSessionManager.getActiveSessions(componentNotificationListener))
	}

	private fun addMediaSessionsChangedListener() =
		mediaSessionManager.addOnActiveSessionsChangedListener(
			onActiveMediaSessionsChangedListener,
			componentNotificationListener
		)

	private fun removeMediaSessionsChangedListener() =
		mediaSessionManager.removeOnActiveSessionsChangedListener(onActiveMediaSessionsChangedListener)
	//endregion MediaSessionsListener

	fun onNotificationPosted(sbn: StatusBarNotification? = null) {
		Log.d("OverlayService", "onNotificationPosted() called with: sbn = ${sbn?.notification}")
		islandView?.expand(sbn?.notification)
	}

	fun onNotificationRemoved(sbn: StatusBarNotification? = null) {
		Log.d("OverlayService", "onNotificationRemoved() called with: sbn = ${sbn?.notification}")
		islandView?.collapse(sbn?.notification)
	}

	private fun onActiveMediaSessionsChanged(mediaControllers: List<MediaController>?) {
		Log.d("OverlayService", "onActiveSessionsChanged() called with: mediaControllers = $mediaControllers")
	}
}