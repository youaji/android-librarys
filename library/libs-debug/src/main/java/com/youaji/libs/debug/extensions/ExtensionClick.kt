package com.youaji.libs.debug.extensions

private var lastFastMultipleClickTimeMillis: Long = 0 // 上次点击时间
private var currFastMultipleClickCount = 0 // 当前快速多次点击次数

/**
 * 快速多次点击
 * @param triggerCount      触发次数，默认10次
 * @param longTimeMillis    多长时间内，默认1000毫秒
 * @param callback          触发后的回调
 */
fun fastMultipleClick(
    triggerCount: Int = 10,
    longTimeMillis: Long = 1000,
    callback: () -> Unit
) {
    val currTimeMillis = System.currentTimeMillis()
    if ((currTimeMillis - lastFastMultipleClickTimeMillis) < longTimeMillis) {
        currFastMultipleClickCount += 1
    } else {
        currFastMultipleClickCount = 1
    }
    lastFastMultipleClickTimeMillis = currTimeMillis
    if (currFastMultipleClickCount == triggerCount) {
        callback.invoke()
    }
}