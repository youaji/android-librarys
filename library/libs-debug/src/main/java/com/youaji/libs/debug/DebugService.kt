package com.youaji.libs.debug

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.pgyer.pgyersdk.PgyerSDKManager
import com.pgyer.pgyersdk.pgyerenum.Features
import com.youaji.libs.debug.crash.CrashActivity
import com.youaji.libs.debug.crash.CrashHandler
import com.youaji.libs.debug.crash.CrashListener
import com.youaji.libs.debug.crash.util.CrashToolUtils
import com.youaji.libs.debug.databinding.LibsDebugDialogDebugMenuBinding
import com.youaji.libs.debug.file.FileExplorerActivity
import com.youaji.libs.debug.ftp.FTPActivity
import com.youaji.libs.debug.logcat.logcatWindow
import com.youaji.libs.debug.pgyer.Version
import com.youaji.libs.debug.util.alertDialog
import com.youaji.libs.debug.widget._float.FloatDisplayType
import com.youaji.libs.debug.widget._float.FloatWindow
import com.youaji.libs.util.logger.Logger
import com.youaji.libs.util.topActivity
import kotlin.system.exitProcess

/**
 * @author youaji
 * @since 2023/3/3
 */
class DebugService private constructor() {

    companion object {
        val get: DebugService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DebugService()
        }
    }

    fun debugTips(context: Context) {
        context.alertDialog {
            message = "内部测试版本！禁止外发！"
            positiveButton("我已知晓") {}
            show()
        }
    }

    fun toCrashIndex() {
        CrashActivity.start()
    }

    fun toFileExplorerIndex() {
        FileExplorerActivity.start()
    }

    fun toFTPIndex() {
        FTPActivity.start()
    }

    fun setFloatButton(application: Application) =
        FloatWindow.with(application)
            .setTag("debug_menu_window")
            .setShowPattern(FloatDisplayType.OnlyForeground)
            .setLocation(100, 100)
            .setAnimator(null)
            .setLayout(R.layout.libs_debug_layout_float_button) {
                it.setOnClickListener {
                    val binding = LibsDebugDialogDebugMenuBinding.inflate(LayoutInflater.from(topActivity))
                    val dialog = AlertDialog.Builder(topActivity)
                        .setView(binding.root)
                        .create()
                    dialog.show()
                    binding.layoutCrash.setOnClickListener {
                        toCrashIndex()
                        dialog.dismiss()
                    }
                    binding.layoutFile.setOnClickListener {
                        toFileExplorerIndex()
                        dialog.dismiss()
                    }
                    binding.layoutFtp.setOnClickListener {
                        toFTPIndex()
                        dialog.dismiss()
                    }
                    binding.layoutLogcat.setOnClickListener {
                        logcatWindow(application)
                        dialog.dismiss()
                    }
                    binding.checkSaveLog.isChecked = Logger.isSaveLogFile
                    binding.checkSaveLog.setOnCheckedChangeListener { _, isChecked -> Logger.isSaveLogFile = isChecked }
                }
            }
            .show()

    fun initCrash(application: Application, callback: ((isRestart: Boolean, recorded: Boolean) -> Unit)? = null) {
        CrashHandler.get.init(application, object : CrashListener {
            /**
             * 重启app
             */
            override fun againStartApplication() {
                println("崩溃重启----------againStartApp------")
                // CrashToolUtils.reStartApp1(application, 2000)
                // CrashToolUtils.reStartApp2(App.this,2000, MainActivity.class);
                // CrashToolUtils.reStartApp3(App.this);
                callback?.invoke(true, false)
            }

            /**
             * 自定义上传 crash，支持开发者上传自己捕获的 crash 数据
             */
            override fun recordedException(throwable: Throwable) {
                println("崩溃----------recordedException------")
                //自定义上传crash，支持开发者上传自己捕获的crash数据
                // StatService.recordException(getApplication(), ex);
                callback?.invoke(false, true)
            }
        })
    }

    @Deprecated("蒲公英SDK官方不再维护，后续去除！")
    fun initPgyer(
        context: Context,
        jsToken: String,
        apiKey: String
    ) {
        PgyerSDKManager
            .Init()
            .setContext(context.applicationContext)
            .setFrontJSToken(jsToken)
            .setApiKey(apiKey)
            .enable(Features.CHECK_UPDATE) // 开启自动更新检测（默认关闭）
            .start()
    }

    @Deprecated("蒲公英SDK官方不再维护，后续去除！", ReplaceWith(""))
    fun setUserInfo(nickname: String) {
        PgyerSDKManager.setUserData("{\"user\":\"$nickname\"}")
    }

    /**
     * 检查版本更新
     */
    @Deprecated(
        message = "因蒲公英SDK官方不再维护，跟随废弃，后续去除！",
        replaceWith = ReplaceWith("checkVersionUpdate(activity, appKey)")
    )
    fun checkVersionUpdate(activity: Activity) {
        checkVersionUpdate(activity, "")
    }

    /**
     * 检查版本更新
     * @param appKey appKey
     */
    fun checkVersionUpdate(activity: Activity, appKey: String) {
        Version.checkVersionUpdate(activity, appKey)
    }
}