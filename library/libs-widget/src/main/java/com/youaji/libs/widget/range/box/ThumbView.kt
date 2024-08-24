@file:Suppress("unused")
package com.youaji.libs.widget.range.box

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt

@SuppressLint("ViewConstructor")
class ThumbView constructor(
    context: Context,
    private var lineWidth: Int,
    color: Int,
) : View(context) {

    /** 刻度下标 */
    var tickIndex: Int = -1

    /** 整体宽度 */
    var thumbWidth: Int = lineWidth * 3
        private set

    /** 中间状态图标与线条之间的差距 */
    var gap = (thumbWidth - lineWidth) / 2
        private set

    private var pressed = false
    private var isVertical = false
    private val linePaint = Paint()
    private val statePaint = Paint()
    private val extendTouchSlop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics).toInt()

    init {
        linePaint.color = color
        statePaint.color = color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isVertical)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(thumbWidth, MeasureSpec.EXACTLY),
            )
        else
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(thumbWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY),
            )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(
            if (isVertical) 0f else gap.toFloat(),
            if (isVertical) gap.toFloat() else 0f,
            if (isVertical) measuredWidth.toFloat() else gap.toFloat() + lineWidth,
            if (isVertical) gap.toFloat() + lineWidth else measuredHeight.toFloat(),
            linePaint
        )

        canvas.drawCircle(
            if (isVertical) measuredWidth / 2f else thumbWidth / 2f,
            if (isVertical) thumbWidth / 2f else measuredHeight / 2f,
            thumbWidth / 2f,
            statePaint
        )
    }

    override fun isPressed(): Boolean = pressed

    override fun setPressed(pressed: Boolean) {
        this.pressed = pressed
    }

    fun isInTarget(x: Int, y: Int): Boolean {
        val rect = Rect()
        getHitRect(rect)
        rect.left -= extendTouchSlop
        rect.right += extendTouchSlop
        rect.top -= extendTouchSlop
        rect.bottom += extendTouchSlop
        return rect.contains(x, y)
    }

    fun setLineWidth(width: Int) {
        this.lineWidth = width
        this.thumbWidth = lineWidth * 3
        this.gap = (thumbWidth - lineWidth) / 2
    }

    fun setLineColor(@ColorInt color: Int) {
        this.linePaint.color = color
    }

    fun setThumbColor(@ColorInt color: Int) {
        this.statePaint.color = color
    }

    fun setVertical(vertical: Boolean) {
        isVertical = vertical
    }
}