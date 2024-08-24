package com.youaji.libs.ui.basic

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow

abstract class BasicPopupWindow @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : PopupWindow(context, attrs, defStyleAttr, defStyleRes) {

    /** 屏幕宽度 */
    private val screenWidth: Int get() = Resources.getSystem().displayMetrics.widthPixels

    /** 屏幕高度 */
    private val screenHeight: Int get() = Resources.getSystem().displayMetrics.heightPixels
    private var dismissListener: OnDismissListener? = null

    private var windowWidth: Int = WindowManager.LayoutParams.WRAP_CONTENT
    private var windowHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT

    init {
        this.getIntercept()?.intercept()
        super.setOnDismissListener {
            resetAlpha(context)
            dismissListener?.onDismiss()
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        contentView?.let { resetAlpha(it.context, getAlpha()) }
        getIntercept()?.showBefore()
        super.showAsDropDown(anchor, xoff, yoff)
        getIntercept()?.showAfter()
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        contentView?.let { resetAlpha(it.context, getAlpha()) }
        getIntercept()?.showBefore()
        super.showAtLocation(parent, gravity, x, y)
        getIntercept()?.showAfter()
    }

    override fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        this.dismissListener = onDismissListener
    }

    private fun resetAlpha(context: Context, alpha: Float = 1.0f) {
        if (context is Activity) {
            val layoutParams = context.window.attributes
            if (layoutParams != null && layoutParams.alpha != alpha) {
                /* 0.0 ~ 1.0*/
                layoutParams.alpha = alpha
                context.window.attributes = layoutParams
            }
        }
    }

    private fun setSize(anchorView: View) {
        this.width = windowWidth
        this.height = windowHeight
//        when (windowWidth) {
////            -1 -> this.width = ViewGroup.LayoutParams.WRAP_CONTENT
//            -1 -> this.width = contentView.measuredWidth
//            0 -> this.width = screenWidth
//            else -> this.width = windowWidth
//        }
//
//        when (windowHeight) {
//            -2 -> {
//                val anchorHeight = anchorView.measuredHeight
//                val outLocation = IntArray(2)
//                anchorView.getLocationInWindow(outLocation)
//                this.height = screenHeight - outLocation[1] - anchorHeight - 1
//            }
//
//            -3 -> {
//                val outLocation = IntArray(2)
//                anchorView.getLocationInWindow(outLocation)
//                this.height = outLocation[1]
//            }
//
////            -1 -> this.height = ViewGroup.LayoutParams.WRAP_CONTENT
//            -1 -> this.height = contentView.measuredHeight
//            0 -> this.height = screenHeight
//            else -> this.height = windowHeight
//        }
    }

    /**
     * 窗口弹出时的背景透明度
     * 0f(透明) ~ 1.0f(正常)
     * 设置了 alpha 时需要在 onDismiss 恢复窗口的 alpha 至默认值(1.0f)
     */
    abstract fun getAlpha(): Float

    abstract fun getInstance(): BasicPopupWindow

    abstract fun getIntercept(): InterceptTransform?

    abstract class InterceptTransform {
        abstract fun showBefore()
        abstract fun showAfter()
        abstract fun intercept()
    }

    /***/
    fun contentView(value: View): BasicPopupWindow {
        this.contentView = value
        return this
    }

    /**
     * (w=0 or h=0) => 全屏
     * (w=-1 or h=-1) => 内容自适应
     * (h=-2) => 布局之下到屏幕底部的高度 TODO: 未完善，暂不可用
     * (h=-3) => 布局之上到屏幕顶部的高度 TODO: 未完善，暂不可用
     */
    fun size(width: Int, height: Int): BasicPopupWindow {
        this.windowWidth = width
        this.windowHeight = height
        return this
    }

    /***/
    fun animationStyle(value: Int): BasicPopupWindow {
        this.animationStyle = value
        return this
    }

    /** 设置背景 */
    fun backgroundDrawable(value: Drawable): BasicPopupWindow {
        this.setBackgroundDrawable(value)
        return this
    }

    /** 设置背景 */
    fun backgroundColor(value: Int): BasicPopupWindow {
        this.setBackgroundDrawable(ColorDrawable(value))
        return this
    }

    /** 设置Outside是否可点击 */
    fun outsideTouchable(value: Boolean): BasicPopupWindow {
        this.isOutsideTouchable = value
        return this
    }

    /***/
    fun focusable(value: Boolean): BasicPopupWindow {
        this.isFocusable = value
        return this
    }

    /**
     * 结合 showAtLocation 使用精准定位，需设置 clippingEnabled 为 false
     * 否则当内容过多时会移位，比如设置在某个控件底下内容过多时 PopupWindow 会上移
     */
    fun clippingEnabled(value: Boolean): BasicPopupWindow {
        this.isClippingEnabled = value
        return this
    }

    fun show(anchorView: View, gravity: Int) {
        when (gravity) {
            Gravity.CENTER -> showAtLocation(anchorView, gravity, 0, 0)
            Gravity.BOTTOM -> showAtLocation(anchorView, gravity, 0, 0)
            Gravity.TOP -> showAtLocation(anchorView, gravity, 0, 0);
            Gravity.START -> showAtLocation(anchorView, gravity, 0, 0);
            Gravity.END -> showAtLocation(anchorView, gravity, 0, 0);
        }
    }

