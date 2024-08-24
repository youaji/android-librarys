@file:Suppress("unused")
package com.youaji.libs.widget.range.box

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.youaji.libs.widget.R
import kotlin.math.abs
import kotlin.math.roundToInt

class RangeBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr) {

    private val defaultTickStart = 0
    private val defaultTickEnd = 100
    private val defaultTickCount = 10
    private val defaultTickLineColor = Color.parseColor("#000000")
    private val defaultTickTextColor = Color.parseColor("#000000")
    private val defaultTickLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics).toInt()
    private val defaultTickTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, context.resources.displayMetrics).toInt()

    private val defaultThumbStartIndex = 1
    private val defaultThumbEndIndex = defaultTickCount
    private val defaultThumbColor = Color.parseColor("#00FF00")
    private val defaultThumbLineWidth = defaultTickLineWidth * 2

    private var isVertical = false

    private var linePaint: Paint

    private val tickView: TickView
    private val startThumb: ThumbView
    private val endThumb: ThumbView

    private var lineWidth: Int
    private var touchSlop: Int
    private var thumbWidth: Int

    private var originalX = 0
    private var originalY = 0
    private var lastX: Int = 0
    private var lastY: Int = 0

    private var tickStart = defaultTickStart
    private var tickEnd = defaultTickEnd
    private var tickCount = defaultTickCount

    private var isDragging = false
    private var changeListener: OnChangeListener? = null

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RangeBox, 0, 0)

        tickStart = array.getInteger(R.styleable.RangeBox_tickStart, defaultTickStart)
        tickEnd = array.getInteger(R.styleable.RangeBox_tickEnd, defaultTickEnd)
        tickCount = array.getInteger(R.styleable.RangeBox_tickCount, defaultTickCount)
        val tickLineWidth = array.getDimensionPixelOffset(R.styleable.RangeBox_tickLineWidth, defaultTickLineWidth)
        val tickLineColor = array.getColor(R.styleable.RangeBox_tickLineColor, defaultTickLineColor)
        val tickTextSize = array.getDimensionPixelOffset(R.styleable.RangeBox_tickTextSize, defaultTickTextSize)
        val tickTextColor = array.getColor(R.styleable.RangeBox_tickTextColor, defaultTickTextColor)

        val thumbStartIndex = array.getInteger(R.styleable.RangeBox_thumbStartIndex, defaultThumbStartIndex)
        val thumbEndIndex = array.getInteger(R.styleable.RangeBox_thumbEndIndex, defaultThumbEndIndex)
        val thumbColor = array.getColor(R.styleable.RangeBox_thumbColor, defaultThumbColor)
        lineWidth = array.getDimensionPixelOffset(R.styleable.RangeBox_thumbLineWidth, defaultThumbLineWidth)

        linePaint = Paint()
        linePaint.color = thumbColor

        touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        startThumb = ThumbView(context, lineWidth, thumbColor)
        endThumb = ThumbView(context, lineWidth, thumbColor)
        thumbWidth = startThumb.thumbWidth

        setTickCount(tickCount)
        setRangeIndex(thumbStartIndex, thumbEndIndex)

        tickView = TickView(context, tickCount, tickLineWidth, tickLineColor, tickTextSize, tickTextColor)
        array.recycle()

        addView(tickView)
        addView(startThumb)
        addView(endThumb)

        setWillNotDraw(false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        isVertical = heightMeasureSpec > widthMeasureSpec
        tickView.setVertical(isVertical)
        startThumb.setVertical(isVertical)
        endThumb.setVertical(isVertical)

        val measureSpecWidth =
            if (isVertical) widthMeasureSpec
            else MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY)
        val measureSpecHeight =
            if (isVertical) MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY)
            else heightMeasureSpec
        super.onMeasure(measureSpecWidth, measureSpecHeight)

        tickView.measure(measureSpecWidth, measureSpecHeight)
        startThumb.measure(
            if (isVertical) if (tickView.hasText) measureSpecWidth / 2 else measureSpecWidth else measureSpecWidth,
            if (isVertical) measureSpecHeight else if (tickView.hasText) measureSpecHeight / 2 else measureSpecHeight
        )
        endThumb.measure(
            if (isVertical) if (tickView.hasText) measureSpecWidth / 2 else measureSpecWidth else measureSpecWidth,
            if (isVertical) measureSpecHeight else if (tickView.hasText) measureSpecHeight / 2 else measureSpecHeight
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        tickView.layout(0, 0, tickView.measuredWidth, tickView.measuredHeight)
        startThumb.layout(0, 0, startThumb.measuredWidth, startThumb.measuredHeight)
        endThumb.layout(0, 0, endThumb.measuredWidth, endThumb.measuredHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        moveThumbByIndex(startThumb, startThumb.tickIndex)
        moveThumbByIndex(endThumb, endThumb.tickIndex)
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)

        val thumbGap = startThumb.gap

        val width = if (tickView.hasText) measuredWidth / 2f else measuredWidth.toFloat()
        val height = if (tickView.hasText) measuredHeight / 2f else measuredHeight.toFloat()

        val startThumbSize = if (isVertical) startThumb.measuredHeight else startThumb.measuredWidth
        val startThumbOffset = if (isVertical) startThumb.y else startThumb.x
        val endThumbOffset = if (isVertical) endThumb.y else endThumb.x

        val line1Left = if (isVertical) 0f else startThumbOffset + startThumbSize - thumbGap
        val line1Top = if (isVertical) startThumbOffset + startThumbSize - thumbGap else 0f
        val line1Right = if (isVertical) lineWidth.toFloat() else endThumbOffset + thumbGap
        val line1Bottom = if (isVertical) endThumbOffset + thumbGap else lineWidth.toFloat()

        val line2Left = if (isVertical) width - lineWidth else startThumbOffset + startThumbSize - thumbGap
        val line2Top = if (isVertical) startThumbOffset + startThumbSize - thumbGap else height - lineWidth
        val line2Right = if (isVertical) width else endThumbOffset + thumbGap
        val line2Bottom = if (isVertical) endThumbOffset + thumbGap else height

        canvas.drawRect(line1Left, line1Top, line1Right, line1Bottom, linePaint)
        canvas.drawRect(line2Left, line2Top, line2Right, line2Bottom, linePaint)
    }

    override fun onDraw(canvas: Canvas) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled)
            return false

        var handle = false
        val eventX: Int
        val eventY: Int

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                eventX = event.x.toInt()
                eventY = event.y.toInt()

                originalX = eventX
                originalY = eventY
                lastX = eventX
                lastY = eventY
                isDragging = false

                if (!startThumb.isPressed && startThumb.isInTarget(eventX, eventY)) {
                    startThumb.isPressed = true
                    handle = true
                } else if (!endThumb.isPressed && endThumb.isInTarget(eventX, eventY)) {
                    endThumb.isPressed = true
                    handle = true
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isDragging = false
                lastX = 0
                lastY = 0
                originalX = 0
                originalY = 0

                parent.requestDisallowInterceptTouchEvent(false)

                if (startThumb.isPressed) {
                    releaseStartThumb()
                    invalidate()
                    handle = true
                } else if (endThumb.isPressed) {
                    releaseEndThumb()
                    invalidate()
                    handle = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                eventX = event.x.toInt()
                eventY = event.y.toInt()

                if (isVertical) {
                    if (!isDragging && abs(eventY - originalY) > touchSlop) {
                        isDragging = true
                    }
                } else {
                    if (!isDragging && abs(eventX - originalX) > touchSlop) {
                        isDragging = true
                    }
                }

                if (isDragging) {
                    val movePixel = if (isVertical) eventY - lastY else eventX - lastX
                    if (startThumb.isPressed) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        moveStartThumbByPixel(movePixel)
                        handle = true
                        invalidate()
                    } else if (endThumb.isPressed) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        moveEndThumbByPixel(movePixel)
                        handle = true
                        invalidate()
                    }
                }

                lastX = eventX
                lastY = eventY
            }
        }

        return handle
    }

    private fun getNearestIndex(pixel: Float): Int =
        if (isVertical) (pixel / (measuredHeight / (tickCount + 1))).roundToInt()
        else (pixel / (measuredWidth / (tickCount + 1))).roundToInt()

    private val thumbStart
        get() = tickView.findLocationByFirstIndex() - thumbWidth / 2f
    private val thumbEnd
        get() = tickView.findLocationByIndex(tickCount) - thumbWidth / 2f

    private fun moveThumbByIndex(view: ThumbView, index: Int): Boolean {
        if (index < 0 || index > tickCount) return false
        val tickLocation = tickView.findLocationByIndex(index)
        if (isVertical) {
            view.y = (tickLocation - view.measuredHeight / 2).toFloat()
        } else {
            view.x = (tickLocation - view.measuredWidth / 2).toFloat()
        }
        if (view.tickIndex != index) {
            view.tickIndex = index
            return true
        }
        return false
    }

    private fun moveStartThumbByPixel(pixel: Int) {
        val moved = if (isVertical) startThumb.y + pixel else startThumb.x + pixel
        if (moved > thumbStart && moved < thumbEnd) {
            val isInRange =
                if (isVertical) moved < endThumb.y - thumbWidth && moved > 0
                else moved < endThumb.x - thumbWidth

            if (isInRange) {
                if (isVertical) startThumb.y = moved else startThumb.x = moved
                val index = getNearestIndex(moved)
                if (startThumb.tickIndex != index) {
                    startThumb.tickIndex = index
                    notifyChange(false)
                }
            }
        }
    }

    private fun moveEndThumbByPixel(pixel: Int) {
        val moved = if (isVertical) endThumb.y + pixel else endThumb.x + pixel
        if (moved > thumbStart && moved < thumbEnd) {
            val isInRange =
                if (isVertical) moved > startThumb.y + thumbWidth
                else moved > startThumb.x + thumbWidth

            if (isInRange) {
                if (isVertical) endThumb.y = moved else endThumb.x = moved
                val index = getNearestIndex(moved)
                if (endThumb.tickIndex != index) {
                    endThumb.tickIndex = index
                    notifyChange(false)
                }
            }
        }
    }

    private fun releaseStartThumb() {
        var index = getNearestIndex(if (isVertical) startThumb.y else startThumb.x)
        val endIndex = endThumb.tickIndex
        if (index >= endIndex) {
            index = endIndex - 1
        }
        moveThumbByIndex(startThumb, index)
        startThumb.isPressed = false
        notifyChange(true)
    }

    private fun releaseEndThumb() {
        var index = getNearestIndex(if (isVertical) endThumb.y else endThumb.x)
        val endIndex = startThumb.tickIndex
        if (index <= endIndex) {
            index = endIndex + 1
        }
        moveThumbByIndex(endThumb, index)
        endThumb.isPressed = false
        notifyChange(true)
    }

    private fun notifyChange(completed: Boolean) {
        val interval = (tickEnd - tickStart) / (tickCount - 1f)
        val start = if (startThumb.tickIndex == 1) tickStart.toFloat() else tickStart + (startThumb.tickIndex - 1) * interval
        val end = if (endThumb.tickIndex == tickCount) tickEnd.toFloat() else tickStart + (endThumb.tickIndex - 1) * interval
        Log.e("sdsf", "$interval $tickCount ${startThumb.tickIndex} ${endThumb.tickIndex}")
        changeListener?.onChanged(this, start, end, completed)
    }

    fun setChangeListener(listener: OnChangeListener) {
        changeListener = listener
    }

    fun setThumbColor(@ColorInt color: Int) {
        startThumb.setThumbColor(color)
        endThumb.setThumbColor(color)
        invalidate()
    }

    fun setThumbLineColor(@ColorInt color: Int) {
        startThumb.setLineColor(color)
        endThumb.setLineColor(color)
        invalidate()
    }

    fun setThumbLineWidth(width: Int) {
        lineWidth = width
        startThumb.setLineWidth(width)
        endThumb.setLineWidth(width)
        thumbWidth = startThumb.thumbWidth
        invalidate()
    }

    fun setTickLineColor(@ColorInt color: Int) {
        tickView.setLineColor(color)
        invalidate()
    }

    fun setTickLineWidth(width: Int) {
        tickView.setLineWidth(width)
        invalidate()
    }

    fun setTickTextColor(@ColorInt color: Int) {
        tickView.setTextColor(color)
        invalidate()
    }

    fun setTickTextSize(size: Int) {
        tickView.setTextSize(size)
        invalidate()
    }

    fun setTickText(textList: Array<String>) {
        if (textList.isEmpty()) return
        val textSize = textList.size
        tickView.setText(textList)
        setTickCount(textSize)
        if (startThumb.tickIndex >= textSize) {
            setRangeIndex(startIndex = textSize - 1)
        }
        invalidate()
    }

    fun setTickCount(count: Int) {
        if (count > 1) {
            tickCount = count
            endThumb.tickIndex = count
        } else {
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    fun setTickRange(start: Int, end: Int) {
        tickStart = start
        tickEnd = end
        invalidate()
    }

    fun setRange(start: Float, end: Float) {
//        if (start < tickStart || start > tickEnd || end < tickStart || end > tickEnd || start > end)
//            return
//
//
//        val tickLength = tickEnd - tickStart
//        val tickInterval = (tickEnd - tickStart) / tickCount
//        val s =  tickLength/start
//
//        val startIndex = (tickLength/start).roundToInt()
//        val endIndex = (tickLength/end).roundToInt()
//
//        if (startThumb.tickIndex != startIndex) {
//            startThumb.tickIndex = startIndex
//        }
//
//        if (endThumb.tickIndex != endIndex) {
//            endThumb.tickIndex = endIndex
//        }
//        invalidate()
    }

    fun setRangeIndex(
        startIndex: Int = startThumb.tickIndex,
        endIndex: Int = endThumb.tickIndex,
    ) {
        val indexOutOfRange = startIndex < 0 || startIndex > tickCount || endIndex < 0 || endIndex > tickCount
        require(!indexOutOfRange) {
            ("Thumb index start $startIndex, or end $endIndex is out of bounds. " +
                    "Check that it is greater than the minimum ($tickStart) " +
                    "and less than the maximum value ($tickEnd)")
        }

        if (startThumb.tickIndex != startIndex) {
            startThumb.tickIndex = startIndex
        }

        if (endThumb.tickIndex != endIndex) {
            endThumb.tickIndex = endIndex
        }
    }

    interface OnChangeListener {
        fun onChanged(view: RangeBox, start: Float, end: Float, completed: Boolean)
    }
}