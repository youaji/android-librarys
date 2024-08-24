package com.youaji.libs.debug.crash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Process
import com.youaji.libs.debug.crash.util.NetDeviceUtils
import com.youaji.libs.debug.crash.util.FileUtil
import com.youaji.libs.debug.crash.util.MemoryUtils
import com.youaji.libs.debug.crash.util.ProcessUtils
import com.youaji.libs.debug.crash.util.logE_LDC
import com.youaji.libs.debug.crash.util.logI_LDC
import com.youaji.libs.util.appVersionCode
import com.youaji.libs.util.appVersionName
import com.youaji.libs.util.isExistOrCreateNewDir
import com.youaji.libs.util.isExistOrCreateNewFile
import com.youaji.libs.util.packageInfo
import com.youaji.libs.util.topActivity
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 异常处理保存文件类
 * @author youaji
 * @since 2024/01/05
 */
@SuppressLint("SimpleDateFormat")
object CrashFileHandler {
    private val dataFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss") }
    private var crashTime: String = ""
    private var crashAppInfo: String = ""
    private var crashDeviceInfo: String = ""
    private var crashMemoryInfo: String = ""
    private var crashThreadInfo: String = ""

    /**
     * 保存错误信息到文件中，一个崩溃保存到一个文件中
     * 待完成
     *      1、过了 n 天自动清除日志
     *      2、文件大小限制
     */
    fun saveCrashInfoInFile(context: Context, throwable: Throwable) {
        crashTime = dataFormat.format(Date(System.currentTimeMillis()))
        initAppInfo()
        initDeviceInfo()
        initMemoryInfo(context)
        initThreadInfo(context)
        writer2File(throwable)
    }

    private fun initAppInfo() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("$crashTime\n\n")
        stringBuilder.append("------ 应用信息 ------")
        stringBuilder.append("\n应用包名：").append(packageInfo.packageName)
        stringBuilder.append("\n应用版本：").append(appVersionName).append("-").append(appVersionCode)
        stringBuilder.append("\n构建类型：").append(BuildConfig.BUILD_TYPE)
        stringBuilder.append("\n\n")
        crashAppInfo = stringBuilder.toString()
    }

    private fun initDeviceInfo() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("------ 系统信息 ------")
        stringBuilder.append("\n设备品牌：").append(NetDeviceUtils.brand)
        stringBuilder.append("\n设备厂商：").append(NetDeviceUtils.manufacturer)
        stringBuilder.append("\n设备型号：").append(NetDeviceUtils.model)
        stringBuilder.append("\n设备版本：").append(NetDeviceUtils.id)
        stringBuilder.append("\n系统版本：Android").append(NetDeviceUtils.sDKVersionName)
        stringBuilder.append("\n系统版本：SDK").append(NetDeviceUtils.sDKVersionCode)
        stringBuilder.append("\nCPU类型 ：").append(NetDeviceUtils.cpuType)
        stringBuilder.append("\n是否ROOT：").append(NetDeviceUtils.isDeviceRooted)
        stringBuilder.append("\n\n")
        crashDeviceInfo = stringBuilder.toString()
    }

    private fun initMemoryInfo(context: Context) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("------ 内存信息 ------")
        val pid = MemoryUtils.currentPid
        val dalvikHeapMem = MemoryUtils.appDalvikHeapMem
        val pssInfo = MemoryUtils.getAppPssInfo(context, pid)
        stringBuilder.append("\n手机堆大小:").append(MemoryUtils.getFormatSize(pssInfo.nativePss.toDouble()))
        stringBuilder.append("\n其他比例大小:").append(MemoryUtils.getFormatSize(pssInfo.otherPss.toDouble()))
        stringBuilder.append("\ndalvik堆大小:").append(MemoryUtils.getFormatSize(pssInfo.dalvikPss.toDouble()))
        stringBuilder.append("\nPSS内存使用量:").append(MemoryUtils.getFormatSize(pssInfo.totalPss.toDouble()))
        stringBuilder.append("\n已用内存：").append(MemoryUtils.getFormatSize(dalvikHeapMem.allocated.toDouble()))
        stringBuilder.append("\n最大内存：").append(MemoryUtils.getFormatSize(dalvikHeapMem.maxMem.toDouble()))
        stringBuilder.append("\n空闲内存：").append(MemoryUtils.getFormatSize(dalvikHeapMem.freeMem.toDouble()))
        stringBuilder.append("\n应用占用内存：").append(MemoryUtils.getFormatSize(MemoryUtils.getAppTotalDalvikHeapSize(context).toDouble()))
        stringBuilder.append("\n\n")
        crashMemoryInfo = stringBuilder.toString()
    }

    private fun initThreadInfo(context: Context) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("------ 线程信息 ------")
        stringBuilder.append("\n进程名：").append(ProcessUtils.getCurrentProcessName(context) ?: "")
        stringBuilder.append("\n进程号：").append(Process.myPid())
        stringBuilder.append("\n当前线程号：").append(Process.myTid())
        stringBuilder.append("\n当前线程名：").append(Thread.currentThread().name)
        stringBuilder.append("\n当前UID：").append(Process.myUid())
        stringBuilder.append("\n当前线程ID：").append(Thread.currentThread().id)
        stringBuilder.append("\n主线程ID：").append(context.mainLooper.thread.id)
        stringBuilder.append("\n主线程名：").append(context.mainLooper.thread.name)
        stringBuilder.append("\n主线程优先级：").append(context.mainLooper.thread.priority)
        stringBuilder.append("\n当前页名称:").append(topActivity.componentName.className)
        stringBuilder.append("\n当前页所在栈的ID:").append(topActivity.taskId)
        stringBuilder.append("\n\n")
        crashThreadInfo = stringBuilder.toString()
    }

    private fun writer2File(throwable: Throwable) {
        var printWriter: PrintWriter? = null
        logI_LDC("\n$crashAppInfo$crashDeviceInfo$crashMemoryInfo$crashThreadInfo")
        try {
            val fileDir = File(FileUtil.crashFileDir)
            if (!fileDir.isExistOrCreateNewDir()) {
                logE_LDC("fileDir create error:$fileDir 不存在且创建失败")
                return
            }
            val tempFileName = "${System.currentTimeMillis()}.temp"
            val tempFile = File(fileDir, tempFileName)
            if (!tempFile.isExistOrCreateNewFile()) {
                logE_LDC("tempFile create error:$tempFile 不存在且创建失败")
                return
            }

            logI_LDC("error log temp file:$tempFileName")

            // 开始写日志
            printWriter = PrintWriter(BufferedWriter(FileWriter(tempFile)))
            printWriter.println(crashAppInfo)
            printWriter.println(crashDeviceInfo)
            printWriter.println(crashMemoryInfo)
            printWriter.println(crashThreadInfo)
            // 导出异常的调用栈信息
            throwable.printStackTrace(printWriter)
            // 异常信息
            var cause = throwable.cause
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            // 重新命名文件
            val throwableStr = throwable.toString()
            val throwableName =
                if (throwableStr.contains(":")) throwable.toString().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                else "UnknownException"
            val crashFile = FileUtil.getCrashFile(crashTime, throwableName)
            FileUtil.renameFile(tempFile, crashFile)
            logI_LDC("error log crash file:$crashFile")
        } catch (e: Exception) {
            logE_LDC("error log crash file save error:${e.message}")
        } finally {
            printWriter?.close()
        }
    }
}