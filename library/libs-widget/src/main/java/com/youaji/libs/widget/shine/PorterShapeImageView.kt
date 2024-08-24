@file:Suppress("unused")
package com.youaji.libs.widget.shine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.youaji.libs.widget.R

/**
 * @author youaji
 * @since 2023/4/7
 */
open class PorterShapeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : PorterImageView(context, attrs, defStyleAttr) {

    private var shape: Drawable? = null
    private var matrix: Matrix? = null
    private var drawMatrix: Matrix? = null

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PorterShapeImageView, defStyleAttr, 0)
            shape = typedArray.getDrawable(R.styleable.PorterShapeImageView_siShape)
            typedArray.recycle()
        }
        matrix = Matrix()
    }

    override fun paintMaskCanvas(maskCanvas: Canvas?, maskPaint: Paint?, width: Int, height: Int) {
        shape?.let { shape ->
            if (shape is BitmapDrawable) {
                configureBitmapBounds(getWidth(), getHeight())
                drawMatrix?.let {
                    maskCanvas?.let {
                        val drawableSaveCount = maskCanvas.saveCount
                        maskCanvas.save()
                        maskCanvas.concat(matrix)
                        shape.draw(maskCanvas)
                        maskCanvas.restoreToCount(drawableSaveCount)
                    }
                    return
                }
            }
            shape.setBounds(0, 0, getWidth(), getHeight())
            maskCanvas?.let { shape.draw(maskCanvas) }
        }
    }

    protected fun setShape(drawable: Drawable) {
        this.shape = drawable
        invalidate()
    }

    private fun configureBitmapBounds(viewWidth: Int, viewHeight: Int) {
        drawMatrix = null
        val drawableWidth = shape?.intrinsicWidth ?: 0
        val drawableHeight = shape?.intrinsicHeight ?: 0
        val fits = viewWidth == drawableWidth && viewHeight == drawableHeight

        if (drawableWidth > 0 && drawableHeight > 0 && !fits) {
            shape?.setBounds(0, 0, drawableWidth, drawableHeight)
            val widthRatio = viewWidth.toFloat() / drawableWidth.toFloat()
            val heightRatio = viewHeight.toFloat() / drawableHeight.toFloat()
            val scale = widthRatio.coerceAtMost(heightRatio)
            val dx = ((viewWidth - drawableWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
            val dy = ((viewHeight - drawableHeight * scale) * 0.5f + 0.5f).toInt().toFloat()
            matrix?.setScale(scale, scale)
            matrix?.postTranslate(dx, dy)
        }
    }

}