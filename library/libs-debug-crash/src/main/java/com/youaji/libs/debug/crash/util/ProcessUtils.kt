package com.youaji.libs.debug.crash.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager.RunningTaskInfo
import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.Arrays
import java.util.Collections

/**
 * 进程工具类
 * @author youaji
 * @since 2024/01/05
 */
object ProcessUtils {
    private var currentProcessName: String? = null

    /**
     * 获取当前进程名
     * 我们优先通过 Application.getProcessName() 方法获取进程名。
     * 如果获取失败，我们再反射ActivityThread.currentProcessName()获取进程名
     * 如果失败，我们才通过常规方法ActivityManager来获取进程名
     * @return                      当前进程名
     */
    fun getCurrentProcessName(context: Context): String? {
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName
        }
        //1)通过Application的API获取当前进程名
        currentProcessName = currentProcessNameByApplication
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName
        }
        //2)通过反射ActivityThread获取当前进程名
        currentProcessName = currentProcessNameByActivityThread
        if (!TextUtils.isEmpty(currentProcessName)) {
            return currentProcessName
        }
        //3)通过ActivityManager获取当前进程名
        currentProcessName = getCurrentProcessNameByActivityManager(context)
        return currentProcessName
    }

    val currentProcessNameByApplication: String?
        /**
         * 通过Application新的API获取进程名，无需反射，无需IPC，效率最高。
         */
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //Application.getProcessName()方法直接返回当前进程名。
            //这个方法只有在android9【也就是aip28】之后的系统才能调用
            Application.getProcessName()
        } else null
    val currentProcessNameByActivityThread: String?
        /**
         * 通过反射ActivityThread获取进程名，避免了ipc
         */
        get() {
            var processName: String? = null
            try {
                //ActivityThread.currentProcessName()方法居然是public static的
                @SuppressLint("PrivateApi") val declaredMethod = Class.forName(
                    "android.app.ActivityThread",
                    false, Application::class.java.classLoader
                )
                    .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
                declaredMethod.isAccessible = true
                val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
                if (invoke is String) {
                    processName = invoke
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return processName
        }

    /**
     * 通过ActivityManager 获取进程名，需要IPC通信
     * 1。ActivityManager.getRunningAppProcesses() 方法需要跨进程通信，效率不高。
     * 需要 和 系统进程的 ActivityManagerService 通信。必然会导致该方法调用耗时。
     * 2。拿到RunningAppProcessInfo的列表之后，还需要遍历一遍找到与当前进程的信息。
     * 3。ActivityManager.getRunningAppProcesses() 有可能调用失败，返回null，也可能 AIDL 调用失败。调用失败是极低的概率。
     */
    fun getCurrentProcessNameByActivityManager(context: Context): String? {
        if (context == null) {
            return null
        }
        //指的是Process的id。每个进程都有一个独立的id，可以通过pid来区分不同的进程。
        val pid = Process.myPid()
        val am = context.applicationContext
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (am != null) {
            //获取当前正在运行的进程
            val runningAppList = am.runningAppProcesses
            if (runningAppList != null) {
                for (processInfo in runningAppList) {
                    //相应的RunningServiceInfo的pid
                    if (processInfo.pid == pid) {
                        return processInfo.processName
                    }
                }
            }
        }
        return null
    }

    /**
     * 获取前台线程包名
     *
     * 当不是查看当前 App，且 SDK 大于 21 时，
     * 需添加权限
     * `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
     *
     * @return 前台应用包名
     */
    fun getForegroundProcessName(context: Context): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pInfo = am.runningAppProcesses
        if (pInfo != null && pInfo.size > 0) {
            for (aInfo in pInfo) {
                if (aInfo.importance
                    == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                ) {
                    return aInfo.processName
                }
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val pm = context.packageManager
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            val list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            Log.i("ProcessUtils", list.toString())
            if (list.size <= 0) {
                Log.i(
                    "ProcessUtils",
                    "getForegroundProcessName: noun of access to usage information."
                )
                return ""
            }
            try { // Access to usage information.
                val info = pm.getApplicationInfo(context.packageName, 0)
                val aom = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                if (aom.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        info.uid,
                        info.packageName
                    ) != AppOpsManager.MODE_ALLOWED
                ) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                if (aom.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        info.uid,
                        info.packageName
                    ) != AppOpsManager.MODE_ALLOWED
                ) {
                    Log.i(
                        "ProcessUtils",
                        "getForegroundProcessName: refuse to device usage stats."
                    )
                    return ""
                }
                val usageStatsManager = context
                    .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                var usageStatsList: List<UsageStats>? = null
                if (usageStatsManager != null) {
                    val endTime = System.currentTimeMillis()
                    val beginTime = endTime - 86400000 * 7
                    usageStatsList = usageStatsManager
                        .queryUsageStats(
                            UsageStatsManager.INTERVAL_BEST,
                            beginTime, endTime
                        )
                }
                if (usageStatsList == null || usageStatsList.isEmpty()) return ""
                var recentStats: UsageStats? = null
                for (usageStats in usageStatsList) {
                    if (recentStats == null
                        || usageStats.lastTimeUsed > recentStats.lastTimeUsed
                    ) {
                        recentStats = usageStats
                    }
                }
                return recentStats?.packageName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        return ""
    }

    /**
     * 需添加权限
     * `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
     * @return 后台服务进程
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun getAllBackgroundProcesses(context: Context): Set<String> {
//        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val info = am.runningAppProcesses
        val set: Set<String> = HashSet()
//        if (info != null) {
//            for (aInfo in info) {
//                Collections.addAll(set, *aInfo.pkgList)
//            }
//        }
        return set
    }

    /**
     * 杀死所有的后台服务进程
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
     *
     * @return 被暂时杀死的服务集合
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    fun killAllBackgroundProcesses(context: Context): Set<String> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var info = am.runningAppProcesses
        val set: MutableSet<String> = HashSet()
        if (info == null) return set
        for (aInfo in info) {
            for (pkg in aInfo.pkgList) {
                am.killBackgroundProcesses(pkg)
                set.add(pkg)
            }
        }
        info = am.runningAppProcesses
        for (aInfo in info) {
            for (pkg in aInfo.pkgList) {
                set.remove(pkg)
            }
        }
        return set
    }

    /**
     * 杀死后台服务进程
     *
     * 需添加权限
     * `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
     *
     * @param packageName The name of the package.
     * @return `true`: 杀死成功<br></br>`false`: 杀死失败
     */
    @SuppressLint("MissingPermission")
    fun killBackgroundProcesses(context: Context, packageName: String): Boolean {
        val am = context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager ?: return false
        var info = am.runningAppProcesses
        if (info == null || info.size == 0) return true
        for (aInfo in info) {
            if (Arrays.asList(*aInfo.pkgList).contains(packageName)) {
                am.killBackgroundProcesses(packageName)
            }
        }
        info = am.runningAppProcesses
        if (info == null || info.size == 0) return true
        for (aInfo in info) {
            if (Arrays.asList(*aInfo.pkgList).contains(packageName)) {
                return false
            }
        }
        return true
    }

    /**
     * Return whether app running in the main process.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isMainProcess(context: Context): Boolean {
        return context.packageName == getCurrentProcessName(context)
    }

    fun isRunningInForeground(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 21) LollipopRunningProcessCompat().isRunningInForeground(context)
        else RunningProcessCompat().isRunningInForeground(context)
    }

    private class LollipopRunningProcessCompat : RunningProcessCompat() {
        override fun isRunningInForeground(context: Context): Boolean {
            try {
                val processStateField = RunningAppProcessInfo::class.java.getDeclaredField("processState")
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val processInfos = am.runningAppProcesses
                if (null == processInfos || processInfos.isEmpty()) {
                    return false
                }
                val packageName = context.packageName
                val var5: Iterator<*> = processInfos.iterator()
                while (var5.hasNext()) {
                    val rapi = var5.next() as RunningAppProcessInfo
                    if (rapi.importance == 100 && rapi.importanceReasonCode == 0) {
                        try {
                            val processState = processStateField.getInt(rapi)
                            if (processState != null && processState == 2 && rapi.pkgList != null && rapi.pkgList.size > 0) {
                                return rapi.pkgList[0] == packageName
                            }
                        } catch (var8: Exception) {
                        }
                    }
                }
            } catch (var9: Exception) {
            }
            return false
        }
    }

    private open class RunningProcessCompat {
        open fun isRunningInForeground(context: Context): Boolean {
            return try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = am.getRunningTasks(1)
                if (null != tasks && !tasks.isEmpty()) (tasks[0] as RunningTaskInfo).topActivity!!.packageName == context.packageName else false
            } catch (var3: Exception) {
                false
            }
        }
    }
}