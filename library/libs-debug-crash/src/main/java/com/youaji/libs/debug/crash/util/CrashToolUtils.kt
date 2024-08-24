package com.youaji.libs.debug.crash.util

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import com.youaji.libs.debug.crash.CrashActivity
import com.youaji.libs.debug.crash.test.CrashTestActivity
import com.youaji.libs.debug.crash.KillSelfService
import com.youaji.libs.util.finishAllActivities
import com.youaji.libs.util.topActivity

/**
 * 工具类
 * @author youaji
 * @since 2024/01/05
 */
object CrashToolUtils {
    /**
     * 后期需求，添加崩溃重启后，恢复activity任务栈操作和数据
     * 1.如何保存任务栈
     * 2.activity重启后数据恢复[自动恢复Activity Stack和数据]
     * 3.崩溃信息的保存显示，以及是否添加过期清除
     * 4.开闭原则，支持拓展性，后期上报数据到自己服务器【待定】
     * 5.是清空缓存处理还是重启app
     */
    /**
     * 退出app操作
     */
    private fun exitApp() {
        finishActivity()
        killCurrentProcess(true)
    }

    /**
     * 杀死进程操作，默认为异常退出
     * System.exit(0)是正常退出程序，而System.exit(1)或者说非0表示非正常退出程序
     * System.exit(1)一般放在catch块中，当捕获到异常，需要停止程序。这个status=1是用来表示这个程序是非正常退出。
     *
     * 为何要杀死进程：如果不主动退出进程，重启后会一直黑屏，所以加入主动杀掉进程
     * @param isThrow                           是否是异常退出
     */
    fun killCurrentProcess(isThrow: Boolean) {
        //需要杀掉原进程，否则崩溃的app处于黑屏,卡死状态
        Process.killProcess(Process.myPid())
        if (isThrow) {
            System.exit(10)
        } else {
            System.exit(0)
        }
    }

    private fun finishActivity() {
        val activity: Activity = topActivity
        if (!activity.isFinishing) {
            //可将activity 退到后台，注意不是finish()退出。
            //判断Activity是否是task根
            if (activity.isTaskRoot) {
                //参数为false——代表只有当前activity是task根，指应用启动的第一个activity时，才有效;
                activity.moveTaskToBack(false)
            } else {
                //参数为true——则忽略这个限制，任何activity都可以有效。
                //使用此方法，便不会执行Activity的onDestroy()方法
                activity.moveTaskToBack(true)
            }
            //使用moveTaskToBack是为了让app退出时，不闪屏，退出柔和一些
        }
    }

    private fun finishAllActivity() {
        finishAllActivities()
    }

    /**
     * 开启一个新的服务，用来重启本APP【使用handler延迟】
     * 软件重启，不清临时数据。
     * 重启整个APP
     * @param context                       上下文
     * @param Delayed                       延迟多少毫秒
     */
    fun reStartApp1(context: Context, Delayed: Long) {
        //finishActivity();
        val intent = Intent(context, KillSelfService::class.java)
        intent.putExtra("PackageName", context.packageName)
        intent.putExtra("Delayed", Delayed)
        context.startService(intent)
        //        ToolLogUtils.w(CrashHandler.TAG, "reStartApp--- 用来重启本APP--1---");
        //exitApp();
        killCurrentProcess(true)
    }

