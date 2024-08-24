@file:Suppress("unused")
package com.youaji.libs.widget.shine

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


/**
 * @author youaji
 * @since 2023/4/7
 */
abstract class PorterImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val porterDuffXferMode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    private var maskCanvas: Canvas? = null
    private var maskBitmap: Bitmap? = null
    private var maskPaint: Paint

    private var drawableCanvas: Canvas? = null
    private var drawableBitmap: Bitmap? = null
    private var drawablePaint: Paint? = null

    private var paintColor = Color.GRAY

    private var mWidth = 0
    private var mHeight = 0
    private var invalidated = true

    init {
        if (scaleType == ScaleType.FIT_CENTER) {
            scaleType = ScaleType.CENTER_CROP
        }

        maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskPaint.color = Color.BLACK
    }

    override fun invalidate() {
        invalidated = true
        super.invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createMaskCanvas(w, h, oldw, oldh)
    }

    private fun createMaskCanvas(width: Int, height: Int, oldw: Int, oldh: Int) {
        val sizeChanged = width != oldw || height != oldh
        val isValid = width > 0 && height > 0
        if (isValid && (maskCanvas == null || sizeChanged)) {
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            maskCanvas = Canvas()
            maskCanvas?.setBitmap(maskBitmap)

            mWidth = width
            mHeight = height

            maskPaint.reset()
            paintMaskCanvas(maskCanvas, maskPaint, width, height)

            drawableCanvas = Canvas()
            drawableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            drawableCanvas?.setBitmap(drawableBitmap)

            drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            drawablePaint?.color = paintColor

            invalidated = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var wMeasureSpec = widthMeasureSpec
        var hMeasureSpec = heightMeasureSpec
        if (wMeasureSpec == 0) {
            wMeasureSpec = 50
        }
        if (hMeasureSpec == 0) {
            hMeasureSpec = 50
        }
        super.onMeasure(wMeasureSpec, hMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        if (isInEditMode) {
            super.onDraw(canvas)
        } else {
            val saveCount = canvas.saveLayer(0.0f, 0.0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
            try {
                if (invalidated) {
                    drawableBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
                    drawableCanvas?.setBitmap(drawableBitmap)

                    drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    drawablePaint?.color = paintColor

                    drawable?.let {
                        invalidated = false
                        val imageMatrix = imageMatrix
                        if (imageMatrix == null) { // && mPaddingTop == 0 && mPaddingLeft == 0) {
                            drawableCanvas?.let { drawable.draw(it) }
                        } else {
                            drawableCanvas?.let {
                                val drawableSaveCount = it.saveCount
                                it.save()
                                it.concat(imageMatrix)
                                drawable.draw(it)
                                it.restoreToCount(drawableSaveCount)
                            }
                        }

                        drawablePaint?.let {
                            it.reset()
                            it.isFilterBitmap = false
                            it.xfermode = porterDuffXferMode
                        }
                        maskBitmap?.let { drawableCanvas?.drawBitmap(it, 0.0f, 0.0f, drawablePaint) }
                    }
                }
                if (!invalidated) {
                    drawablePaint?.xfermode = null
                    drawableBitmap?.let { canvas.drawBitmap(it, 0.0f, 0.0f, drawablePaint) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                saveCount.let { canvas.restoreToCount(it) }
            }
        }
    }

    protected abstract fun paintMaskCanvas(maskCanvas: Canvas?, maskPaint: Paint?, width: Int, height: Int)

    fun setSrcColor(color: Int) {
        paintColor = color
        setImageDrawable(ColorDrawable(color))
        drawablePaint?.let {
            it.color = color
            invalidate()
        }
    }

    fun setSrcDrawable(drawable: Drawable, color: Int) {
        paintColor = color
        setImageDrawable(drawable)
        drawablePaint?.let {
            it.color = color
            invalidate()
        }
    }
}