@file:Suppress("unused")
package com.youaji.libs.util

import android.os.Handler
import android.os.Looper

val mainThreadHandler by lazy { Handler(Looper.getMainLooper()) }

/**
 * 是否在主线程
 */
val isMainThread: Boolean get() = Looper.myLooper() == Looper.getMainLooper()

/**
 * 在主线程运行
 */
fun mainThread(block: () -> Unit) {
  if (isMainThread) block() else mainThreadHandler.post(block)
}

fun mainThread(delayMillis: Long, block: () -> Unit) =
  mainThreadHandler.postDelayed(block, delayMillis)
