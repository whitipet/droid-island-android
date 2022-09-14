package com.whitipet.droidisland

import android.view.WindowManager

internal fun WindowManager.isDisplayCutoutExist(): Boolean = currentWindowMetrics.windowInsets.displayCutout != null

// TODO
internal fun WindowManager.isDisplayCutoutSuitable(): Boolean = isDisplayCutoutExist()