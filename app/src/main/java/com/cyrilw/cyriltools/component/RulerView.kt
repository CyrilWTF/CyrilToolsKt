package com.cyrilw.cyriltools.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.ceil

class RulerView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val LENGTH: Float = 96f
    }

    private val mPaint: Paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        strokeWidth = 2f
        isAntiAlias = true
        isDither = true
        textAlign = Paint.Align.RIGHT
        textSize = 40f
    }

    private val mPadding: Float = 16 * context.resources.displayMetrics.density
    private val mPixelPerInch: Float = context.resources.displayMetrics.xdpi
    private val mScaleGapPixel: Float = mPixelPerInch / 25.4f

    private var mPointerStartPixel: Float = 0f
    private var mScaleStartMillimeter: Float = 0f
    private var mPointerNowPixel: Float = mPadding
    private var isTouched: Boolean = false
    private var isExtensionNeeded: Boolean = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            val width = width.toFloat()
            val height = height
            var now: Float
            var scale: Int

            if (mScaleStartMillimeter % 1f == 0f) {
                now = mPadding
                scale = mScaleStartMillimeter.toInt()
            } else {
                val ceil = ceil(mScaleStartMillimeter)
                now = mPadding + (ceil - mScaleStartMillimeter) * mPixelPerInch / 25.4f
                scale = ceil.toInt()
            }

            while (now < height - mPadding) {
                when {
                    scale % 10 == 0 -> {
                        val start = width - LENGTH
                        drawLine(start, now, width, now, mPaint)
                        drawText((scale / 10).toString(), start - 16f, now + 16f, mPaint)
                    }
                    scale % 5 == 0 -> {
                        drawLine(width - 3 / 4f * LENGTH, now, width, now, mPaint)
                    }
                    else -> {
                        drawLine(width - LENGTH / 2f, now, width, now, mPaint)
                    }
                }
                now += mScaleGapPixel
                ++scale
            }

            drawCircle(
                2 * mPadding,
                mPointerNowPixel,
                mPadding,
                mPaint.apply { style = Paint.Style.STROKE }
            )
            drawLine(
                3 * mPadding,
                mPointerNowPixel,
                width,
                mPointerNowPixel,
                mPaint.apply { style = Paint.Style.FILL }
            )
        }

    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val offset = abs(event.y - mPointerNowPixel)
                if (offset < mPadding) {
                    mPointerStartPixel = mPointerNowPixel
                    isTouched = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTouched) {
                    val offset = event.y - mPointerStartPixel
                    if (abs(offset) > 4) {
                        val new = mPointerNowPixel + offset
                        if (offset > 0) {
                            isExtensionNeeded = false
                            mPointerNowPixel = if (new < height - mPadding) {
                                new
                            } else {
                                height - mPadding
                            }
                        } else {
                            isExtensionNeeded = true
                            val fix: Float
                            if (new > mPadding) {
                                fix = offset
                                mPointerNowPixel = new
                            } else {
                                fix = mPadding - mPointerNowPixel
                                mPointerNowPixel = mPadding
                            }
                            mScaleStartMillimeter += -fix * 25.4f / mPixelPerInch
                        }
                        mPointerStartPixel = mPointerNowPixel
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> isTouched = false
        }
        return true
    }

    fun reset() {
        mScaleStartMillimeter = 0f
        mPointerNowPixel = mPadding
        invalidate()
    }

}