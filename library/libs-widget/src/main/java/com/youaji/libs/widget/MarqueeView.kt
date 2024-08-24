@file:Suppress("unused")
package com.youaji.libs.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ViewFlipper

/**
 * @author youaji
 * @since 2023/2/9
 */
class MarqueeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : ViewFlipper(context, attrs) {

    private val isSetAnimDuration = false
    private val interval = 4000

    /** 动画时间 */
    private val animDuration = 500

    /** 点击 */
    private var onItemClickListener: OnItemClickListener? = null


    init {
        flipInterval = interval
        val animIn = AnimationUtils.loadAnimation(context, R.anim.anim_in_down)
        if (isSetAnimDuration) animIn.duration = animDuration.toLong()
        inAnimation = animIn
        val animOut = AnimationUtils.loadAnimation(context, R.anim.anim_out_up)
        if (isSetAnimDuration) animOut.duration = animDuration.toLong()
        outAnimation = animOut
    }

    /**
     * 设置循环滚动的 View 数组
     * @param views
     */
    fun setViews(views: List<View>) {
        if (views.isEmpty()) return
        removeAllViews()
        for (i in views.indices) {
            views[i].parent?.let {
                (it as ViewGroup).removeAllViews()
            }
            addView(views[i])
            // 设置监听回调
            views[i].setOnClickListener {
                onItemClickListener?.onItemClick(i, views[i])
            }
        }
        startFlipping()
    }

    /**
     * 设置监听接口
     * @param onItemClickListener
     */
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    /**
     * itemView 接口
     */
    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }

}