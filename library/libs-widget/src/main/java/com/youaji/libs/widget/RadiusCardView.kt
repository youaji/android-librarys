@file:Suppress("unused")
package com.youaji.libs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.cardview.widget.CardView


/**
 * @author youaji
 * @since 2023/6/26
 */
class RadiusCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(context, attrs, defStyleAttr) {

    private var tlRadius = 0f
    private var trRadius = 0f
    private var brRadius = 0f
    private var blRadius = 0f

    init {
        radius = 0f
        attrs?.let {
            val array = context.obtainStyledAttributes(attrs, R.styleable.RadiusCardView)
            tlRadius = array.getDimension(R.styleable.RadiusCardView_top_left_radius, 0f)
            trRadius = array.getDimension(R.styleable.RadiusCardView_top_right_radius, 0f)
            blRadius = array.getDimension(R.styleable.RadiusCardView_bottom_left_radius, 0f)
            brRadius = array.getDimension(R.styleable.RadiusCardView_bottom_right_radius, 0f)
            array.recycle()
        }
        background = ColorDrawable()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val path = Path()
        val rectF = getRectF()
        val radius = floatArrayOf(tlRadius, tlRadius, trRadius, trRadius, brRadius, brRadius, blRadius, blRadius)
        path.addRoundRect(rectF, radius, Path.Direction.CW)
        canvas.clipPath(path, Region.Op.INTERSECT)
        super.onDraw(canvas)
    }

    private fun getRectF(): RectF {
        val rect = Rect()
        getDrawingRect(rect)
        return RectF(rect)
    }
}