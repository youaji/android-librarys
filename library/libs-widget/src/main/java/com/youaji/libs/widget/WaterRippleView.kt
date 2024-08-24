@file:Suppress("unused")
package com.youaji.libs.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View

/**
 * 水波纹
 * 水波纹色值：ffca71  7.5%透明度，白色2px描边  透明度30%
 * 每隔一秒 画一个圆，每个圆放大四秒
 * @author youaji
 * @since 2023/1/17
 */
class WaterRippleView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var cx = 0f
    private var cy = 0f
    private var mWidthSpeed = 0
    private val startWidthList: MutableList<Int> = ArrayList()

    private var mPaint: Paint? = null
    private var mWhitePaint: Paint? = null
    private var mFrameCallback: Choreographer.FrameCallback? = null

    private val maxRadius: Int
        get() =
            if (width > height) height / 2
            else width / 2

    init {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint?.style = Paint.Style.FILL
        mPaint?.color = 0xffca71
        mWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mWhitePaint?.style = Paint.Style.STROKE
        mWhitePaint?.strokeWidth = dp2Px(1).toFloat()
        mWhitePaint?.color = 0x13ffffff
        mFrameCallback = Choreographer.FrameCallback { drawNext() }
        startWidthList.add(START_WIDTH)
    }

    fun start() {
        drawNext()
    }

    fun stop() {
        Choreographer.getInstance().removeFrameCallback(mFrameCallback)
    }

    private fun dp2Px(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    private fun drawNext() {
        invalidate()
        Choreographer.getInstance().postFrameCallback(mFrameCallback)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //  获取圆心位置
        cx = (width / 2).toFloat()
        cy = (height / 2).toFloat()
        mWidthSpeed = (maxRadius * 16 / DRAW_DURATION_MS).toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 颜色：完全透明
        setBackgroundColor(Color.TRANSPARENT)
        for (i in startWidthList.indices) {
            val startWidth = startWidthList[i]
            // 根据半径来计算alpha值
            val alpha = START_ALPHA - startWidth * START_ALPHA / maxRadius
            mPaint?.alpha = alpha.toInt()
            mWhitePaint?.alpha = alpha.toInt()
            // 画圆
            mPaint?.let { canvas.drawCircle(cx, cy, startWidth.toFloat(), it) }
            // 画圆环
            mWhitePaint?.let { canvas.drawCircle(cx, cy, startWidth.toFloat(), it) }

            // 同心圆扩散
            if (alpha > 0 && startWidth < maxRadius) {
                startWidthList[i] = (startWidth + mWidthSpeed)
            }
        }

        // 上一个大小达到5分之1则增加一个圆
        val addRadius = maxRadius / MAX_CIRCLE_NUM
        if (startWidthList[startWidthList.size - 1] > addRadius) {
            startWidthList.add(START_WIDTH)
        }

        /** 最多同时显示 [MAX_CIRCLE_NUM]个 */
        if (startWidthList.size > MAX_CIRCLE_NUM) {
            startWidthList.removeAt(0)
        }
    }

    companion object {
        /***/
        private const val START_ALPHA = 150f

        /***/
        private const val START_WIDTH = 0

        /***/
        private const val MAX_CIRCLE_NUM = 4

        /** 画一个圈的时间毫秒 */
        private const val DRAW_DURATION_MS = (3 * 1000).toLong()
    }
}