package com.whitipet.droidisland

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator

@SuppressLint("ViewConstructor")
internal class IslandView constructor(context: Context, wm: WindowManager) : View(context) {

	private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
	}

	private val displayCutoutBounds = RectF()
	private var radius: Float = 0.0f

	init {
		wm.currentWindowMetrics.windowInsets.displayCutout?.cutoutPath?.let { cutoutPath: Path ->
			cutoutPath.computeBounds(displayCutoutBounds, false)
			radius = displayCutoutBounds.height() / 2
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		canvas.drawRoundRect(displayCutoutBounds, radius, radius, paint)
	}

	private val expandAnimator = ValueAnimator()

	fun expand() {
		val initWidth = displayCutoutBounds.width()
		val initLeft = displayCutoutBounds.left
		val initRight = displayCutoutBounds.right

		expandAnimator.setFloatValues(0.0f, 1.0f)
		expandAnimator.duration = 500
		expandAnimator.interpolator = OvershootInterpolator()
		expandAnimator.addUpdateListener { animation ->
			val animatedValue = animation.animatedValue as Float
			val expandBy = initWidth * animatedValue * 2
			displayCutoutBounds.inset(expandBy, 0.0f)
			displayCutoutBounds.left = initLeft - expandBy
			displayCutoutBounds.right = initRight + expandBy
			invalidate()
		}
		expandAnimator.start()
	}
}