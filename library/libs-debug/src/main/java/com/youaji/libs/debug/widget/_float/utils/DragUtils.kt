package com.youaji.libs.debug.widget._float.utils

import android.view.*
import com.youaji.libs.debug.R
import com.youaji.libs.debug.widget._float.FloatWindow
import com.youaji.libs.debug.widget._float.anim.DefaultAnimator
import com.youaji.libs.debug.widget._float.FloatDisplayType
import com.youaji.libs.debug.widget._float.FloatSideMode
import com.youaji.libs.debug.widget._float.interfaces.OnFloatAnimator
import com.youaji.libs.debug.widget._float.interfaces.OnTouchRangeListener
import com.youaji.libs.debug.widget._float.widget.BaseSwitchView

/**
 * 拖拽打开、关闭浮窗
 * @author youaji
 * @since 2024/01/05
 */
object DragUtils {

    private const val ADD_TAG = "ADD_TAG"
    private const val CLOSE_TAG = "CLOSE_TAG"
    private var addView: BaseSwitchView? = null
    private var closeView: BaseSwitchView? = null
    private var downX = 0f
    private var screenWidth = 0
    private var offset = 0f

    /**
     * 注册侧滑创建浮窗
     * @param event Activity 的触摸事件
     * @param listener 右下角区域触摸事件回调
     * @param layoutId 右下角区域的布局文件
     * @param slideOffset 当前屏幕侧滑进度
     * @param start 动画开始阈值
     * @param end 动画结束阈值
     */
    @JvmOverloads
    fun registerSwipeAdd(
        event: MotionEvent?,
        listener: OnTouchRangeListener? = null,
        layoutId: Int = R.layout.libs_debug_layout_float_default_swipe_add,
        slideOffset: Float = -1f,
        start: Float = 0.1f,
        end: Float = 0.5f
    ) {
        if (event == null) return

        // 设置了侧滑监听，使用侧滑数据
        if (slideOffset != -1f) {
            // 如果滑动偏移，超过了动画起始位置，开始显示浮窗，并执行偏移动画
            if (slideOffset >= start) {
                val progress = minOf((slideOffset - start) / (end - start), 1f)
                setAddView(event, progress, listener, layoutId)
            } else dismissAdd()
        } else {
            // 未提供侧滑监听，根据手指坐标信息，判断浮窗信息
            LifecycleUtils.application?.let { screenWidth = DisplayUtils.getScreenWidth(it) }
            offset = event.rawX / screenWidth
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.rawX
                MotionEvent.ACTION_MOVE -> {
                    // 起始值小于最小边界值，并且当前偏离量大于最小边界
                    if (downX < start * screenWidth && offset >= start) {
                        val progress = minOf((offset - start) / (end - start), 1f)
                        setAddView(event, progress, listener, layoutId)
                    } else dismissAdd()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    downX = 0f
                    setAddView(event, offset, listener, layoutId)
                }
            }
        }
    }

    private fun setAddView(
        event: MotionEvent,
        progress: Float,
        listener: OnTouchRangeListener? = null,
        layoutId: Int
    ) {
        // 设置触摸状态监听
        addView?.let {
            it.setTouchRangeListener(event, listener)
            it.translationX = it.width * (1 - progress)
            it.translationY = it.width * (1 - progress)
        }
        // 手指抬起或者事件取消，关闭添加浮窗
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) dismissAdd()
        else showAdd(layoutId)
    }

    private fun showAdd(layoutId: Int) {
        if (FloatWindow.isShow(ADD_TAG)) return
        LifecycleUtils.application?.let {
            FloatWindow.with(it)
                .setLayout(layoutId)
                .setShowPattern(FloatDisplayType.OnlyCurrent)
                .setTag(ADD_TAG)
                .setDragEnable(false)
                .setSidePattern(FloatSideMode.FixedBottom)
                .setGravity(Gravity.BOTTOM or Gravity.END)
                .setAnimator(null)
                .registerCallback {
                    createResult { isCreated, _, view ->
                        if (!isCreated || view == null) return@createResult
                        if ((view as ViewGroup).childCount > 0) {
                            // 获取区间判断布局
                            view.getChildAt(0).apply {
                                if (this is BaseSwitchView) {
                                    addView = this
                                    translationX = width.toFloat()
                                    translationY = width.toFloat()
                                }
                            }
                        }
                    }
                    dismiss { addView = null }
                }
                .show()
        }
    }

    /**
     * 注册侧滑关闭浮窗
     * @param event 浮窗的触摸事件
     * @param listener 关闭区域触摸事件回调
     * @param layoutId 关闭区域的布局文件
     * @param floatDisplayType 关闭区域的浮窗类型
     * @param appFloatAnimator 关闭区域的浮窗出入动画
     */
    @JvmOverloads
    fun registerDragClose(
        event: MotionEvent,
        listener: OnTouchRangeListener? = null,
        layoutId: Int = R.layout.libs_debug_layout_float_default_swipe_close,
        floatDisplayType: FloatDisplayType = FloatDisplayType.OnlyCurrent,
        appFloatAnimator: OnFloatAnimator? = DefaultAnimator()
    ) {
        showClose(layoutId, floatDisplayType, appFloatAnimator)
        // 设置触摸状态监听
        closeView?.setTouchRangeListener(event, listener)
        // 抬起手指时，关闭删除选项
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) dismissClose()
    }

    private fun showClose(
        layoutId: Int,
        floatDisplayType: FloatDisplayType,
        appFloatAnimator: OnFloatAnimator?
    ) {
        if (FloatWindow.isShow(CLOSE_TAG)) return
        LifecycleUtils.application?.let {
            FloatWindow.with(it)
                .setLayout(layoutId)
                .setShowPattern(floatDisplayType)
                .setMatchParent(widthMatch = true)
                .setTag(CLOSE_TAG)
                .setSidePattern(FloatSideMode.FixedBottom)
                .setGravity(Gravity.BOTTOM)
                .setAnimator(appFloatAnimator)
                .registerCallback {
                    createResult { isCreated, _, view ->
                        if (!isCreated || view == null) return@createResult
                        if ((view as ViewGroup).childCount > 0) {
                            // 获取区间判断布局
                            view.getChildAt(0).apply { if (this is BaseSwitchView) closeView = this }
                        }
                    }
                    dismiss { closeView = null }
                }
                .show()
        }
    }

    private fun dismissAdd() = FloatWindow.dismiss(ADD_TAG)

    private fun dismissClose() = FloatWindow.dismiss(CLOSE_TAG)

}