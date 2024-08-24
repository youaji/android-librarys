package com.youaji.libs.debug.widget._float.core

import android.content.Context
import android.view.View
import com.youaji.libs.debug.widget._float.WARN_REPEATED_TAG
import com.youaji.libs.debug.widget._float.FloatConfig
import com.youaji.libs.debug.widget._float.utils.Logger
import java.util.concurrent.ConcurrentHashMap

/**
 * 负责多个悬浮窗的管理
 * @author youaji
 * @since 2024/01/05
 */
internal object FloatWindowManager {

    private const val DEFAULT_TAG = "default"
    val windowMap = ConcurrentHashMap<String, FloatWindowHelper>()

    /**
     * 创建浮窗，tag不存在创建，tag存在创建失败
     * 创建结果通过tag添加到相应的map进行管理
     */
    fun create(context: Context, config: FloatConfig) {
        if (!checkTag(config)) {
            val helper = FloatWindowHelper(context, config)
            helper.createWindow(object : FloatWindowHelper.CreateCallback {
                override fun onCreate(success: Boolean) {
                    if (success) windowMap[config.floatTag!!] = helper
                }
            })
        } else {
            // 存在相同的tag，直接创建失败
            config.callback?.createdResult(false, WARN_REPEATED_TAG, null)
            config.floatCallback?.builder?.createdResult?.invoke(false, WARN_REPEATED_TAG, null)
            Logger.w(WARN_REPEATED_TAG)
        }
    }

    /**
     * 关闭浮窗，执行浮窗的退出动画
     */
    fun dismiss(tag: String? = null, force: Boolean = false) =
        getHelper(tag)?.run { if (force) remove(force) else exitAnim() }

    /**
     * 移除当条浮窗信息，在退出完成后调用
     */
    fun remove(floatTag: String?) = windowMap.remove(getTag(floatTag))

    /**
     * 设置浮窗的显隐，用户主动调用隐藏时，needShow需要为false
     */
    fun visible(
        isShow: Boolean,
        tag: String? = null,
        needShow: Boolean = windowMap[tag]?.config?.needShow ?: true
    ) = getHelper(tag)?.setVisible(if (isShow) View.VISIBLE else View.GONE, needShow)

    /**
     * 检测浮窗的tag是否有效，不同的浮窗必须设置不同的tag
     */
    private fun checkTag(config: FloatConfig): Boolean {
        // 如果未设置tag，设置默认tag
        config.floatTag = getTag(config.floatTag)
        return windowMap.containsKey(config.floatTag!!)
    }

    /**
     * 获取浮窗tag，为空则使用默认值
     */
    private fun getTag(tag: String?) = tag ?: DEFAULT_TAG

    /**
     * 获取具体的系统浮窗管理类
     */
    fun getHelper(tag: String?) = windowMap[getTag(tag)]

}