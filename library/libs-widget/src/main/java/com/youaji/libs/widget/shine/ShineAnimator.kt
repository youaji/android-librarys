@file:Suppress("unused")
package com.youaji.libs.widget.shine

import android.animation.ValueAnimator
import com.youaji.libs.widget.shine.interpolator.Ease
import com.youaji.libs.widget.shine.interpolator.EasingInterpolator

/**
 * @author youaji
 * @since 2023/4/7
 */
class ShineAnimator : ValueAnimator {

    var maxValue = 1.5f
    var animDuration: Long = 2000
//    var canvas: Canvas? = null

    constructor() {
        setFloatValues(1f, maxValue)
        duration = animDuration
        startDelay = 200
        interpolator = EasingInterpolator(Ease.CUBIC_OUT)
    }

    constructor(duration: Long, max_value: Float, delay: Long) {
        setFloatValues(1f, max_value)
        setDuration(duration)
        startDelay = delay
        interpolator = EasingInterpolator(Ease.LINEAR)
    }

    fun startAnim() {
        start()
    }

//    fun setCanvas(canvas: Canvas?) {
//        this.canvas = canvas
//    }
}