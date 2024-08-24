package com.youaji.libs.debug.crash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.youaji.libs.debug.crash.compat.ActivityKillerV15_V20
import com.youaji.libs.debug.crash.compat.ActivityKillerV21_V23
import com.youaji.libs.debug.crash.compat.ActivityKillerV24_V25
import com.youaji.libs.debug.crash.compat.ActivityKillerV26
import com.youaji.libs.debug.crash.compat.ActivityKillerV28
import com.youaji.libs.debug.crash.compat.IActivityKiller
import com.youaji.libs.debug.crash.reflection.Reflection
import com.youaji.libs.debug.crash.util.logW_LDC

/**
 * 异常帮助类
 *
 * @author youaji
 * @since 2024/01/05
 */
class CrashHelper private constructor() {

    companion object {
        private var activityKiller: IActivityKiller? = null
        private var exceptionHandler: ExceptionHandler? = null

        /** 标记位，避免重复安装卸载 */
        private var isInstalled = false
        private var isSafeMode = false

        val get: CrashHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrashHelper()
        }
    }

    fun install(ctx: Context) {
        if (isInstalled) return
        try {
            // 解除 android P 反射限制
            Reflection.unseal(ctx)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        isInstalled = true
        initActivityKiller()
    }

    fun setSafe(thread: Thread, ex: Throwable?) {
        logW_LDC("setSafe--- thread --- ${thread.name}")
        // 判断是否是同一个线程
        if (thread === Looper.getMainLooper().thread) {
            isChoreographerException(ex)
            safeMode()
            logW_LDC("setSafe--- safeMode ---")
        }
    }

    fun setExceptionHandler(exceptionHandler: ExceptionHandler?) {
        Companion.exceptionHandler = exceptionHandler
    }

    /**
     * 替换 ActivityThread.mH.mCallback，实现拦截 Activity 生命周期，直接忽略生命周期的异常的话会导致黑屏
     * 目前会调用 ActivityManager 的 finishActivity 结束掉生命周期抛出异常的 Activity
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun initActivityKiller() {
        // 各版本 ActivityManager 获取方式，finishActivity 的参数，token(binder 对象)的获取不一样
        if (Build.VERSION.SDK_INT >= 28) {
            activityKiller = ActivityKillerV28()
        } else if (Build.VERSION.SDK_INT >= 26) {
            activityKiller = ActivityKillerV26()
        } else if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 24) {
            activityKiller = ActivityKillerV24_V25()
        } else if (Build.VERSION.SDK_INT in 21..23) {
            activityKiller = ActivityKillerV21_V23()
        } else if (Build.VERSION.SDK_INT in 15..20) {
            activityKiller = ActivityKillerV15_V20()
        } else if (Build.VERSION.SDK_INT < 15) {
            activityKiller = ActivityKillerV15_V20()
        }

        try {
            hookmH()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    @Throws(Exception::class)
    private fun hookmH() {
        val launchActivity = 100
        val pauseActivity = 101
        val pauseActivityFinishing = 102
        val stopActivityHide = 104
        val resumeActivity = 107
        val destroyActivity = 109
//        val newIntent = 112
//        val relaunchActivity = 126
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getDeclaredMethod("currentActivityThread").invoke(null)
        val mhField = activityThreadClass.getDeclaredField("mH")
        mhField.isAccessible = true
        val mhHandler = mhField[activityThread] as Handler
        val callbackField = Handler::class.java.getDeclaredField("mCallback")
        callbackField.isAccessible = true
        callbackField[mhHandler] = Handler.Callback { msg ->
            if (Build.VERSION.SDK_INT >= 28) {
                // android P 生命周期全部走这
                val executeTransaction = 159
                if (msg.what == executeTransaction) {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        activityKiller?.finishLaunchActivity(msg)
                        notifyException(throwable)
                    }
                    return@Callback true
                }
                return@Callback false
            }
            when (msg.what) {
                launchActivity -> {
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        activityKiller?.finishLaunchActivity(msg)
                        notifyException(throwable)
                    }
                    return@Callback true
                }

                resumeActivity -> {
                    // 回到activity onRestart onStart onResume
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        activityKiller?.finishResumeActivity(msg)
                        notifyException(throwable)
                    }
                    return@Callback true
                }

                pauseActivityFinishing -> {
                    // 按返回键 onPause
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        activityKiller?.finishPauseActivity(msg)
                        notifyException(throwable)
                    }
                    return@Callback true
                }

                pauseActivity -> {
                    // 开启新页面时，旧页面执行 activity.onPause
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        activityKiller?.finishPauseActivity(msg)
                        notifyException(throwable)
                    }
                    return@Callback true
                }

                stopActivityHide -> {
                    // 开启新页面时，旧页面执行 activity.onStop
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        activityKiller?.finishStopActivity(msg)
                        notifyException(throwable)
                    }
                    return@Callback true
                }

                destroyActivity -> {
                    // 关闭 activity onStop  onDestroy
                    try {
                        mhHandler.handleMessage(msg)
                    } catch (throwable: Throwable) {
                        notifyException(throwable)
                    }
                    return@Callback true
                }
            }
            false
        }
    }

    private fun notifyException(throwable: Throwable) {
        if (exceptionHandler == null) return
        if (isSafeMode) {
            exceptionHandler?.bandageExceptionHappened(throwable)
        } else {
            exceptionHandler?.uncaughtExceptionHappened(Looper.getMainLooper().thread, throwable)
            safeMode()
        }
    }

    /** 开启保护模式 */
    private fun safeMode() {
        isSafeMode = true
        exceptionHandler?.enterSafeMode()
        //开启一个循环
        while (true) {
            try {
                Looper.loop()
            } catch (e: Throwable) {
                isChoreographerException(e)
                exceptionHandler?.bandageExceptionHappened(e)
            }
        }
    }

    /**
     * view measure layout draw 时抛出异常会导致 Choreographer 挂掉
     * 建议直接杀死 app。以后的版本会只关闭黑屏的 Activity
     *
     * @param throwable Throwable
     */
    private fun isChoreographerException(throwable: Throwable?) {
        if (throwable == null || exceptionHandler == null) {
            return
        }
        val elements = throwable.stackTrace ?: return
        for (i in elements.size - 1 downTo -1 + 1) {
            if (elements.size - i > 20) {
                return
            }
            val element = elements[i]
            if ("android.view.Choreographer" == element.className &&
                "Choreographer.java" == element.fileName &&
                "doFrame" == element.methodName
            ) {
                exceptionHandler?.mayBeBlackScreen(throwable)
                return
            }
        }
    }
}