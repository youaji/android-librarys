package com.youaji.libs.debug.crash

import android.app.Application
import android.content.Context
import com.youaji.libs.debug.crash.util.logD_LDC
import com.youaji.libs.debug.crash.util.logW_LDC

/**
 * 异常处理类
 * @author youaji
 * @since 2024/01/05
 */
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    companion object {
        val get: CrashHandler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrashHandler()
        }
    }

    /** 系统默认的 UncaughtException 处理类 */
    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    /** 程序的Context对象 */
    private var context: Context? = null

    /** 监听 */
    private var crashListener: CrashListener? = null

    /** 是否写崩溃日志到 file 文件夹，默认开启 */
    private var isSaveLogFile = true

    /** 点击按钮异常后设置处理崩溃而是关闭当前 activity */
    private var isFinishCurrent = false

    /**
     * 初始化
     * 获取系统默认的 UncaughtException 处理器,
     * 设置该 CrashHandler 为程序的默认处理器
     *
     * @param application   application
     */
    fun init(application: Application, listener: CrashListener? = null) {
        if (isFinishCurrent) {
            CrashHelper.get.install(application)
        }
        context = application
        this.crashListener = listener
        // 获取系统默认的 UncaughtExceptionHandler
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        logD_LDC("init defaultUncaughtExceptionHandler --- $defaultUncaughtExceptionHandler")
        // 将当前实例设为系统默认的异常处理器
        // 设置一个处理者当一个线程突然因为一个未捕获的异常而终止时将自动被调用。
        // 未捕获的异常处理的控制第一个被当前线程处理，如果该线程没有捕获并处理该异常，其将被线程的 ThreadGroup 对象处理，最后被默认的未捕获异常处理器处理。
        Thread.setDefaultUncaughtExceptionHandler(this)
        logD_LDC("init setDefaultUncaughtExceptionHandler --- ${Thread.getDefaultUncaughtExceptionHandler()}")
    }

    fun setSaveLogFile(saveLogFile: Boolean) {
        isSaveLogFile = saveLogFile
    }

    fun setFinishCurrent(finishCurrent: Boolean) {
        isFinishCurrent = finishCurrent
    }

    /**
     * 当 UncaughtException 发生时会转入该函数来处理
     * 该方法来实现对运行时线程进行异常处理
     */
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val isHandle = handleException(throwable)
        logD_LDC("uncaughtException --- handleException --- $isHandle")

        initCustomBug(throwable)

        if (defaultUncaughtExceptionHandler != null && !isHandle) {
            // 收集完信息后，交给系统自己处理崩溃
            // 当给定的线程因为发生了未捕获的异常而导致终止时将通过该方法将线程对象和异常对象传递进来。
            logD_LDC("uncaughtException --- throwable")
            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)
        } else {
            //否则自己处理
            if (context is Application) {
                logW_LDC("handleException --- againStartApplication")
                crashListener?.againStartApplication()
            }
        }

        if (isFinishCurrent) {
            CrashHelper.get.setSafe(thread, throwable)
        }
    }

    /** 自定义上传crash */
    private fun initCustomBug(throwable: Throwable) {
        //捕获监听中异常，防止外部开发者使用方代码抛出异常时导致的反复调用
        try {
            crashListener?.recordedException(throwable)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 自定义错误处理,收集错误信息，发送错误报告等操作均在此完成.
     * 开发者可以根据自己的情况来自定义异常处理逻辑
     */
    private fun handleException(throwable: Throwable): Boolean {
        // 收集crash信息
        val message = throwable.localizedMessage ?: return false
        logW_LDC("handleException\n$message")
        throwable.printStackTrace()
        if (isSaveLogFile) {
            context?.let { CrashFileHandler.saveCrashInfoInFile(it, throwable) }
        }
        return true
    }
}