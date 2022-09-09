package com.whitipet.droidisland

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListenerService : NotificationListenerService() {

	override fun onNotificationPosted(sbn: StatusBarNotification?) {
		Log.d("NotificationListenerService", "onNotificationPosted() called with: sbn = $sbn")
	}
}