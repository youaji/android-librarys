@file:Suppress("unused")
package com.youaji.libs.widget.shine

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import com.youaji.libs.widget.shine.interpolator.Ease
import com.youaji.libs.widget.shine.interpolator.EasingInterpolator
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * @author youaji
 * @since 2023/4/7
 */
class ShineView : View {

    private val frameRefreshDelay: Long = 25 // default 10ms ,change to 25ms for saving cpu.

    private var shineAnimator: ShineAnimator? = null
    private var clickAnimator: ValueAnimator? = null

    private var shineButton: ShineButton? = null
    private var paint: Paint? = null
    private var paint2: Paint? = null
    private var paintSmall: Paint? = null

    private var colorCount = 10
    private var colorRandom = IntArray(10)

    // Customer property
    private var shineCount = 0
    private var smallOffsetAngle = 0f
    private var turnAngle = 0f
    private var animDuration: Long = 0
    private var clickAnimDuration: Long = 0
    private var shineDistanceMultiple = 0f
    private var smallShineColor = colorRandom[0]
    private var bigShineColor = colorRandom[1]

    private var shineSize = 0

    private var allowRandomColor = false
    private var enableFlashing = false

    private var rectF = RectF()
    private var rectFSmall = RectF()

    private var random = Random()
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
        paint?.color = bigShineColor
        paint?.strokeWidth = 20f
        paint?.style = Paint.Style.STROKE
        paint?.strokeCap = Paint.Cap.ROUND

        paint2 = Paint()
        paint2?.color = Color.WHITE
        paint2?.strokeWidth = 20f
        paint2?.strokeCap = Paint.Cap.ROUND

        paintSmall = Paint()
        paintSmall?.color = smallShineColor
        paintSmall?.strokeWidth = 10f
        paintSmall?.style = Paint.Style.STROKE
        paintSmall?.strokeCap = Paint.Cap.ROUND

