package com.youaji.libs.debug.pgyer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.pgyer.pgyersdk.PgyerSDKManager
import com.pgyer.pgyersdk.callback.CheckoutCallBack
import com.pgyer.pgyersdk.model.CheckSoftModel
import com.youaji.libs.debug.R
import com.youaji.libs.debug.util.alertDialog
import com.youaji.libs.util.appVersionName
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.logger.logError
import com.youaji.libs.util.logger.logWarn
import java.io.File
import kotlin.math.max

object Version {

    fun checkVersionUpdate(activity: Activity, appKey: String = "", callback: VersionCallback? = null) {
        logWarn(
            "=== 开始版本更新检查 ===\n" +
                    "--- 版本使用蒲公英管理，特此说明 ---\n" +
                    "1、蒲公英SDK已不再维护，当前使用V4.3.3还能使用，后续情况未知！\n" +
                    "2、为兼容上述，适配了API形式进行版本更新！\n" +
                    "--- 区别说明 ---\n" +
                    "1、当[apiKey]为空时，使用SDK形式检查，会自动检验是否有新版本\n" +
                    "2、当[apiKey]不为空时，使用API形式检查，需要自行校验是否有新版本\n" +
                    "   因API未返回versionCode值，因此目前使用versionName值校验\n" +
                    "   ！！！发包时尤其注意！！！"
        )
        if (appKey.isNotEmpty())
            VersionChecker().check(appKey,
                callback = object : VersionChecker.Callback {
                    override fun result(updateInfo: VersionChecker.UpdateInfo) {
                        activity.runOnUiThread {
                            val versionInfo = VersionInfo(
                                updateInfo.forceUpdateVersion,
                                updateInfo.forceUpdateVersionNo,
                                updateInfo.buildVersion,
                                updateInfo.buildVersionNo.toInt(),
                                updateInfo.buildUpdateDescription,
                                updateInfo.downloadURL,
                                updateInfo.buildShortcutUrl,
                                updateInfo.needForceUpdate,
                                updateInfo.buildHaveNewVersion
                            )
                            logDebug(
                                "=== 版本更新检查 ===\n" +
                                        "使用API形式检查成功\n" +
                                        "当前版本：$appVersionName\n" +
                                        "最新版本：${versionInfo.newVersionName}"
                            )
                            if (compareVersion(versionInfo.newVersionName, appVersionName) > 0) {
                                newVersionDialog(activity, versionInfo)
                            }
                            callback?.onSuccess(versionInfo)
                        }
                    }

                    override fun error(message: String) {
                        logError(
                            "=== 版本更新检查 ===\n" +
                                    "使用API形式检查错误\n" +
                                    "当前版本：$appVersionName\n" +
                                    "错误信息：$message"
                        )
                        activity.runOnUiThread { callback?.onFailed(message) }
                    }

                })
        else
            PgyerSDKManager.checkVersionUpdate(object : CheckoutCallBack {
                override fun onNewVersionExist(model: CheckSoftModel?) {
                    logDebug(
                        "=== 版本更新检查 ===\n" +
                                "使用SDK形式检查成功\n" +
                                "当前版本：$appVersionName\n" +
                                "版本信息：$model"
                    )
                    activity.runOnUiThread {
                        model?.let {
                            val versionInfo = VersionInfo(
                                it.forceUpdateVersion,
                                it.forceUpdateVersionNo,
                                it.buildVersion,
                                it.buildVersionNo.toInt(),
                                it.buildUpdateDescription,
                                it.downloadURL,
                                it.buildShortcutUrl,
                                it.isNeedForceUpdate,
                                it.isBuildHaveNewVersion
                            )
                            newVersionDialog(activity, versionInfo)
                            callback?.onSuccess(versionInfo)
                        }
                    }
                }

                override fun onNonentityVersionExist(error: String?) {
                    activity.runOnUiThread { callback?.onNothing(error ?: "") }
                }

                override fun onFail(error: String?) {
                    logError(
                        "=== 版本更新检查 ===\n" +
                                "使用SDK形式检查错误\n" +
                                "当前版本：$appVersionName\n" +
                                "错误信息：$error"
                    )
                    activity.runOnUiThread { callback?.onFailed(error ?: "") }
                }
            })
    }

