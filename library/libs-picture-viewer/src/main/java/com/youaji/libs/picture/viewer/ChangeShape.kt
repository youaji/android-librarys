package com.youaji.libs.picture.viewer

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Outline
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.transition.Transition
import androidx.transition.TransitionValues
import kotlin.math.max

/**
 * 图形变换，目前只有圆角变换
 */
class ChangeShape(private val startRadius: Float, private val endRadius: Float) : Transition() {

    companion object {
        private const val PROPNAME_RADIUS = "android:ChangeShape:radius"
        private val sTransitionProperties = arrayOf(
            PROPNAME_RADIUS
        )
    }

    /**
     * A constructor that takes an identifying name and [type][.getType] for the property.
     *
     * @param startValue 属性改变的起始值
     * @param endValue   属性改变的结束值
     */
    private class Property(
        private val startValue: Float,
        private val endValue: Float
    ) : android.util.Property<View, Float>(Float::class.java, "radius") {

        private var mProvider: ViewOutlineProvider? = null
        private var offset: Float

        init {
            val maxValue = max(startValue.toDouble(), endValue.toDouble()).toFloat()
            offset = 0.01f
            if (maxValue in 20.0..30.0) {
                offset += 0.005f + (30 - maxValue) * 0.001f
            } else {
                offset = 0.2f
            }
        }

        @SuppressLint("ObsoleteSdkInt")
        override fun set(view: View, value: Float?) {
            if (value == null
                || startValue <= endValue
                && value < endValue * offset
                || startValue > endValue
                && value < startValue * offset
            ) { // 打开预览，此时圆角小于 startValue * offset 不做处理
                //如果不做此判断，那么动画结束时会闪屏,目前不清楚为什么出现该情况
                return
            }
            if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                if (mProvider == null) {
                    mProvider = ViewOutlineProvider()
                }
                mProvider?.setRadius(value)
                view.setOutlineProvider(mProvider)
            } else if (view is CardView) {
                view.radius = value
            }
        }

        override fun get(obj: View): Float? = null
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    internal class ViewOutlineProvider : android.view.ViewOutlineProvider() {
        private var radius = 0f
        fun setRadius(radius: Float) {
            this.radius = radius
        }

        override fun getOutline(view: View, outline: Outline) {
            // 采用此种裁剪方式，需要内容填满 View，比如 ImageView 控件为正方形，设置缩放模式非完全填充，比如 fit_center,
            // 那么图片不是正方形时，此时图片无法完全占满 ImageView 控件，而此时裁剪是对 ImageView 进行裁剪，最终裁剪效果与
            // 先裁剪图片再设置图片将不一致，比如 Glide 框架.后续待完善
            val left = view.left
            val top = view.top
            val width = view.width
            val height = view.height
            outline.setRoundRect(left, top, left + width, top + height, radius)
        }
    }

    override fun getTransitionProperties(): Array<String> =
        sTransitionProperties

    override fun captureStartValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_RADIUS] = startRadius
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_RADIUS] = endRadius
    }

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }
        val startRadius = startValues.values[PROPNAME_RADIUS] as Float?
        val endRadius = endValues.values[PROPNAME_RADIUS] as Float?
        return if (startRadius == null || endRadius == null || startRadius == endRadius) null
        else ofFloat(endValues.view, startRadius, endRadius)
    }

    @SuppressLint("ObsoleteSdkInt", "ObjectAnimatorBinding")
    private fun ofFloat(target: View, startValue: Float, endValue: Float): ObjectAnimator? {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            target.setClipToOutline(true)
            return ObjectAnimator.ofFloat(target, Property(startValue, endValue), startValue, endValue)
        }
        return if (target is CardView) ObjectAnimator.ofFloat(target, "radius", startValue, endValue)
        else null
    }


}
