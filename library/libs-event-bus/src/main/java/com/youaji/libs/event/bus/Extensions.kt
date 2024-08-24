@file:Suppress("unused")
package com.youaji.libs.event.bus

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

inline fun <reified T> getEventObserverCount(event: Class<T>): Int {
    return ApplicationScopeViewModelProvider.getApplicationScopeViewModel(Core::class.java)
        .getEventObserverCount(event.name)
}

inline fun <reified T> getEventObserverCount(scope: ViewModelStoreOwner, event: Class<T>): Int {
    return ViewModelProvider(scope)[Core::class.java]
        .getEventObserverCount(event.name)
}


//移除粘性事件
inline fun <reified T> removeStickyEvent(event: Class<T>) {
    ApplicationScopeViewModelProvider.getApplicationScopeViewModel(Core::class.java)
        .removeStickEvent(event.name)
}

inline fun <reified T> removeStickyEvent(scope: ViewModelStoreOwner, event: Class<T>) {
    ViewModelProvider(scope)[Core::class.java]
        .removeStickEvent(event.name)
}


// 清除粘性事件缓存
inline fun <reified T> clearStickyEvent(event: Class<T>) {
    ApplicationScopeViewModelProvider.getApplicationScopeViewModel(Core::class.java)
        .clearStickEvent(event.name)
}

inline fun <reified T> clearStickyEvent(scope: ViewModelStoreOwner, event: Class<T>) {
    ViewModelProvider(scope)[Core::class.java]
        .clearStickEvent(event.name)
}


fun <T> LifecycleOwner.launchWhenStateAtLeast(minState: Lifecycle.State, block: suspend CoroutineScope.() -> T): Job =
    lifecycleScope.launch { lifecycle.whenStateAtLeast(minState, block) }
