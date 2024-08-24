package com.youaji.libs.picture.viewer.custom

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.View

internal object Compat {

    private const val SIXTY_FPS_INTERVAL = 1000 / 60

    @JvmStatic
    @SuppressLint("ObsoleteSdkInt")
    fun postOnAnimation(view: View, runnable: Runnable) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            postOnAnimationJellyBean(view, runnable)
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL.toLong())
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private fun postOnAnimationJellyBean(view: View, runnable: Runnable) {
        view.postOnAnimation(runnable)
    }
}