    /**
     * 对比版本号大小
     * version1 > version2 return 1
     * version1 < version2 return -1
     * other               return 0
     * @param version1
     * @param version2
     */
    private fun compareVersion(version1: String, version2: String): Int {
        val arr1 = version1.split(".")
        val arr2 = version2.split(".")
        val maxSize = max(arr1.size, arr2.size)
        for (i in 0 until maxSize) {
            val a1 = if (i < arr1.size) arr1[i] else "0"
            val a2 = if (i < arr2.size) arr2[i] else "0"
            if (a1.toDouble() > a2.toDouble()) {
                return 1
            } else if (a1.toDouble() < a2.toDouble()) {
                return -1
            }
        }
        return 0
    }

    private fun newVersionDialog(activity: Activity, versionInfo: VersionInfo) {
        activity.alertDialog {
            title = "新版本[注意：此为debug功能]"
            message = versionInfo.newVersionDesc
            isCancelable = false
            if (!versionInfo.isForce) {
                negativeButton("取消") { it.dismiss() }
            }
            neutralPressed("下载地址") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(versionInfo.shortcutUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
            positiveButton("立即更新") { downloadDialog(activity, versionInfo) }
        }.show()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    private fun downloadDialog(activity: Activity, versionInfo: VersionInfo) {
        val textMessage = "安装包下载中 "
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.libs_debug_dialog_download_progress, null)
        val messageView = dialogView.findViewById<AppCompatTextView>(R.id.message)
        val progressView = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
        messageView.text = textMessage

        val dialog = activity.alertDialog {
            customView = dialogView
            isCancelable = false
        }.show()

        FileDownloader.setup(activity)
        FileDownloader.getImpl()
            .create(versionInfo.downloadUrl)
            .setPath(
                activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath,
                true
            )// 参数2：路径是一个目录
            .setForceReDownload(true)// 强制重新下载，将会忽略检测文件是否健在
            .setListener(object : FileDownloadListener() {
                // 等待
                override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {}

                // 下载中
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    if (totalBytes > 0) {
                        progressView.max = totalBytes
                        progressView.progress = soFarBytes
                        val progress = soFarBytes.toDouble() / totalBytes.toDouble() * 100
                        messageView.text = textMessage + "  ${task?.speed ?: 0}KB/s  " + "${progress.toInt()}%"
                    } else {
                        progressView.isIndeterminate = true
                    }
                }

                // 下载完成
                override fun completed(task: BaseDownloadTask?) {
                    messageView.text = textMessage + "  ${task?.speed ?: 0}KB/s  " + "100%"
                    task?.let {
                        progressView.progress = task.smallFileTotalBytes
                        if (it.status == FileDownloadStatus.completed) {
                            installApk(activity, File(it.path + File.separator + it.filename))
                        }
                    }
                    dialog.dismiss()
                }

                // 下载暂停
                override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {}

                // 下载错误
                override fun error(task: BaseDownloadTask?, e: Throwable?) {
                    Toast.makeText(activity, e?.message ?: "", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                // 下载警告
                override fun warn(task: BaseDownloadTask?) {}
            })
            .start()
    }

    private fun installApk(activity: Activity, file: File) {
        val install = Intent(Intent.ACTION_VIEW)
        install.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            install.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            FileProvider.getUriForFile(activity, "${activity.application.packageName}.fileProvider", file)
        } else {
            Uri.fromFile(file)
        }
        install.setDataAndType(data, "application/vnd.android.package-archive")
        activity.startActivity(install)
    }

    data class VersionInfo(
        val forceVersionName: String,// 强制更新版本名称，String（1.0）
        val forceVersionNo: String,  // 强制更新版本编号，Int（1）
        val newVersionName: String,  // 新版本名称，String（1.0）
        val newVersionNo: Int,       // 新版本编号，Int（1）蒲公英的自增号
        val newVersionDesc: String,  // 新版本说明
        val downloadUrl: String,     // 新版本安装包下载地址
        val shortcutUrl: String,     // 新版本下载页面地址
        var isForce: Boolean,        // 是否强制更新
        var hasNew: Boolean,         // 是否有新版本
    )

    interface VersionCallback {
        fun onSuccess(info: VersionInfo)
        fun onNothing(info: String)
        fun onFailed(error: String)
    }
}

