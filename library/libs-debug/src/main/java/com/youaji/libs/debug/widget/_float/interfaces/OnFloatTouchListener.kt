package com.youaji.libs.debug.widget._float.interfaces

import android.view.MotionEvent

/**
 * 系统浮窗的触摸事件
 * @author youaji
 * @since 2024/01/05
 */
internal interface OnFloatTouchListener {

    fun onTouch(event: MotionEvent)
}