    fun show(anchorView: View, gravity: GRAVITY) {
        setSize(anchorView)
        val pwWidth = width
        val pwHeight = height
        val anchorWidth = anchorView.measuredWidth
        val anchorHeight = anchorView.measuredHeight
        // 获取在当前窗口内的绝对坐标，含 toolBar
        val outLocation = IntArray(2)
        anchorView.getLocationInWindow(outLocation)
        when (gravity) {
            /*
             * showAsDropDown 参数 xOff，yOff 说明
             * 两参数是指距离原点的偏移量
             * 原点：anchorView 左下角的坐标作参考，为[0,0]
             * 偏移量：window 左上角坐标点距离原点的偏移差
             *
             * TODO：此处还需再次验证
             */
            GRAVITY.LEFT_TOP_2_LEFT_TOP -> {
                // 屏宽 - 锚点x < 弹窗宽 表示无法显示完全
                if (screenWidth - outLocation[0] < pwWidth) {
                    showAsDropDown(anchorView, -pwWidth, -anchorHeight)
                } else {
                    showAsDropDown(anchorView, 0, -anchorHeight)
                }
            }

            GRAVITY.LEFT_TOP_2_LEFT_BOTTOM -> {
                // 屏宽 - 锚点x < 弹窗宽 表示无法显示完全
                if (screenWidth - outLocation[0] < pwWidth) {
                    showAsDropDown(anchorView, -pwWidth, 0)
                } else {
                    showAsDropDown(anchorView, 0, 0)
                }
            }

            GRAVITY.LEFT_TOP_2_RIGHT_TOP -> {
                // 屏宽 - 锚点x - 描点宽 < 弹窗宽 表示无法显示完全
                if (screenWidth - outLocation[0] - anchorWidth < pwWidth) {
                    showAsDropDown(anchorView, anchorWidth - pwWidth, -anchorHeight)
                } else {
                    showAsDropDown(anchorView, anchorWidth, -anchorHeight)
                }
            }

            GRAVITY.LEFT_TOP_2_RIGHT_BOTTOM -> {
                // 屏宽 - 锚点x - 描点宽 < 弹窗宽 表示无法显示完全
                if (screenWidth - outLocation[0] - anchorWidth < pwWidth) {
                    showAsDropDown(anchorView, anchorWidth - pwWidth, 0)
                } else {
                    showAsDropDown(anchorView, anchorWidth, 0)
                }
            }

            GRAVITY.LEFT_BOTTOM_2_LEFT_TOP ->
                showAsDropDown(anchorView, 0, -(pwHeight + anchorHeight))

            GRAVITY.LEFT_BOTTOM_2_RIGHT_TOP ->
                showAsDropDown(anchorView, anchorWidth, -(pwHeight + anchorHeight))

            GRAVITY.RIGHT_TOP_2_LEFT_BOTTOM -> {
                // 锚点x < 弹窗宽 表示无法显示完全
                if (outLocation[0] < pwWidth) {
                    showAsDropDown(anchorView, anchorWidth, 0)
                } else {
                    showAsDropDown(anchorView, -pwWidth, 0)
                }
            }

            GRAVITY.RIGHT_TOP_2_RIGHT_TOP -> {
                // 锚点x + 描点宽 < 弹窗宽 表示无法显示完全
                if (outLocation[0] + anchorWidth < pwWidth) {
                    showAsDropDown(anchorView, 0, -anchorHeight)
                } else {
                    showAsDropDown(anchorView, anchorWidth - pwWidth, -anchorHeight)
                }
            }

            GRAVITY.RIGHT_TOP_2_RIGHT_BOTTOM -> {
                // 锚点x + 描点宽 < 弹窗宽 表示无法显示完全
                if (outLocation[0] + anchorWidth < pwWidth) {
                    showAsDropDown(anchorView, 0, 0)
                } else {
                    showAsDropDown(anchorView, anchorWidth - pwWidth, 0)
                }
            }

            GRAVITY.RIGHT_BOTTOM_2_LEFT_TOP ->
                showAsDropDown(anchorView, -pwWidth, -(pwHeight + anchorHeight))

            GRAVITY.RIGHT_BOTTOM_2_RIGHT_TOP ->
                showAsDropDown(anchorView, anchorWidth - pwWidth, -(pwHeight + anchorHeight))
        }
        // fix: isShowing 总是返回 false
        // 原因: 焦点没有聚焦在 PopupWindow 上，最终以为 PopupWindow 对象是隐藏的
        isFocusable = true
        isTouchable = true
    }

    enum class GRAVITY {
        /***/
        LEFT_TOP_2_LEFT_TOP,

        /***/
        LEFT_TOP_2_LEFT_BOTTOM,

        /***/
        LEFT_TOP_2_RIGHT_TOP,

        /***/
        LEFT_TOP_2_RIGHT_BOTTOM,

        /***/
        LEFT_BOTTOM_2_LEFT_TOP,

        /***/
        LEFT_BOTTOM_2_RIGHT_TOP,

        /***/
        RIGHT_TOP_2_LEFT_BOTTOM,

        /***/
        RIGHT_TOP_2_RIGHT_TOP,

        /***/
        RIGHT_TOP_2_RIGHT_BOTTOM,

        /***/
        RIGHT_BOTTOM_2_LEFT_TOP,

        /***/
        RIGHT_BOTTOM_2_RIGHT_TOP,
    }
}