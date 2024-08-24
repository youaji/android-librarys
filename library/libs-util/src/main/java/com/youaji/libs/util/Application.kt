@file:Suppress("unused")
package com.youaji.libs.util

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Process
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import com.youaji.libs.util.logger.logError

/**
 * @return 获取 Application
 */
lateinit var application: Application
    internal set

/**
 * @return 获取包名
 */
inline val packageName: String get() = application.packageName

/**
 * @return 获取包信息
 */
inline val packageInfo: PackageInfo
    get() = application.packageManager.getPackageInfo(packageName, 0)

/**
 * @return 获取 App 名字
 */
inline val appName: String
    get() = application.applicationInfo.loadLabel(application.packageManager).toString()

/**
 * @return 获取 App 图标
 */
inline val appIcon: Drawable get() = packageInfo.applicationInfo.loadIcon(application.packageManager)

/**
 * @return 获取 App 版本名称
 */
inline val appVersionName: String get() = packageInfo.versionName

/**
 * @return 获取 App 版本号
 */
inline val appVersionCode: Long get() = PackageInfoCompat.getLongVersionCode(packageInfo)

/**
 * @return 是否是 Debug 版本
 */
inline val isAppDebug: Boolean get() = application.isAppDebug

/**
 * @return 是否为 debug 版本
 */
inline val Application.isAppDebug: Boolean
    get() = packageManager.getApplicationInfo(packageName, 0).flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

/**
 * @return 是否是夜间模式
 */
inline val isAppDarkMode: Boolean
    get() = (application.resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES

/**
 * @param key
 * @return meta data（默认值：空字符串）
 */
fun getMetaData(key: String): String {
    var value = ""
    try {
        if (key.isNotEmpty()) {
            val applicationInfo = application.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            value = applicationInfo.metaData.getString(key, value)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        logError("获取MetaData异常：$e")
    }
    return value
}

/**
 * @return 启动 App 详情设置
 */
fun launchAppSettings(): Boolean = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", packageName, null) }.startForActivity()

/**
 * @return    重启 App
 */
fun relaunchApp(killProcess: Boolean = true) = application.packageManager.getLaunchIntentForPackage(packageName)?.let {
    it.addFlags(FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(it)
    if (killProcess) Process.killProcess(Process.myPid())
}
