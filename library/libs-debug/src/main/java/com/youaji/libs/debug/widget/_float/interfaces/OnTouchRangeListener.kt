package com.youaji.libs.debug.widget._float.interfaces

import com.youaji.libs.debug.widget._float.widget.BaseSwitchView

/**
 * 区域触摸事件回调
 * @author youaji
 * @since 2024/01/05
 */
interface OnTouchRangeListener {

    /**
     * 手指触摸到指定区域
     */
    fun touchInRange(inRange: Boolean, view: BaseSwitchView)

    /**
     * 在指定区域抬起手指
     */
    fun touchUpInRange()

}