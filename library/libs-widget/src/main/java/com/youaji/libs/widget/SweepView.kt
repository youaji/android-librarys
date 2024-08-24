@file:Suppress("unused")
package com.youaji.libs.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * @author youaji
 * @since 2023/2/9
 */
class SweepView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val defaultWidth = 100
    private val defaultHeight = 100
    private var mWidth = 0
    private var mHeight = 0
    private var rectF: RectF? = null
    private var paint: Paint = Paint()
    private var mColor = Color.RED // 默认颜色为红色
    private var mSweep = 0f // 扇形角度

    init {
        paint.color = mColor            //画笔颜色
        paint.style = Paint.Style.FILL  //填充
        paint.isAntiAlias = true        //是否抗锯齿
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val resultWidth = measureWidth(widthMeasureSpec)
        val resultHeight = measureHeight(heightMeasureSpec)
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mHeight = h
        mWidth = w
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        // 画扇形
        rectF?.let { canvas.drawArc(it, -90f, mSweep, true, paint) }
    }

    /** 绘制的宽 */
    private fun measureWidth(widthMeasureSpec: Int): Int {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        var result: Int
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = defaultWidth
            if (mode == MeasureSpec.AT_MOST) {
                result = size.coerceAtMost(defaultWidth)
            }
        }
        return result
    }

    /** 绘制的高 */
    private fun measureHeight(heightMeasureSpec: Int): Int {
        val size = MeasureSpec.getSize(heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        var result: Int
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = defaultHeight
            if (mode == MeasureSpec.AT_MOST) {
                result = size.coerceAtMost(defaultHeight)
            }
        }
        return result
    }

    /** 设置扇形颜色 */
    fun setColor(color: Int) {
        mColor = color
        paint.color = mColor
        // 调用 onDraw 重绘
        invalidate()
    }

    /** 设置扇形的区域0-360 */
    fun setSweep(sweep: Float) {
        this.mSweep = sweep
        //调用 onDraw 重绘
        invalidate()
    }
}