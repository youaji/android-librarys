@file:Suppress("unused")
package com.youaji.libs.widget.shine.interpolator

import kotlin.math.*

/**
 * @author youaji
 * @since 2023/4/7
 */
object EasingProvider {

    /**
     * @param ease            Easing type
     * @param elapsedTimeRate Elapsed time / Total time
     * @return easedValue
     */
    operator fun get(ease: Ease?, elapsedTimeRate: Float): Float {
        var timeRate = elapsedTimeRate
        return when (ease) {
            Ease.LINEAR -> timeRate
            Ease.QUAD_IN -> getPowIn(timeRate, 2.0)
            Ease.QUAD_OUT -> getPowOut(timeRate, 2.0)
            Ease.QUAD_IN_OUT -> getPowInOut(timeRate, 2.0)
            Ease.CUBIC_IN -> getPowIn(timeRate, 3.0)
            Ease.CUBIC_OUT -> getPowOut(timeRate, 3.0)
            Ease.CUBIC_IN_OUT -> getPowInOut(timeRate, 3.0)
            Ease.QUART_IN -> getPowIn(timeRate, 4.0)
            Ease.QUART_OUT -> getPowOut(timeRate, 4.0)
            Ease.QUART_IN_OUT -> getPowInOut(timeRate, 4.0)
            Ease.QUINT_IN -> getPowIn(timeRate, 5.0)
            Ease.QUINT_OUT -> getPowOut(timeRate, 5.0)
            Ease.QUINT_IN_OUT -> getPowInOut(timeRate, 5.0)
            Ease.SINE_IN -> (1f - cos(timeRate * Math.PI / 2f)).toFloat()
            Ease.SINE_OUT -> sin(timeRate * Math.PI / 2f).toFloat()
            Ease.SINE_IN_OUT -> (-0.5f * (cos(Math.PI * timeRate) - 1f)).toFloat()
            Ease.BACK_IN -> (timeRate * timeRate * ((1.7 + 1f) * timeRate - 1.7)).toFloat()
            Ease.BACK_OUT -> (--timeRate * timeRate * ((1.7 + 1f) * timeRate + 1.7) + 1f).toFloat()
            Ease.BACK_IN_OUT -> getBackInOut(timeRate, 1.7f)
            Ease.CIRC_IN -> -(sqrt((1f - timeRate * timeRate).toDouble()) - 1).toFloat()
            Ease.CIRC_OUT -> sqrt((1f - --timeRate * timeRate).toDouble()).toFloat()
            Ease.CIRC_IN_OUT ->
                if (2f.let { timeRate *= it; timeRate } < 1f) {
                    (-0.5f * (sqrt((1f - timeRate * timeRate).toDouble()) - 1f)).toFloat()
                } else (0.5f * (sqrt((1f - 2f.let { timeRate -= it; timeRate } * timeRate).toDouble()) + 1f)).toFloat()
            Ease.BOUNCE_IN -> getBounceIn(timeRate)
            Ease.BOUNCE_OUT -> getBounceOut(timeRate)
            Ease.BOUNCE_IN_OUT ->
                if (timeRate < 0.5f) {
                    getBounceIn(timeRate * 2f) * 0.5f
                } else getBounceOut(timeRate * 2f - 1f) * 0.5f + 0.5f
            Ease.ELASTIC_IN -> getElasticIn(timeRate, 1.0, 0.3)
            Ease.ELASTIC_OUT -> getElasticOut(timeRate, 1.0, 0.3)
            Ease.ELASTIC_IN_OUT -> getElasticInOut(timeRate, 1.0, 0.45)
            Ease.EASE_IN_EXPO ->
                2.0.pow((10 * (timeRate - 1)).toDouble()).toFloat()
            Ease.EASE_OUT_EXPO ->
                -2.0.pow((-10 * timeRate).toDouble()).toFloat() + 1
            Ease.EASE_IN_OUT_EXPO ->
                if (2.let { timeRate *= it; timeRate } < 1) {
                    2.0.pow((10 * (timeRate - 1)).toDouble()).toFloat() * 0.5f
                } else ((-2.0).pow((-10 * --timeRate).toDouble()) + 2f).toFloat() * 0.5f
            else -> timeRate
        }
    }

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param pow             pow The exponent to use (ex. 3 would return a cubic ease).
     * @return easedValue
     */
    private fun getPowIn(elapsedTimeRate: Float, pow: Double): Float =
        elapsedTimeRate.toDouble().pow(pow).toFloat()

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param pow             pow The exponent to use (ex. 3 would return a cubic ease).
     * @return easedValue
     */
    private fun getPowOut(elapsedTimeRate: Float, pow: Double): Float =
        (1f - (1 - elapsedTimeRate).toDouble().pow(pow)).toFloat()

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param pow             pow The exponent to use (ex. 3 would return a cubic ease).
     * @return easedValue
     */
    private fun getPowInOut(elapsedTimeRate: Float, pow: Double): Float {
        var timeRate = elapsedTimeRate
        return if (2.let { timeRate *= it; timeRate } < 1) {
            (0.5 * timeRate.toDouble().pow(pow)).toFloat()
        } else (1 - 0.5 * abs((2 - timeRate).toDouble().pow(pow))).toFloat()
    }

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param amount          amount The strength of the ease.
     * @return easedValue
     */
    private fun getBackInOut(elapsedTimeRate: Float, amount: Float): Float {
        var timeRate = elapsedTimeRate
        var amountTmp = amount
        amountTmp *= 1.525.toFloat()
        return if (2.let { timeRate *= it; timeRate } < 1) {
            (0.5 * (timeRate * timeRate * ((amountTmp + 1) * timeRate - amountTmp))).toFloat()
        } else (0.5 * (2.let { timeRate -= it; timeRate } * timeRate * ((amountTmp + 1) * timeRate + amountTmp) + 2)).toFloat()
    }

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @return easedValue
     */
    private fun getBounceIn(elapsedTimeRate: Float): Float =
        1f - getBounceOut(1f - elapsedTimeRate)

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @return easedValue
     */
    private fun getBounceOut(elapsedTimeRate: Float): Float {
        var timeRate = elapsedTimeRate
        return if (timeRate < 1 / 2.75) {
            (7.5625 * timeRate * timeRate).toFloat()
        } else if (timeRate < 2 / 2.75) {
            (7.5625f * (1.5f / 2.75f.let { timeRate -= it; timeRate }) * timeRate + 0.75).toFloat()
        } else if (timeRate < 2.5 / 2.75) {
            (7.5625f * (2.25f / 2.75f.let { timeRate -= it; timeRate }) * timeRate + 0.9375).toFloat()
        } else {
            (7.5625f * (2.625f / 2.75f.let { timeRate -= it; timeRate }) * timeRate + 0.984375).toFloat()
        }
    }

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param amplitude       Amplitude of easing
     * @param period          Animation of period
     * @return easedValue
     */
    private fun getElasticIn(elapsedTimeRate: Float, amplitude: Double, period: Double): Float {
        var timeRate = elapsedTimeRate
        if (timeRate == 0f || timeRate == 1f)
            return timeRate
        val pi2 = Math.PI * 2
        val s = period / pi2 * asin(1 / amplitude)
        return -(amplitude * 2.0.pow((10f * 1f.let { timeRate -= it; timeRate }).toDouble()) * sin((timeRate - s) * pi2 / period)).toFloat()
    }

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param amplitude       Amplitude of easing
     * @param period          Animation of period
     * @return easedValue
     */
    private fun getElasticOut(elapsedTimeRate: Float, amplitude: Double, period: Double): Float {
        if (elapsedTimeRate == 0f || elapsedTimeRate == 1f)
            return elapsedTimeRate
        val pi2 = Math.PI * 2
        val s = period / pi2 * asin(1 / amplitude)
        return (amplitude * 2.0.pow((-10 * elapsedTimeRate).toDouble()) * sin((elapsedTimeRate - s) * pi2 / period) + 1).toFloat()
    }

    /**
     * @param elapsedTimeRate Elapsed time / Total time
     * @param amplitude       Amplitude of easing
     * @param period          Animation of period
     * @return easedValue
     */
    private fun getElasticInOut(elapsedTimeRate: Float, amplitude: Double, period: Double): Float {
        var timeRate = elapsedTimeRate
        val pi2 = Math.PI * 2
        val s = period / pi2 * asin(1 / amplitude)
        return if (2.let { timeRate *= it; timeRate } < 1) {
            (-0.5f * (amplitude * 2.0.pow((10 * 1f.let { timeRate -= it; timeRate }).toDouble()) * sin((timeRate - s) * pi2 / period))).toFloat()
        } else (amplitude * 2.0.pow((-10 * 1.let { timeRate -= it; timeRate }).toDouble()) * sin((timeRate - s) * pi2 / period) * 0.5 + 1).toFloat()
    }
}