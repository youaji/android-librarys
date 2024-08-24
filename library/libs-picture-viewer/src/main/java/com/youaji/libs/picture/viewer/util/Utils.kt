package com.youaji.libs.picture.viewer.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager

object Utils {
    fun dp2px(context: Context, dipValue: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue.toFloat(), context.resources.displayMetrics).toInt()
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * 是否是沉浸式状态栏或无状态栏，此种情况都无需处理状态栏导致的偏移值
     */
    @SuppressLint("ObsoleteSdkInt")
    fun isImmersionBar(window: Window): Boolean {
        if (window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return true
        }
        val decorView = window.decorView
        if (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) {
            return true
        } else if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            return decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_STABLE == View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        return false
    }
}
