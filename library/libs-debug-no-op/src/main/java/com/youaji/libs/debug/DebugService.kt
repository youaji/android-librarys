package com.youaji.libs.debug

import android.app.Activity
import android.app.Application
import android.content.Context

/**
 * @author youaji
 * @since 2023/4/1
 */
class DebugService private constructor() {

    companion object {
        val get: DebugService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DebugService()
        }
    }

    fun debugTips(context: Context) {}
    fun toCrashIndex() {}
    fun toFileExplorerIndex() {}
    fun toFTPIndex() {}
    fun setFloatButton(application: Application) {}
    fun initCrash(application: Application, callback: ((isRestart: Boolean, recorded: Boolean) -> Unit)? = null) {}
    fun initPgyer(context: Context, jsToken: String, apiKey: String) {}
    fun setUserInfo(nickname: String) {}
    fun checkVersionUpdate(activity: Activity) {}
    fun checkVersionUpdate(activity: Activity, appKey: String) {}
}