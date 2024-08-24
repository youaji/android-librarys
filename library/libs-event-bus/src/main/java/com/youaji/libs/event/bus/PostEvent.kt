package com.youaji.libs.event.bus

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

/**
 * 应用范围的事件
 * @param event         事件类型
 * @param timeMillis    延迟时间
 */
inline fun <reified T> postEvent(event: T, timeMillis: Long = 0L) {
    event?.let {
        ApplicationScopeViewModelProvider.getApplicationScopeViewModel(Core::class.java)
            .postEvent(T::class.java.name, event, timeMillis)
    }
}

/**
 * 限定范围的事件
 * @param scope         闲置范围
 * @param event         事件类型
 * @param timeMillis    延迟时间
 */
inline fun <reified T> postEvent(scope: ViewModelStoreOwner, event: T, timeMillis: Long = 0L) {
    event?.let {
        ViewModelProvider(scope)[Core::class.java]
            .postEvent(T::class.java.name, event, timeMillis)
    }
}