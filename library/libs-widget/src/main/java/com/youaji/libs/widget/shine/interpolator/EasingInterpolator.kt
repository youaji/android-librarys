@file:Suppress("unused")
package com.youaji.libs.widget.shine.interpolator

import android.view.animation.Interpolator


/**
 * @author youaji
 * @since 2023/4/7
 */
class EasingInterpolator(var ease: Ease) : Interpolator {

    override fun getInterpolation(input: Float): Float {
        return EasingProvider[ease, input]
    }
}