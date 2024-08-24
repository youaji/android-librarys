@file:Suppress("unused")
package com.youaji.libs.debug.widget._float

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.youaji.libs.debug.widget._float.utils.LifecycleUtils

/**
 * 生命周期回调的初始化
 * @author youaji
 * @since 2024/01/05
 */
class FloatWindowInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        LifecycleUtils.setLifecycleCallbacks(context.applicationContext as Application)
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}