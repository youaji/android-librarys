package com.youaji.libs.debug.crash

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.youaji.libs.debug.crash.util.logW_LDC

/**
 * 重启开启 app
 * @author youaji
 * @since 2024/01/05
 */
class KillSelfService : Service() {

    private var packageName: String? = null
    private var handler: Handler?

    init {
        handler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val stopDelayed = intent.getLongExtra("Delayed", 2000)
        packageName = intent.getStringExtra("PackageName")
        handler?.postDelayed({
            logW_LDC("--- start app --- $packageName")
            packageName?.let {
                val launchIntent = packageManager.getLaunchIntentForPackage(it)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(launchIntent)
                this@KillSelfService.stopSelf()
            }
        }, stopDelayed)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }
}