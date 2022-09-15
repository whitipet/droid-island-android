package com.whitipet.droidisland

import android.view.DisplayCutout
import android.view.WindowManager
import androidx.annotation.IntDef

private val WindowManager.displayCutout: DisplayCutout? get() = currentWindowMetrics.windowInsets.displayCutout

// TODO
internal fun WindowManager.isDisplayCutoutSuitable(): Boolean = displayCutout != null

internal fun WindowManager.getDisplayCutoutSide(): DisplayCutoutSide {
	@Side var side: Int = Side.UNDEFINED
	var size = 0
	displayCutout?.let { cutout ->
		if (cutout.safeInsetTop != 0) {
			side = Side.TOP
			size = cutout.safeInsetTop
		} else if (cutout.safeInsetLeft != 0) {
			side = Side.LEFT
			size = cutout.safeInsetLeft
		} else if (cutout.safeInsetRight != 0) {
			side = Side.RIGHT
			size = cutout.safeInsetRight
		} else if (cutout.safeInsetBottom != 0) {
			side = Side.BOTTOM
			size = cutout.safeInsetBottom
		}
	}
	return DisplayCutoutSide(side, size)
}

internal data class DisplayCutoutSide(
	@Side val side: Int = Side.UNDEFINED,
	val size: Int,
)

@Retention(AnnotationRetention.SOURCE)
@IntDef(
	Side.UNDEFINED,
	Side.LEFT,
	Side.TOP,
	Side.RIGHT,
	Side.BOTTOM,
)
internal annotation class Side {
	companion object {
		const val UNDEFINED = 0
		const val LEFT = 1
		const val TOP = 2
		const val RIGHT = 3
		const val BOTTOM = 4
	}
}