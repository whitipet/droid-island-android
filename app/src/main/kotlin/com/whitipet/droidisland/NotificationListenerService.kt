package com.whitipet.droidisland

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListenerService : NotificationListenerService() {

	override fun onNotificationPosted(sbn: StatusBarNotification?) {
		Log.d("NotificationListenerService", "onNotificationPosted() called with: sbn = $sbn")
		sbn?.let { OverlayService.instance?.onNotificationPosted(sbn) }
	}

	override fun onNotificationRemoved(sbn: StatusBarNotification?) {
		Log.d("NotificationListenerService", "onNotificationRemoved() called with: sbn = $sbn")
		sbn?.let { OverlayService.instance?.onNotificationRemoved(sbn) }
	}
}