        clickAnimator = ValueAnimator.ofFloat(0f, 1.1f)
        ValueAnimator.setFrameDelay(frameRefreshDelay)
        clickAnimator?.duration = clickAnimDuration
        clickAnimator?.interpolator = EasingInterpolator(Ease.QUART_OUT)
        clickAnimator?.addUpdateListener { valueAnimator ->
            clickValue = valueAnimator.animatedValue as Float
            invalidate()
        }
        clickAnimator?.doOnEnd {
            clickValue = 0f
            invalidate()
        }
        shineAnimator?.doOnEnd {
            shineButton.removeView(this@ShineView)
        }
    }

    fun showAnimation(shineButton: ShineButton) {
        btnWidth = shineButton.width
        btnHeight = shineButton.height
        thirdLength = getThirdLength(btnHeight, btnWidth)
        val location = IntArray(2)
        shineButton.getLocationInWindow(location)
        centerAnimX = location[0] + shineButton.width / 2
        centerAnimY = location[1] + shineButton.height / 2

        if (shineButton.mFixDialog != null && shineButton.mFixDialog?.window != null) {
            val decor = shineButton.mFixDialog?.window?.decorView
            centerAnimX -= decor?.paddingLeft ?: 0
            centerAnimY -= decor?.paddingTop ?: 0
        }

        shineAnimator?.addUpdateListener { valueAnimator ->
            value = valueAnimator.animatedValue as Float

            if (shineSize != 0 && shineSize > 0) {
                paint?.strokeWidth = shineSize * (shineDistanceMultiple - value)
                paintSmall?.strokeWidth = shineSize.toFloat() / 3 * 2 * (shineDistanceMultiple - value)
            } else {
                paint?.strokeWidth = btnWidth / 2 * (shineDistanceMultiple - value)
                paintSmall?.strokeWidth = btnWidth / 3 * (shineDistanceMultiple - value)
            }

            rectF[
                    centerAnimX - btnWidth / (3 - shineDistanceMultiple) * value,
                    centerAnimY - btnHeight / (3 - shineDistanceMultiple) * value,
                    centerAnimX + btnWidth / (3 - shineDistanceMultiple) * value
            ] = centerAnimY + btnHeight / (3 - shineDistanceMultiple) * value

            rectFSmall[
                    centerAnimX - btnWidth / (3 - shineDistanceMultiple + distanceOffset) * value,
                    centerAnimY - btnHeight / (3 - shineDistanceMultiple + distanceOffset) * value,
                    centerAnimX + btnWidth / (3 - shineDistanceMultiple + distanceOffset) * value
            ] = centerAnimY + btnHeight / (3 - shineDistanceMultiple + distanceOffset) * value

            invalidate()
        }
        shineAnimator?.startAnim()
        clickAnimator?.start()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until shineCount) {
            if (allowRandomColor) {
                paint?.color = colorRandom[if (abs(colorCount / 2 - i) >= colorCount) colorCount - 1 else abs(colorCount / 2 - i)]
            }
            paint?.let {
                canvas.drawArc(
                    rectF,
                    360f / shineCount * i + 1 + (value - 1) * turnAngle,
                    0.1f,
                    false,
                    getConfigPaint(it)
                )
            }
        }
        for (i in 0 until shineCount) {
            if (allowRandomColor) {
                paint?.color = colorRandom[if (abs(colorCount / 2 - i) >= colorCount) colorCount - 1 else abs(colorCount / 2 - i)]
            }
            paintSmall?.let {
                canvas.drawArc(
                    rectFSmall,
                    360f / shineCount * i + 1 - smallOffsetAngle + (value - 1) * turnAngle,
                    0.1f,
                    false,
                    getConfigPaint(it)
                )
            }
        }

        paint?.strokeWidth = btnWidth * clickValue * (shineDistanceMultiple - distanceOffset)

        if (clickValue != 0f) {
            paint2?.strokeWidth = btnWidth * clickValue * (shineDistanceMultiple - distanceOffset) - 8
        } else {
            paint2?.strokeWidth = 0f
        }

        paint?.let { canvas.drawPoint(centerAnimX.toFloat(), centerAnimY.toFloat(), it) }
        paint2?.let { canvas.drawPoint(centerAnimX.toFloat(), centerAnimY.toFloat(), it) }

        if (shineAnimator != null && !isRun) {
            isRun = true
            shineButton?.let { showAnimation(it) }
        }
    }

    private fun getConfigPaint(paint: Paint): Paint {
        if (enableFlashing) {
            paint.color = colorRandom[random.nextInt(colorCount - 1)]
        }
        return paint
    }

    private fun getThirdLength(btnHeight: Int, btnWidth: Int): Double {
        val all = btnHeight * btnHeight + btnWidth * btnWidth
        return sqrt(all.toDouble())
    }

    inner class ShineParams internal constructor() {
        var allowRandomColor = false
        var animDuration: Long = 1500
        var bigShineColor = 0
        var clickAnimDuration: Long = 200
        var enableFlashing = false
        var shineCount = 7
        var shineTurnAngle = 20f
        var shineDistanceMultiple = 1.5f
        var smallShineOffsetAngle = 20f
        var smallShineColor = 0
        var shineSize = 0

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

    private fun initShineParams(shineParams: ShineParams, shineButton: ShineButton) {
        shineCount = shineParams.shineCount
        turnAngle = shineParams.shineTurnAngle
        smallOffsetAngle = shineParams.smallShineOffsetAngle
        enableFlashing = shineParams.enableFlashing
        allowRandomColor = shineParams.allowRandomColor
        shineDistanceMultiple = shineParams.shineDistanceMultiple
        animDuration = shineParams.animDuration
        clickAnimDuration = shineParams.clickAnimDuration
        smallShineColor = shineParams.smallShineColor
        bigShineColor = shineParams.bigShineColor
        shineSize = shineParams.shineSize
        if (smallShineColor == 0) {
            smallShineColor = colorRandom[6]
        }
        if (bigShineColor == 0) {
            bigShineColor = shineButton.getCheckedColor()
        }
    }

}