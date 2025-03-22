package com.youaji.libs.debug.crash

import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import com.youaji.libs.util.appName

class CrashTipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this)
            .setTitle("[$appName]遇到异常了！")
            .setMessage("\n                  注意：" +
                    "\n         此为debug功能！" +
                    "\n         正式版无此功能！")
            .setCancelable(false)
            .setNeutralButton("查看崩溃日志") { _, _ ->
                CrashActivity.start()
                finish()
            }
            .setNegativeButton("关闭") { _, _ ->
                Process.killProcess(Process.myPid())
                finish()
            }
            .setPositiveButton("重启") { _, _ ->
                ProcessPhoenix.triggerRebirth(this@CrashTipsActivity)
                finish()
            }
            .create().show()
    }
}