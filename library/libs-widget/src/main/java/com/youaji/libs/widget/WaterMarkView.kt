package com.youaji.libs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * @author youaji
 * @since 2024/2/26
 */
class WaterMarkView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {
    companion object {
        const val DEFAULT_RADIAN: Float = (PI / 6).toFloat()
        private var DEFAULT_COLOR: Int = Color.parseColor("#808080")
    }

    var markText: CharSequence = "MARK"
    var markerSpace = 0//间距
    var markerTextColor: Int = DEFAULT_COLOR
    var markerTextSize = 0f
    var lineSpace: Int = 5                      //行间距
    var lineHeight = 0                          //px
    var radian = DEFAULT_RADIAN                 //弧度
    var textAlpha: Float = 0.2f
        set(value) {
            field = when {
                value > 1 -> 1f
                value < 0.2 -> 0.2f
                else -> value
            }
            alpha = field
        }

    private val path = Path()
    private var singleMarkerWidth = 0
    private var singleMarkerHeight = 0
    private var deltaFixSpace = 0//修正间距
    private var repeatCountX = 1
    private var repeatCountY = 1

    //多行修正
    private var fixedX: Int = 0
    private var fixedY: Int = 0
    private var fixedStartX: Int = 0
    private var fixedStartY: Int = 0
    private var hasCalculatorMarkerWidth: Boolean = false

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WaterMarkView)
        markText = a.getString(R.styleable.WaterMarkView_markerText) ?: "MARK"
        lineHeight = a.getDimensionPixelOffset(R.styleable.WaterMarkView_lineHeight, dp2px(50f))
        markerTextSize = a.getDimension(R.styleable.WaterMarkView_markerTextSize, 48f)
        markerTextColor = a.getColor(R.styleable.WaterMarkView_markerTextColor, DEFAULT_COLOR)
        markerSpace = a.getDimension(R.styleable.WaterMarkView_markerSpace, dp2px(30f).toFloat()).toInt()
        deltaFixSpace = a.getDimension(R.styleable.WaterMarkView_fixSpace, markerSpace.toFloat() / 2).toInt()
        radian = a.getFloat(R.styleable.WaterMarkView_radian, radian)
        textAlpha = a.getFloat(R.styleable.WaterMarkView_android_alpha, 0.2f)
        a.recycle()
        alpha = textAlpha
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = markerTextColor
        val markerTexts = markText.split("\n")
        if (!hasCalculatorMarkerWidth) {
            calculatorMarkerWidth(markerTexts)
        }
        canvas.rotate(radian)
        for ((index, value) in markerTexts.withIndex()) {
            drawWaterMarker(value, index, canvas)
        }
    }

    /**
     * 计算水印每行宽度、高度 获取最大值
     */
    private fun calculatorMarkerWidth(markerTexts: List<String>) {
        val rectText = Rect()
        var tempWidth: Int
        var tempHeight: Int
        markerTexts.forEach {
            paint.textSize = markerTextSize
            paint.getTextBounds(it, 0, it.length, rectText)
            tempWidth = rectText.width()
            tempHeight = rectText.height()
            singleMarkerWidth = if (tempWidth > singleMarkerWidth) tempWidth else singleMarkerWidth
            singleMarkerHeight =
                if (tempHeight > singleMarkerHeight) tempHeight else singleMarkerHeight
        }
        repeatCountX =
            ceil(width * 1.0 / abs(cos(radian)) / (singleMarkerWidth + markerSpace)).toInt()
        repeatCountY = floor(height * 1.0 / lineHeight).toInt()
        fixedX = ((singleMarkerHeight + lineSpace) * sin(radian)).toInt()
        fixedY = ((singleMarkerHeight + lineSpace) * cos(radian)).toInt()
        fixedStartX = ((singleMarkerWidth + markerSpace) * cos(radian)).toInt()
        fixedStartY = abs(((singleMarkerWidth + markerSpace) * sin(radian)).toInt())
        hasCalculatorMarkerWidth = true
    }

    private fun drawWaterMarker(text: String, lineNumber: Int, canvas: Canvas) {
        val rectText = Rect()
        paint.getTextBounds(text, 0, text.length, rectText)
        paint.textSize = markerTextSize
        val itemStdHeight = getItemHeight()
        var start = 1
        var end = repeatCountY
        val flag = if (radian > 0) -1 else 1
        if (radian > 0) end += width / lineHeight else start -= width / lineHeight
        for (i in start..end) {
            val x = getEndX(itemStdHeight, i) + deltaFixSpace + fixedX * lineNumber
            val y = getEndY(itemStdHeight, i) + fixedY * lineNumber
            var sx: Float
            var sy: Float
            for (j in 0..repeatCountX) {
                path.reset()
                sx = 0f + deltaFixSpace + fixedX * lineNumber + fixedStartX * j
                sy = i * lineHeight.toFloat() + fixedY * lineNumber + flag * fixedStartY * j
                if (sx >= x) continue
                path.moveTo(sx, sy)
                path.lineTo(x, y)
                canvas.drawTextOnPath(text, path, 0f, 0f, paint)
            }
        }
    }


    private fun isEnoughHeight(itemHeight: Float, times: Int) = itemHeight <= times * lineHeight

    private fun getEndX(itemHeight: Float, times: Int): Float =
        if (isEnoughHeight(itemHeight, times)) width.toFloat()
        else (lineHeight * times * 1.0f / tan(radian))

    private fun getEndY(itemHeight: Float, times: Int): Float =
        if (isEnoughHeight(itemHeight, times)) (lineHeight * times - itemHeight)
        else 0f

    private fun getItemHeight(): Float {
        return width * tan(radian)
    }

    private fun dp2px(dipValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}