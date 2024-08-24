package com.youaji.libs.debug.widget._float.anim

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import com.youaji.libs.debug.widget._float.FloatSideMode
import com.youaji.libs.debug.widget._float.interfaces.OnFloatAnimator
import com.youaji.libs.debug.widget._float.utils.DisplayUtils
import kotlin.math.min

/**
 * 系统浮窗的默认效果
 * 选择靠近左右侧的一边进行出入
 * @author youaji
 * @since 2024/01/05
 */
open class DefaultAnimator : OnFloatAnimator {
    override fun enterAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        floatSideMode: FloatSideMode
    ): Animator? = getAnimator(view, params, windowManager, floatSideMode, false)

    override fun exitAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        floatSideMode: FloatSideMode
    ): Animator? = getAnimator(view, params, windowManager, floatSideMode, true)

    private fun getAnimator(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        floatSideMode: FloatSideMode,
        isExit: Boolean
    ): Animator {
        val triple = initValue(view, params, windowManager, floatSideMode)
        // 退出动画的起始值、终点值，与入场动画相反
        val start = if (isExit) triple.second else triple.first
        val end = if (isExit) triple.first else triple.second
        return ValueAnimator.ofInt(start, end).apply {
            addUpdateListener {
                try {
                    val value = it.animatedValue as Int
                    if (triple.third) params.x = value else params.y = value
                    // 动画执行过程中页面关闭，出现异常
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    cancel()
                }
            }
        }
    }

    /**
     * 计算边距，起始坐标等
     */
    private fun initValue(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        floatSideMode: FloatSideMode
    ): Triple<Int, Int, Boolean> {
        val parentRect = Rect()
        windowManager.defaultDisplay.getRectSize(parentRect)
        // 浮窗各边到窗口边框的距离
        val leftDistance = params.x
        val rightDistance = parentRect.right - (leftDistance + view.right)
        val topDistance = params.y
        val bottomDistance = parentRect.bottom - (topDistance + view.bottom)
        // 水平、垂直方向的距离最小值
        val minX = min(leftDistance, rightDistance)
        val minY = min(topDistance, bottomDistance)

        val isHorizontal: Boolean
        val endValue: Int
        val startValue: Int = when (floatSideMode) {
            FloatSideMode.FixedLeft, FloatSideMode.Ending2Left -> {
                // 从左侧到目标位置，右移
                isHorizontal = true
                endValue = params.x
                -view.right
            }
            FloatSideMode.FixedRight, FloatSideMode.Ending2Right -> {
                // 从右侧到目标位置，左移
                isHorizontal = true
                endValue = params.x
                parentRect.right
            }
            FloatSideMode.FixedTop, FloatSideMode.Ending2Top -> {
                // 从顶部到目标位置，下移
                isHorizontal = false
                endValue = params.y
                -view.bottom
            }
            FloatSideMode.FixedBottom, FloatSideMode.Ending2Bottom -> {
                // 从底部到目标位置，上移
                isHorizontal = false
                endValue = params.y
                parentRect.bottom + getCompensationHeight(view, params)
            }

            FloatSideMode.Default, FloatSideMode.AutoHorizontal, FloatSideMode.Ending2Horizontal -> {
                // 水平位移，哪边距离屏幕近，从哪侧移动
                isHorizontal = true
                endValue = params.x
                if (leftDistance < rightDistance) -view.right else parentRect.right
            }
            FloatSideMode.AutoVertical, FloatSideMode.Ending2Vertical -> {
                // 垂直位移，哪边距离屏幕近，从哪侧移动
                isHorizontal = false
                endValue = params.y
                if (topDistance < bottomDistance) -view.bottom
                else parentRect.bottom + getCompensationHeight(view, params)
            }

            else -> if (minX <= minY) {
                isHorizontal = true
                endValue = params.x
                if (leftDistance < rightDistance) -view.right else parentRect.right
            } else {
                isHorizontal = false
                endValue = params.y
                if (topDistance < bottomDistance) -view.bottom
                else parentRect.bottom + getCompensationHeight(view, params)
            }
        }
        return Triple(startValue, endValue, isHorizontal)
    }

    /**
     * 单页面浮窗（popupWindow），坐标从顶部计算，需要加上状态栏的高度
     */
    private fun getCompensationHeight(view: View, params: WindowManager.LayoutParams): Int {
        val location = IntArray(2)
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location)
        // 绝对高度和相对高度相等，说明是单页面浮窗（popupWindow），计算底部动画时需要加上状态栏高度
        return if (location[1] == params.y) DisplayUtils.statusBarHeight(view) else 0
    }

}