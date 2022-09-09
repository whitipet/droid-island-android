package com.whitipet.droidisland

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class OverlayService : AccessibilityService() {

	override fun onAccessibilityEvent(event: AccessibilityEvent?) {
		Log.d("OverlayService", "onAccessibilityEvent() called with: event = $event")
	}

	override fun onInterrupt() {
		Log.d("OverlayService", "onInterrupt() called")
	}
}