package com.youaji.libs.debug.crash

/**
 * @author youaji
 * @since 2024/01/05
 */
abstract class ExceptionHandler {
    /**
     * 子线程抛出异常时始终调用该方法。
     * 主线程只有第一次抛出异常时才会调用该方法，该方法中到的 throwable 都可上报。
     * 以后主线程的异常只调用 [onBandageExceptionHappened]
     *
     * @param thread
     * @param throwable
     */
    protected abstract fun onUncaughtExceptionHappened(thread: Thread?, throwable: Throwable)

    /**
     * 当原本导致app崩溃的主线程异常发生后，主线程再次抛出导致 app 崩溃异常时会调用该方法。
     * 自己 try catch 的异常不会导致 app 崩溃，该方法中到的 throwable 不会上报，也无需上报，
     * 因为本次异常可能是由于第一次主线程异常时 app 没有崩溃掉才发生的，只要修复了 bug 就不会发生该异常了
     * @param throwable 主线程的异常
     */
    protected abstract fun onBandageExceptionHappened(throwable: Throwable)
    protected abstract fun onEnterSafeMode()
    protected fun onMayBeBlackScreen(e: Throwable) {}

    fun uncaughtExceptionHappened(thread: Thread?, throwable: Throwable) {
        try {
            // 捕获监听中异常，防止使用方代码抛出异常时导致的反复调用
            onUncaughtExceptionHappened(thread, throwable)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun bandageExceptionHappened(throwable: Throwable) {
        try {
            // 捕获监听中异常，防止使用方代码抛出异常时导致的反复调用
            onBandageExceptionHappened(throwable)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun enterSafeMode() {
        try {
            onEnterSafeMode()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    fun mayBeBlackScreen(e: Throwable) {
        try {
            onMayBeBlackScreen(e)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }
}