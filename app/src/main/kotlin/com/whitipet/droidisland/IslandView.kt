package com.whitipet.droidisland

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.content.res.AppCompatResources

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

	private var mediaAppIcon: Drawable? = null
	private val playIcon: Drawable? by lazy {
		AppCompatResources.getDrawable(context, android.R.drawable.ic_media_play)?.also {
			it.setTint(Color.WHITE)
		}
	}

	private var iconSize: Float = 0.0f
	private var iconOffset: Float = 0.0f

	init {
		wm.currentWindowMetrics.windowInsets.displayCutout?.cutoutPath?.let { cutoutPath: Path ->
			cutoutPath.computeBounds(cutoutRect, false)
			cutoutExpandedRect.set(cutoutRect)
			cutoutExpandedRect.inset(-cutoutRect.width(), 0.0f)
			cutoutDrawRect.set(cutoutRect)
			radius = cutoutRect.height() / 2

			iconSize = cutoutRect.height() * 0.6f
			iconOffset = (cutoutRect.height() - iconSize) / 2
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		canvas.drawRoundRect(cutoutDrawRect, radius, radius, paint)

		mediaAppIcon?.let { icon ->
			icon.setBounds(
				(cutoutDrawRect.left + iconOffset * 2).toInt(),
				(cutoutDrawRect.top + iconOffset).toInt(),
				(cutoutDrawRect.left + iconOffset * 2 + iconSize).toInt(),
				(cutoutDrawRect.top + iconOffset + iconSize).toInt()
			)
			icon.draw(canvas)
		}

		playIcon?.let { icon ->
			icon.setBounds(
				(cutoutDrawRect.right - iconOffset - iconSize).toInt(),
				(cutoutDrawRect.top + iconOffset).toInt(),
				(cutoutDrawRect.right - iconOffset).toInt(),
				(cutoutDrawRect.top + iconOffset + iconSize).toInt()
			)
			icon.draw(canvas)
		}

		canvas.drawRoundRect(cutoutRect, radius, radius, paint)
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

	fun expand(postedNotification: Notification? = null) {
		postedNotification?.let { notification ->
			val notificationTemplate = notification.extras.get("android.template")
			if (notificationTemplate == "android.app.Notification\$MediaStyle") {
				mediaAppIcon = notification.getLargeIcon()?.loadDrawable(context)
				mediaAppIcon?.let {
					invalidate()

					if (!expandValueAnimator.isRunning && cutoutDrawRect != cutoutExpandedRect) expandValueAnimator.start()
				}
			}
		}
	}

	private val collapseValueAnimator: ValueAnimator = ValueAnimator().apply {
		setFloatValues(1.0f, 0.0f)
		duration = 500
		interpolator = AnticipateInterpolator()
		addUpdateListener(animatorUpdateListener)
		addListener(object : AnimatorListenerAdapter() {
			override fun onAnimationEnd(animation: Animator?) {
				mediaAppIcon = null
				invalidate()
			}
		})
	}

	fun collapse(removedNotification: Notification? = null) {
		if (!collapseValueAnimator.isRunning && cutoutDrawRect != cutoutRect) collapseValueAnimator.start()
	}
}