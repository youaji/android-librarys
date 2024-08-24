@file:Suppress("unused")
package com.youaji.libs.widget.shine

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import com.youaji.libs.widget.shine.interpolator.Ease
import com.youaji.libs.widget.shine.interpolator.EasingInterpolator
import kotlin.math.sqrt

/**
 * @author youaji
 * @since 2023/4/7
 */
class ShinePlusView : View {

    private val frameRefreshDelay: Long = 10 // default 10ms ,change to 25ms for saving cpu.

    private var shineAnimator: ShineAnimator? = null
    private var clickAnimator: ValueAnimator? = null

    private var shineButton: ShineButton? = null
    private var paint: Paint? = null

    companion object {
        private var colorCount = 10
        private var colorRandom = IntArray(colorCount)
    }

    // Customer property
    private var smallOffsetAngle = 0f
    private var turnAngle = 0f
    private var animDuration: Long = 0
    private var clickAnimDuration: Long = 0
    private var shineDistanceMultiple = 0f
    private var smallShineColor = colorRandom[0]
    private var bigShineColor = colorRandom[1]
    private var sbBorderColor = Color.parseColor("#333333")

    private var shineSize = 0

    private var allowRandomColor = false
    private var enableFlashing = false

    private var rectF = RectF()
    private var centerAnimX = 0
    private var centerAnimY = 0
    private var btnWidth = 0
    private var btnHeight = 0

    private var thirdLength = 0.0
    private var value = 0f
    private var clickValue = 0f
    private var isRun = false
    private val distanceOffset = 0.2f

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, shineButton: ShineButton, shineParams: ShineParams) : super(context) {
        initShineParams(shineParams, shineButton)
        shineAnimator = ShineAnimator(animDuration, shineDistanceMultiple, clickAnimDuration)
        ValueAnimator.setFrameDelay(frameRefreshDelay)
        this.shineButton = shineButton

        paint = Paint()
        paint?.color = Color.RED
        paint?.strokeWidth = 30f
        paint?.textSize = 36f
        paint?.style = Paint.Style.FILL
        paint?.textAlign = Paint.Align.CENTER

        clickAnimator = ValueAnimator.ofFloat(0f, 1.1f)
        ValueAnimator.setFrameDelay(frameRefreshDelay)
        clickAnimator?.duration = clickAnimDuration
        clickAnimator?.interpolator = EasingInterpolator(Ease.QUART_OUT)
        clickAnimator?.addUpdateListener {
            clickValue = it.animatedValue as Float
            invalidate()
        }
        clickAnimator?.doOnEnd {
            clickValue = 0f
            invalidate()
        }
        shineAnimator?.doOnEnd { shineButton.removeView(this@ShinePlusView) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (value > 1.35) {
            paint?.alpha = (255 * (1 - (value - 1.35) / 0.15)).toInt()
        }
        if (value > 1) {
            paint?.let { canvas.drawText("+1", centerAnimX.toFloat(), centerAnimY - (value - 1) * 180, it) }
        }
        paint?.strokeWidth = btnWidth * clickValue * (shineDistanceMultiple - distanceOffset)
        if (shineAnimator != null && !isRun) {
            isRun = true
            val handler = Handler(Looper.getMainLooper())
            shineButton?.let { handler.postDelayed({ showAnimation(it) }, 100) }
        }
    }

    private fun initShineParams(shineParams: ShineParams, shineButton: ShineButton) {
        turnAngle = shineParams.shineTurnAngle
        smallOffsetAngle = shineParams.smallShineOffsetAngle
        enableFlashing = shineParams.enableFlashing
        allowRandomColor = shineParams.allowRandomColor
        shineDistanceMultiple = shineParams.shineDistanceMultiple
        animDuration = shineParams.animDuration
        clickAnimDuration = shineParams.clickAnimDuration
        smallShineColor = shineParams.smallShineColor
        bigShineColor = shineParams.bigShineColor
        sbBorderColor = shineParams.sbBorderColor
        shineSize = shineParams.shineSize
        if (smallShineColor == 0) {
            smallShineColor = colorRandom[6]
        }
        if (bigShineColor == 0) {
            bigShineColor = shineButton.getCheckedColor()
        }
    }

    private fun showAnimation(shineButton: ShineButton) {
        btnWidth = shineButton.width
        btnHeight = shineButton.height
        thirdLength = getThirdLength(btnHeight, btnWidth)

        val outLocation = IntArray(2)
        shineButton.getLocationInWindow(outLocation)
        centerAnimX = outLocation[0] + shineButton.width / 2
        centerAnimY = outLocation[1] + shineButton.height / 2

        if (shineButton.mFixDialog != null && shineButton.mFixDialog?.window != null) {
            val decor = shineButton.mFixDialog?.window?.decorView
            centerAnimX -= decor?.paddingLeft ?: 0
            centerAnimY -= decor?.paddingTop ?: 0
        }

        shineAnimator?.addUpdateListener { valueAnimator ->
            value = valueAnimator.animatedValue as Float
            if (shineSize != 0 && shineSize > 0) {
                paint?.strokeWidth = shineSize * (shineDistanceMultiple - value)
            } else {
                paint?.strokeWidth = btnWidth / 2 * (shineDistanceMultiple - value)
            }
            rectF[
                    centerAnimX - btnWidth / (3 - shineDistanceMultiple) * value,
                    centerAnimY - btnHeight / (3 - shineDistanceMultiple) * value,
                    centerAnimX + btnWidth / (3 - shineDistanceMultiple) * value
            ] = centerAnimY + btnHeight / (3 - shineDistanceMultiple) * value
            invalidate()
        }
        shineAnimator?.startAnim()
        clickAnimator?.start()
    }

    private fun getThirdLength(btnHeight: Int, btnWidth: Int): Double {
        val all = btnHeight * btnHeight + btnWidth * btnWidth
        return sqrt(all.toDouble())
    }

    class ShineParams internal constructor() {
        var allowRandomColor = false
        var animDuration: Long = 1000
        var bigShineColor = 0
        var clickAnimDuration: Long = 200
        var enableFlashing = false
        var shineCount = 7
        var shineTurnAngle = 20f
        var shineDistanceMultiple = 1.5f
        var smallShineOffsetAngle = 20f
        var smallShineColor = 0
        var shineSize = 0
        var sbBorderColor = 0

        init {
            colorRandom[0] = Color.parseColor("#FFFF99")
            colorRandom[1] = Color.parseColor("#FFCCCC")
            colorRandom[2] = Color.parseColor("#996699")
            colorRandom[3] = Color.parseColor("#FF6666")
            colorRandom[4] = Color.parseColor("#FFFF66")
            colorRandom[5] = Color.parseColor("#F44336")
            colorRandom[6] = Color.parseColor("#666666")
            colorRandom[7] = Color.parseColor("#CCCC00")
            colorRandom[8] = Color.parseColor("#666666")
            colorRandom[9] = Color.parseColor("#999933")
        }
    }
}