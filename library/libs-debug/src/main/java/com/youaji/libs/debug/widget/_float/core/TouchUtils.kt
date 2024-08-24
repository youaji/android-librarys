package com.youaji.libs.debug.widget._float.core

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import com.youaji.libs.debug.widget._float.FloatConfig
import com.youaji.libs.debug.widget._float.FloatDisplayType
import com.youaji.libs.debug.widget._float.FloatSideMode
import com.youaji.libs.debug.widget._float.utils.DisplayUtils
import kotlin.math.max
import kotlin.math.min

/**
 * 根据吸附模式，实现相应的拖拽效果
 * @author youaji
 * @since 2024/01/05
 */
internal class TouchUtils(
    val context: Context,
    val config: FloatConfig,
) {

    // 窗口所在的矩形
    private var parentRect: Rect = Rect()

    // 悬浮的父布局高度、宽度
    private var parentSize = Size(0, 0)

    // 四周坐标边界值
    private var border = Rect(0, 0, 0, 0)

    // 浮窗各边距离父布局的距离
    private var distance = Rect(0, 0, 0, 0)

    // 起点坐标
    private var lastPoint = PointF(0f, 0f)

    // x轴、y轴的最小距离值
    private var minX = 0
    private var minY = 0
    private val location = IntArray(2)
    private var statusBarHeight = 0

    // 屏幕可用高度 - 浮窗自身高度 的剩余高度
    private var emptyHeight = 0

    /**
     * 根据吸附模式，实现相应的拖拽效果
     */
    fun updateFloat(
        view: View,
        event: MotionEvent,
        windowManager: WindowManager,
        params: LayoutParams
    ) {
        config.callback?.touchEvent(view, event)
        config.floatCallback?.builder?.touchEvent?.invoke(view, event)
        // 不可拖拽、或者正在执行动画，不做处理
        if (!config.dragEnable || config.isAnimating) {
            config.isDragging = false
            return
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                config.isDragging = false
                // 记录触摸点的位置
                lastPoint.x = event.rawX
                lastPoint.y = event.rawY
                // 初始化一些边界数据
                initBoarderValue(view, params)
            }

            MotionEvent.ACTION_MOVE -> {
                // 过滤边界值之外的拖拽
                if (event.rawX < border.left || event.rawX > border.right + view.width
                    || event.rawY < border.top || event.rawY > border.bottom + view.height
                ) return

                // 移动值 = 本次触摸值 - 上次触摸值
                val dx = event.rawX - lastPoint.x
                val dy = event.rawY - lastPoint.y
                // 忽略过小的移动，防止点击无效
                if (!config.isDragging && dx * dx + dy * dy < 81) return
                config.isDragging = true

                var x = params.x + dx.toInt()
                var y = params.y + dy.toInt()
                // 检测浮窗是否到达边缘
                x = when {
                    x < border.left -> border.left
                    x > border.right -> border.right
                    else -> x
                }

                if (config.floatDisplayType == FloatDisplayType.OnlyCurrent) {
                    // 单页面浮窗，设置状态栏不沉浸时，最小高度为状态栏高度
                    if (y < statusBarHeight(view) && !config.immersionStatusBar) y =
                        statusBarHeight(view)
                }

                y = when {
                    y < border.top -> border.top
                    // 状态栏沉浸时，最小高度为-statusBarHeight，反之最小高度为0
                    y < 0 -> if (config.immersionStatusBar) {
                        if (y < -statusBarHeight) -statusBarHeight else y
                    } else 0

                    y > border.bottom -> border.bottom
                    else -> y
                }

                when (config.floatSideMode) {
                    FloatSideMode.FixedLeft -> x = 0
                    FloatSideMode.FixedRight -> x = parentSize.width - view.width
                    FloatSideMode.FixedTop -> y = 0
                    FloatSideMode.FixedBottom -> y = emptyHeight

                    FloatSideMode.AutoHorizontal ->
                        x = if (event.rawX * 2 > parentSize.width) parentSize.width - view.width else 0

                    FloatSideMode.AutoVertical ->
                        y = if ((event.rawY - parentRect.top) * 2 > parentSize.height)
                            parentSize.height - view.height else 0

                    FloatSideMode.AutoSide -> {
                        distance = Rect(
                            event.rawX.toInt(),
                            event.rawY.toInt() - parentRect.top,
                            parentSize.width - event.rawX.toInt(),
                            parentSize.height + parentRect.top - event.rawY.toInt(),
                        )

                        minX = min(distance.left, distance.right)
                        minY = min(distance.top, distance.bottom)
                        if (minX < minY) {
                            x = if (distance.left == minX) 0 else parentSize.width - view.width
                        } else {
                            y = if (distance.top == minY) 0 else emptyHeight
                        }
                    }

                    else -> {
                    }
                }

                // 重新设置坐标信息
                params.x = x
                params.y = y
                windowManager.updateViewLayout(view, params)
                config.callback?.drag(view, event)
                config.floatCallback?.builder?.drag?.invoke(view, event)
                // 更新上次触摸点的数据
                lastPoint.x = event.rawX
                lastPoint.y = event.rawY
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!config.isDragging) return
                // 回调拖拽事件的ACTION_UP
                config.callback?.drag(view, event)
                config.floatCallback?.builder?.drag?.invoke(view, event)
                when (config.floatSideMode) {
                    FloatSideMode.Ending2Left,
                    FloatSideMode.Ending2Right,
                    FloatSideMode.Ending2Top,
                    FloatSideMode.Ending2Bottom,
                    FloatSideMode.Ending2Horizontal,
                    FloatSideMode.Ending2Vertical,
                    FloatSideMode.Ending2Side -> sideAnim(view, params, windowManager)

                    else -> {
                        config.callback?.dragEnd(view)
                        config.floatCallback?.builder?.dragEnd?.invoke(view)
                    }
                }
            }

            else -> return
        }
    }

    /**
     * 根据吸附类别，更新浮窗位置
     */
    fun updateFloat(
        view: View,
        params: LayoutParams,
        windowManager: WindowManager
    ) {
        initBoarderValue(view, params)
        sideAnim(view, params, windowManager)
    }

    /**
     * 初始化边界值等数据
     */
    private fun initBoarderValue(view: View, params: LayoutParams) {
        // 屏幕宽高需要每次获取，可能会有屏幕旋转、虚拟导航栏的状态变化
        parentSize = Size(
            DisplayUtils.getScreenWidth(context),
            config.displayHeight.getDisplayRealHeight(context)
        )
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location)
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        statusBarHeight = if (location[1] > params.y) statusBarHeight(view) else 0
        emptyHeight = parentSize.height - view.height - statusBarHeight

        border.left = max(0, config.border.left)
        border.right = min(parentSize.width, config.border.right) - view.width
        border.top = if (config.floatDisplayType == FloatDisplayType.OnlyCurrent) {
            // 单页面浮窗，坐标屏幕顶部计算
            if (config.immersionStatusBar) config.border.top
            else config.border.top + statusBarHeight(view)
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            if (config.immersionStatusBar) config.border.top - statusBarHeight(view) else config.border.top
        }
        border.bottom = if (config.floatDisplayType == FloatDisplayType.OnlyCurrent) {
            // 单页面浮窗，坐标屏幕顶部计算
            if (config.immersionStatusBar)
                min(emptyHeight, config.border.bottom - view.height)
            else
                min(emptyHeight, config.border.bottom + statusBarHeight(view) - view.height)
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            if (config.immersionStatusBar)
                min(emptyHeight, config.border.bottom - statusBarHeight(view) - view.height)
            else
                min(emptyHeight, config.border.bottom - view.height)
        }
    }

    private fun sideAnim(
        view: View,
        params: LayoutParams,
        windowManager: WindowManager
    ) {
        initDistanceValue(params)
        val isX: Boolean
        val end = when (config.floatSideMode) {
            FloatSideMode.Ending2Left -> {
                isX = true
                border.left
            }

            FloatSideMode.Ending2Right -> {
                isX = true
                params.x + distance.right
            }

            FloatSideMode.Ending2Horizontal -> {
                isX = true
                if (distance.left < distance.right) border.left else params.x + distance.right
            }

            FloatSideMode.Ending2Top -> {
                isX = false
                border.top
            }

            FloatSideMode.Ending2Bottom -> {
                isX = false
                // 不要轻易使用此相关模式，需要考虑虚拟导航栏的情况
                border.bottom
            }

            FloatSideMode.Ending2Vertical -> {
                isX = false
                if (distance.top < distance.bottom) border.top else border.bottom
            }

            FloatSideMode.Ending2Side -> {
                if (minX < minY) {
                    isX = true
                    if (distance.left < distance.right) border.left
                    else params.x + distance.right
                } else {
                    isX = false
                    if (distance.top < distance.bottom) border.top
                    else border.bottom
                }
            }

            else -> return
        }

        val animator = ValueAnimator.ofInt(if (isX) params.x else params.y, end)
        animator.addUpdateListener {
            try {
                if (isX) params.x = it.animatedValue as Int else params.y = it.animatedValue as Int
                // 极端情况，还没吸附就调用了关闭浮窗，会导致吸附闪退
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                animator.cancel()
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                dragEnd(view)
            }

            override fun onAnimationCancel(animation: Animator) {
                dragEnd(view)
            }

            override fun onAnimationStart(animation: Animator) {
                config.isAnimating = true
            }
        })
        animator.start()
    }

    private fun dragEnd(view: View) {
        config.isAnimating = false
        config.callback?.dragEnd(view)
        config.floatCallback?.builder?.dragEnd?.invoke(view)
    }

    /**
     * 计算一些边界距离数据
     */
    private fun initDistanceValue(params: LayoutParams) {
        distance = Rect(
            params.x - border.left,
            params.y - border.top,
            border.right - params.x,
            border.bottom - params.y,
        )

        minX = min(distance.left, distance.right)
        minY = min(distance.top, distance.bottom)
    }

    private fun statusBarHeight(view: View) = DisplayUtils.statusBarHeight(view)

}