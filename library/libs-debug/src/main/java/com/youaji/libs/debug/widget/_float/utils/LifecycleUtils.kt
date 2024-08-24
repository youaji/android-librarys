package com.youaji.libs.debug.widget._float.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.youaji.libs.debug.widget._float.core.FloatWindowManager
import com.youaji.libs.debug.widget._float.FloatDisplayType
import java.lang.ref.WeakReference

/**
 * 通过生命周期回调，判断系统浮窗的过滤信息，以及app是否位于前台，控制浮窗显隐
 * @author youaji
 * @since 2024/01/05
 */
internal object LifecycleUtils {

    var application: Application? = null
    private var activityCount = 0
    private var mTopActivity: WeakReference<Activity>? = null

    fun isForeground() = activityCount > 0

    fun getTopActivity(): Activity? = mTopActivity?.get()

    fun setLifecycleCallbacks(application: Application) {
        this.application = application
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                // 计算启动的activity数目
                activityCount++
            }

            override fun onActivityResumed(activity: Activity) {
                mTopActivity?.clear()
                mTopActivity = WeakReference<Activity>(activity)
                // 每次都要判断当前页面是否需要显示
                checkShow(activity)
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                // 计算关闭的activity数目，并判断当前App是否处于后台
                activityCount--
                checkHide(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    /**
     * 判断浮窗是否需要显示
     */
    private fun checkShow(activity: Activity) =
        FloatWindowManager.windowMap.forEach { (tag, manager) ->
            manager.config.apply {
                when {
                    // 当前页面的浮窗，不需要处理
                    floatDisplayType == FloatDisplayType.OnlyCurrent -> return@apply
                    // 仅后台显示模式下，隐藏浮窗
                    floatDisplayType == FloatDisplayType.OnlyBackground -> setVisible(false, tag)
                    // 如果没有手动隐藏浮窗，需要考虑过滤信息
                    needShow -> setVisible(activity.componentName.className !in filterSet, tag)
                }
            }
        }

    /**
     * 判断浮窗是否需要隐藏
     */
    private fun checkHide(activity: Activity) {
        // 如果不是finish，并且处于前台，无需判断
        if (!activity.isFinishing && isForeground()) return
        FloatWindowManager.windowMap.forEach { (tag, manager) ->
            // 判断浮窗是否需要关闭
            if (activity.isFinishing) manager.params.token?.let {
                // 如果token不为空，并且是当前销毁的Activity，关闭浮窗，防止窗口泄漏
                if (it == activity.window?.decorView?.windowToken) {
                    FloatWindowManager.dismiss(tag, true)
                }
            }

            manager.config.apply {
                if (!isForeground() && manager.config.floatDisplayType != FloatDisplayType.OnlyCurrent) {
                    // 当app处于后台时，全局、仅后台显示的浮窗，如果没有手动隐藏，需要显示
                    setVisible(floatDisplayType != FloatDisplayType.OnlyForeground && needShow, tag)
                }
            }
        }
    }



    private fun setVisible(isShow: Boolean = isForeground(), tag: String?) =
        FloatWindowManager.visible(isShow, tag)

}