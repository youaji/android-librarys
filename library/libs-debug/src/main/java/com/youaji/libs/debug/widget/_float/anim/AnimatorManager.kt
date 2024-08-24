package com.youaji.libs.debug.widget._float.anim

import android.animation.Animator
import android.view.View
import android.view.WindowManager
import com.youaji.libs.debug.widget._float.FloatConfig

/**
 * App浮窗的出入动画管理类
 * 只需传入具体的动画实现类（策略模式）
 * @author youaji
 * @since 2024/01/05
 */
internal class AnimatorManager(
    private val view: View,
    private val params: WindowManager.LayoutParams,
    private val windowManager: WindowManager,
    private val config: FloatConfig
) {

    fun enterAnim(): Animator? =
        config.floatAnimator?.enterAnim(view, params, windowManager, config.floatSideMode)

    fun exitAnim(): Animator? =
        config.floatAnimator?.exitAnim(view, params, windowManager, config.floatSideMode)
}