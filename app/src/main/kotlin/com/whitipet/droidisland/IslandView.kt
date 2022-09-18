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
internal class IslandView constructor(context: Context, private val wm: WindowManager) : View(context) {

	private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
	}

	@Side private var cutoutSide: Int = Side.UNDEFINED

	private val cutoutBoundsRect = RectF()
	private val cutoutExpandedBoundsRect = RectF()

	private val cutoutDrawBoundsRect = RectF()
	private var cutoutDrawRadius: Float = 0.0f

	private val cutoutCenterPoint: PointF = PointF()

	private var mediaAppIcon: Drawable? = null
	private val playIcon: Drawable? by lazy {
		AppCompatResources.getDrawable(context, android.R.drawable.ic_media_play)?.also {
			it.setTint(Color.WHITE)
		}
	}

	private var iconSize: Float = 0.0f
	private var iconOffset: Float = 0.0f

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)

		cutoutSide = wm.getDisplayCutoutSide().side

		if (cutoutSide == Side.UNDEFINED) return

		wm.displayCutout?.cutoutPath?.let { cutoutPath: Path ->
			cutoutPath.computeBounds(cutoutBoundsRect, false)
			cutoutExpandedBoundsRect.set(cutoutBoundsRect)
			cutoutExpandedBoundsRect.inset(-cutoutBoundsRect.width(), 0.0f)

			cutoutCenterPoint.set(cutoutBoundsRect.centerX(), cutoutBoundsRect.centerY())

			cutoutDrawBoundsRect.set(cutoutBoundsRect)
			cutoutDrawRadius = cutoutBoundsRect.height() / 2

			iconSize = cutoutBoundsRect.height() * 0.6f
			iconOffset = (cutoutBoundsRect.height() - iconSize) / 2
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		if (cutoutSide == Side.UNDEFINED) return

		canvas.rotate(when (cutoutSide) {
			Side.LEFT -> -90.0f
			Side.RIGHT -> 90.0f
			else -> 0.0f
		}, cutoutCenterPoint.x, cutoutCenterPoint.y)

		canvas.drawRoundRect(cutoutDrawBoundsRect, cutoutDrawRadius, cutoutDrawRadius, paint)

		mediaAppIcon?.let { icon ->
			icon.setBounds((cutoutDrawBoundsRect.left + iconOffset * 2).toInt(),
				(cutoutDrawBoundsRect.top + iconOffset).toInt(),
				(cutoutDrawBoundsRect.left + iconOffset * 2 + iconSize).toInt(),
				(cutoutDrawBoundsRect.top + iconOffset + iconSize).toInt())
			icon.draw(canvas)
		}

		playIcon?.let { icon ->
			icon.setBounds((cutoutDrawBoundsRect.right - iconOffset - iconSize).toInt(),
				(cutoutDrawBoundsRect.top + iconOffset).toInt(),
				(cutoutDrawBoundsRect.right - iconOffset).toInt(),
				(cutoutDrawBoundsRect.top + iconOffset + iconSize).toInt())
			icon.draw(canvas)
		}

		canvas.drawRoundRect(cutoutBoundsRect, cutoutDrawRadius, cutoutDrawRadius, paint)
	}

	private val animatorUpdateListener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
		val animatorValue: Float = it.animatedValue as Float

		cutoutDrawBoundsRect.left =
			cutoutBoundsRect.left - ((cutoutBoundsRect.left - cutoutExpandedBoundsRect.left) * animatorValue)
		cutoutDrawBoundsRect.right =
			cutoutBoundsRect.right + ((cutoutExpandedBoundsRect.right - cutoutBoundsRect.right) * animatorValue)

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

					if (!expandValueAnimator.isRunning && cutoutDrawBoundsRect != cutoutExpandedBoundsRect) expandValueAnimator.start()
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
			override fun onAnimationEnd(animation: Animator) {
				mediaAppIcon = null
				invalidate()
			}
		})
	}

	fun collapse(removedNotification: Notification? = null) {
		if (!collapseValueAnimator.isRunning && cutoutDrawBoundsRect != cutoutBoundsRect) collapseValueAnimator.start()
	}
}