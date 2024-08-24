@file:Suppress("unused")
package com.youaji.libs.event.bus

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import kotlinx.coroutines.*

/**
 * 监听 App Scope 事件
 * @param dispatcher
 * @param minActiveState    处于对应生命周期阶段时方可触发
 * @param isSticky          粘性事件
 * @param onReceived        响应回调
 */
@MainThread
inline fun <reified T> LifecycleOwner.observeEvent(
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    isSticky: Boolean = false,
    noinline onReceived: (T) -> Unit
): Job {
    return ApplicationScopeViewModelProvider.getApplicationScopeViewModel(Core::class.java)
        .observeEvent(
            this,
            T::class.java.name,
            minActiveState,
            dispatcher,
            isSticky,
            onReceived
        )
}

/**
 * 监听 Fragment Scope 事件
 * @param scope             作用范围
 * @param dispatcher
 * @param minActiveState    处于对应生命周期阶段时方可触发
 * @param isSticky          粘性事件
 * @param onReceived        响应回调
 */
@MainThread
inline fun <reified T> observeEvent(
    scope: Fragment,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    isSticky: Boolean = false,
    noinline onReceived: (T) -> Unit
): Job {
    return ViewModelProvider(scope)[Core::class.java]
        .observeEvent(
            scope,
            T::class.java.name,
            minActiveState,
            dispatcher,
            isSticky,
            onReceived
        )
}

/**
 * Fragment 监听 Activity Scope 事件
 * @param scope             作用范围
 * @param dispatcher
 * @param minActiveState    处于对应生命周期阶段时方可触发
 * @param isSticky          粘性事件
 * @param onReceived        响应回调
 */
@MainThread
inline fun <reified T> observeEvent(
    scope: ComponentActivity,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    isSticky: Boolean = false,
    noinline onReceived: (T) -> Unit
): Job {
    return ViewModelProvider(scope)[Core::class.java]
        .observeEvent(
            scope,
            T::class.java.name,
            minActiveState,
            dispatcher,
            isSticky,
            onReceived
        )
}

/**
 * @param coroutineScope    作用域
 * @param isSticky          粘性事件
 * @param onReceived        响应回调
 */
@MainThread
inline fun <reified T> observeEvent(
    coroutineScope: CoroutineScope,
    isSticky: Boolean = false,
    noinline onReceived: (T) -> Unit
): Job {
    return coroutineScope.launch {
        ApplicationScopeViewModelProvider.getApplicationScopeViewModel(Core::class.java)
            .observeWithoutLifecycle(
                T::class.java.name,
                isSticky,
                onReceived
            )
    }
}

/**
 * @param scope             作用范围
 * @param coroutineScope    作用域
 * @param isSticky          粘性事件
 * @param onReceived        响应回调
 */
@MainThread
inline fun <reified T> observeEvent(
    scope: ViewModelStoreOwner,
    coroutineScope: CoroutineScope,
    isSticky: Boolean = false,
    noinline onReceived: (T) -> Unit
): Job {
    return coroutineScope.launch {
        ViewModelProvider(scope)[Core::class.java]
            .observeWithoutLifecycle(
                T::class.java.name,
                isSticky,
                onReceived
            )
    }
}