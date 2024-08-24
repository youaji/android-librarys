@file:Suppress("unused")
package com.youaji.libs.ui.dialog

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape

/**
 * @author youaji
 * @since 2022/11/23
 */
internal class CircleDrawable(
    backgroundColor: Int,
    leftTopRadius: Int,
    rightTopRadius: Int,
    rightBottomRadius: Int,
    leftBottomRadius: Int
) : ShapeDrawable() {

    init {
        paint.color = backgroundColor
        shape = getRoundRectShape(leftTopRadius, rightTopRadius, rightBottomRadius, leftBottomRadius)
    }

    private fun getRoundRectShape(leftTop: Int, rightTop: Int, rightBottom: Int, leftBottom: Int): RoundRectShape {
        val outerRadii = FloatArray(8)
        if (leftTop > 0) {
            outerRadii[0] = leftTop.toFloat()
            outerRadii[1] = leftTop.toFloat()
        }
        if (rightTop > 0) {
            outerRadii[2] = rightTop.toFloat()
            outerRadii[3] = rightTop.toFloat()
        }
        if (rightBottom > 0) {
            outerRadii[4] = rightBottom.toFloat()
            outerRadii[5] = rightBottom.toFloat()
        }
        if (leftBottom > 0) {
            outerRadii[6] = leftBottom.toFloat()
            outerRadii[7] = leftBottom.toFloat()
        }
        return RoundRectShape(outerRadii, null, null)
    }

}