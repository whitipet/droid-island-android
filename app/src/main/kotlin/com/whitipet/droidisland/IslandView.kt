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

	private val cutoutRect = RectF()
	private val cutoutExpandedRect = RectF()
	private val cutoutDrawRect = RectF()
	private var radius: Float = 0.0f

	init {
		wm.currentWindowMetrics.windowInsets.displayCutout?.cutoutPath?.let { cutoutPath: Path ->
			cutoutPath.computeBounds(cutoutRect, false)
			cutoutExpandedRect.set(cutoutRect)
			cutoutExpandedRect.inset(-cutoutRect.width(), 0.0f)
			cutoutDrawRect.set(cutoutRect)
			radius = cutoutRect.height() / 2
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		canvas.drawRoundRect(cutoutDrawRect, radius, radius, paint)
	}

	private val animatorUpdateListener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
		val animatorValue: Float = it.animatedValue as Float

		cutoutDrawRect.left = cutoutRect.left - ((cutoutRect.left - cutoutExpandedRect.left) * animatorValue)
		cutoutDrawRect.right = cutoutRect.right + ((cutoutExpandedRect.right - cutoutRect.right) * animatorValue)

		invalidate()
	}

	private val expandValueAnimator: ValueAnimator = ValueAnimator().apply {
		setFloatValues(0.0f, 1.0f)
		duration = 500
		interpolator = OvershootInterpolator()
		addUpdateListener(animatorUpdateListener)
	}

	fun expand() {
		if (!expandValueAnimator.isRunning && cutoutDrawRect != cutoutExpandedRect) expandValueAnimator.start()
	}

	private val collapseValueAnimator: ValueAnimator = ValueAnimator().apply {
		setFloatValues(1.0f, 0.0f)
		duration = 500
		interpolator = AnticipateInterpolator()
		addUpdateListener(animatorUpdateListener)
	}

	fun collapse() {
		if (!collapseValueAnimator.isRunning && cutoutDrawRect != cutoutRect) collapseValueAnimator.start()
	}
}