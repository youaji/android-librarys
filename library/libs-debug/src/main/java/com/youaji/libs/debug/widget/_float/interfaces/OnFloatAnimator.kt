package com.youaji.libs.debug.widget._float.interfaces

import android.animation.Animator
import android.view.View
import android.view.WindowManager
import com.youaji.libs.debug.widget._float.FloatSideMode

/**
 * 系统浮窗的出入动画
 * @author youaji
 * @since 2024/01/05
 */
interface OnFloatAnimator {

    fun enterAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        floatSideMode: FloatSideMode
    ): Animator? = null

    fun exitAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        floatSideMode: FloatSideMode
    ): Animator? = null

}