    /**
     * 用来重启本APP[使用闹钟，整体重启，临时数据清空（推荐）]
     * 重启整个APP
     * @param context                       上下文
     * @param Delayed                       延迟多少毫秒
     */
    fun reStartApp2(context: Context, Delayed: Long, clazz: Class<*>?) {
        //finishActivity();
        //Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        val intent = Intent(context.applicationContext, clazz)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        /*intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);*/if (intent.component != null) {
            //如果类名已经设置，我们强制它模拟启动器启动。
            //如果我们不这样做，如果你从错误活动重启，然后按home，
            //然后从启动器启动活动，主活动将在backstack上出现两次。
            //这很可能不会有任何有害的影响，因为如果你设置了Intent组件，
            //if将始终启动，而不考虑此处指定的操作。
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
        }
        //为何用PendingIntent，不能Intent
        val restartIntent = PendingIntent.getActivity(
            context.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT
        )
        //退出程序
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + Delayed] = restartIntent
        //        ToolLogUtils.w(CrashHandler.TAG, "reStartApp--- 用来重启本APP--2---"+clazz);
        //exitApp();
        killCurrentProcess(true)
    }

    fun reStartApp3(context: Context) {
        val packageName = context.packageName
        val activity: Activity = topActivity
        val clazz = guessRestartActivityClass(activity)
        //        ToolLogUtils.w(CrashHandler.TAG, "reStartApp--- 用来重启本APP--3-"+packageName + "--"+clazz);
        val intent = Intent(activity, clazz)
        restartApplicationWithIntent(activity, intent)
    }

    fun reStartApp4(context: Context) {
        relaunchApp(context, false)
    }

    /**
     * 通过包名打开app
     * @param packageName                           包名
     */
    fun reStartApp5(context: Context?, packageName: String?) {
        if (packageName == null || packageName.length == 0) {
            return
        }
        if (context == null) {
            return
        }
        val launchAppIntent = getLaunchAppIntent(context, packageName)
        if (launchAppIntent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.")
            return
        }
        context.startActivity(launchAppIntent)
    }

    private fun restartApplicationWithIntent(activity: Activity, intent: Intent) {
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        )
        if (intent.component != null) {
            //如果类名已经设置，我们强制它模拟启动器启动。
            //如果我们不这样做，如果你从错误活动重启，然后按home，
            //然后从启动器启动活动，主活动将在backstack上出现两次。
            //这很可能不会有任何有害的影响，因为如果你设置了Intent组件，
            //if将始终启动，而不考虑此处指定的操作。
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
        }
        activity.startActivity(intent)
        activity.finish()
        killCurrentProcess(true)
    }

    private fun guessRestartActivityClass(context: Context): Class<out Activity>? {
        var resolvedActivityClass: Class<out Activity>?
        resolvedActivityClass = getRestartActivityClassWithIntentFilter(context)
        if (resolvedActivityClass == null) {
            resolvedActivityClass = getLauncherActivity(context)
        }
        return resolvedActivityClass
    }

    private fun getRestartActivityClassWithIntentFilter(context: Context): Class<out Activity>? {
        val searchedIntent = Intent().setPackage(context.packageName)
        //检索可以为给定意图执行的所有活动
        val resolveInfo = context.packageManager.queryIntentActivities(
            searchedIntent,
            PackageManager.GET_RESOLVED_FILTER
        )
        if (resolveInfo.size > 0) {
            val info = resolveInfo[0]
            try {
                return Class.forName(info.activityInfo.name) as Class<out Activity>
            } catch (e: ClassNotFoundException) {
//                ToolLogUtils.e(CrashHandler.TAG+e.getMessage());
            }
        }
        return null
    }

    private fun getLauncherActivity(context: Context): Class<out Activity>? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null && intent.component != null) {
            try {
                return Class.forName(intent.component!!.className) as Class<out Activity>
            } catch (e: ClassNotFoundException) {
//                ToolLogUtils.e(CrashHandler.TAG+e.getLocalizedMessage());
            }
        }
        return null
    }

    /**
     * Relaunch the application.
     *
     * @param context
     * @param isKillProcess True to kill the process, false otherwise.
     */
    fun relaunchApp(context: Context, isKillProcess: Boolean) {
        val intent = getLaunchAppIntent(context, context.applicationContext.packageName, true)
        if (intent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.")
            return
        }
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        context.startActivity(intent)
        if (!isKillProcess) return
        Process.killProcess(Process.myPid())
        System.exit(0)
    }

    private fun getLaunchAppIntent(context: Context, packageName: String): Intent? {
        return getLaunchAppIntent(context, packageName, false)
    }

    private fun getLaunchAppIntent(context: Context, packageName: String, isNewTask: Boolean): Intent? {
        val launcherActivity = getLauncherActivity(context, packageName)
        if (!launcherActivity.isEmpty()) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val cn = ComponentName(packageName, launcherActivity)
            intent.component = cn
            return if (isNewTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) else intent
        }
        return null
    }

    private fun getLauncherActivity(context: Context, pkg: String): String {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setPackage(pkg)
        val pm = context.applicationContext.packageManager
        val info = pm.queryIntentActivities(intent, 0)
        val size = info.size
        if (size == 0) return ""
        for (i in 0 until size) {
            val ri = info[i]
            if (ri.activityInfo.processName == pkg) {
                return ri.activityInfo.name
            }
        }
        return info[0].activityInfo.name
    }
}