@file:Suppress("unused")
package com.youaji.libs.widget.range.box

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt

@SuppressLint("ViewConstructor")
class TickView constructor(
    context: Context,
    private var count: Int,
    private var lineWidth: Int,
    lineColor: Int,
    textSize: Int,
    textColor: Int,
) : View(context) {

    private var isVertical = false
    private val linePaint = Paint()
    private val textPaint = Paint()
    private var intervalPixel = 0
    private var textList = arrayOf<String>()
    val hasText
        get() = textList.isNotEmpty()

    init {
        linePaint.color = lineColor
        textPaint.color = textColor
        textPaint.textSize = textSize.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        intervalPixel =
            if (isVertical) (measuredHeight - lineWidth * count) / (count + 1)
            else (measuredWidth - lineWidth * count) / (count + 1)
    }

    override fun onDraw(canvas: Canvas) {
        drawTickLine(canvas)
        drawTickText(canvas)
    }

    private fun drawTickLine(canvas: Canvas?) {
        val line1Left = 0f
        val line1Top = 0f
        val line1Right = if (isVertical) lineWidth.toFloat() else measuredWidth.toFloat()
        val line1Bottom = if (isVertical) measuredHeight.toFloat() else lineWidth.toFloat()

        val width = if (hasText) measuredWidth / 2f else measuredWidth.toFloat()
        val height = if (hasText) measuredHeight / 2f else measuredHeight.toFloat()

        val line2Left = if (isVertical) width - lineWidth else 0f
        val line2Top = if (isVertical) 0f else height - lineWidth
        val line2Right = if (isVertical) width else measuredWidth.toFloat()
        val line2Bottom = if (isVertical) measuredHeight.toFloat() else height

        canvas?.drawRect(line1Left, line1Top, line1Right, line1Bottom, linePaint)
        canvas?.drawRect(line2Left, line2Top, line2Right, line2Bottom, linePaint)

        for (index in 1..count) {
            val start = intervalPixel * index + lineWidth * (index - 1f)
            canvas?.drawRect(
                if (isVertical) lineWidth.toFloat() else start,
                if (isVertical) start else lineWidth.toFloat(),
                if (isVertical) width - lineWidth else lineWidth + start,
                if (isVertical) lineWidth + start else height - lineWidth,
                linePaint,
            )
        }
    }

    private fun drawTickText(canvas: Canvas?) {
        if (textList.isEmpty()) return

        val textBounds = Rect()
        val textCanvasSize =
            if (isVertical) measuredWidth / 2f
            else measuredHeight / 2f

        textList.forEachIndexed { index, text ->
            val tickLineCenter = intervalPixel * (index + 1) + lineWidth * index + lineWidth / 2
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val textWidth = textBounds.width()
            val textHeight = textBounds.height()
            val textX =
                if (isVertical) textCanvasSize + (textCanvasSize / 2) - (textWidth / 2)
                else tickLineCenter - (textWidth / 2f)
            val textY =
                if (isVertical) tickLineCenter + (textHeight / 2f)
                else textCanvasSize + textHeight
            canvas?.drawText(text, textX, textY, textPaint)
        }
    }

    fun setVertical(vertical: Boolean) {
        isVertical = vertical
    }

    fun setLineWidth(width: Int) {
        lineWidth = width
    }

    fun setLineColor(@ColorInt color: Int) {
        linePaint.color = color
    }

    fun setTextSize(size: Int) {
        textPaint.textSize = size.toFloat()
    }

    fun setTextColor(@ColorInt color: Int) {
        textPaint.color = color
    }

    fun setText(textList: Array<String>) {
        this.textList = textList
        this.count = textList.size
    }

    /**
     * @param index 1 <= index <= count
     * @return 位置坐标 x or y 值
     */
    fun findLocationByIndex(index: Int): Int {
        return intervalPixel * index + lineWidth * (index - 1) + lineWidth / 2
    }

    fun findLocationByFirstIndex(): Int = findLocationByIndex(1)

}