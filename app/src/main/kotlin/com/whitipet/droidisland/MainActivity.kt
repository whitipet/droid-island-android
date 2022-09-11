package com.whitipet.droidisland

import android.content.Intent
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(R.layout.activity_main) {

	fun onClick(view: View) {
		when (view.id) {
			R.id.btn_accessibility -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
			R.id.btn_notifications -> startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

//			R.id.btn_simulate_notification_posted -> OverlayService.instance?.onNotificationPosted()
//			R.id.btn_simulate_notification_removed -> OverlayService.instance?.onNotificationRemoved()
		}
	}
}