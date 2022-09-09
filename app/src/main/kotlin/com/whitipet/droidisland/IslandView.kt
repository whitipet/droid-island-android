package com.whitipet.droidisland

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator

@SuppressLint("ViewConstructor")
internal class IslandView constructor(context: Context, wm: WindowManager) : View(context) {

	private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
	}

	private val displayCutoutBounds = RectF()
	private var radius: Float = 0.0f

	private var initWidth: Float = displayCutoutBounds.width()
	private var initLeft: Float = displayCutoutBounds.left
	private var initRight: Float = displayCutoutBounds.right

	init {
		wm.currentWindowMetrics.windowInsets.displayCutout?.cutoutPath?.let { cutoutPath: Path ->
			cutoutPath.computeBounds(displayCutoutBounds, false)
			radius = displayCutoutBounds.height() / 2

			initWidth = displayCutoutBounds.width()
			initLeft = displayCutoutBounds.left
			initRight = displayCutoutBounds.right
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		canvas.drawRoundRect(displayCutoutBounds, radius, radius, paint)
	}

	fun expand() {
		ValueAnimator().apply {
			setFloatValues(0.0f, 1.0f)
			duration = 500
			interpolator = OvershootInterpolator()
			addUpdateListener { animation ->
				val changeBy = initWidth * (animation.animatedValue as Float) * 2
				displayCutoutBounds.inset(changeBy, 0.0f)
				displayCutoutBounds.left = initLeft - changeBy
				displayCutoutBounds.right = initRight + changeBy
				invalidate()
			}
		}.start()
	}

	fun collapse() {
		ValueAnimator().apply {
			setFloatValues(1.0f, 0.0f)
			duration = 500
			interpolator = AnticipateInterpolator()
			addUpdateListener { animation ->
				val changeBy = initWidth * (animation.animatedValue as Float) * 2
				displayCutoutBounds.inset(changeBy, 0.0f)
				displayCutoutBounds.left = initLeft - changeBy
				displayCutoutBounds.right = initRight + changeBy
				invalidate()
			}
		}.start()
	}
}