@file:Suppress("unused")
package com.youaji.libs.ui.basic

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar

import com.youaji.libs.ui.R
import com.youaji.libs.ui.binding.ActivityBinding
import com.youaji.libs.ui.binding.ActivityBindingDelegate
import com.youaji.libs.ui.state.Decorative
import com.youaji.libs.ui.state.LoadingState
import com.youaji.libs.ui.state.LoadingStateDelegate
import com.youaji.libs.ui.state.OnReloadListener

/**
 * @author youaji
 * @since 2022/9/13
 */
abstract class BasicBindingActivity<VB : ViewBinding> :
    AppCompatActivity(),
    LoadingState by LoadingStateDelegate(),
    OnReloadListener,
    Decorative,
    ActivityBinding<VB> by ActivityBindingDelegate() {

    protected val statusBarHeight by lazy { ImmersionBar.getStatusBarHeight(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetContentView()
//        defaultStatusBar()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        this.currentFocus?.let {
            // 点击空白位置 隐藏软键盘
            val mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            return mInputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
        return super.onTouchEvent(event)
    }

    /** 初始化或重置 contentView 的加载（常见于横竖屏切换时调用） */
    protected fun resetContentView() {
        setContentViewWithBinding()
        binding.root.decorate(this, this)
    }

    protected fun defaultStatusBar(fits: Boolean = true) {
        setStatusBarColor(R.color.libs_ui_white, fits)
    }

    protected fun transparentStatusBar() {
        immersionBar {
            fitsSystemWindows(false)
            statusBarColor(R.color.libs_ui_transparent)
            transparentStatusBar()
            autoDarkModeEnable(true)
        }
    }

    protected fun setStatusBarColor(@ColorRes statusBarColor: Int, fits: Boolean = true) {
        immersionBar {
            fitsSystemWindows(fits)
            statusBarColor(statusBarColor)
            autoDarkModeEnable(true)
        }
    }

    protected fun statusBarColorTransform(view: View, isDarkFont: Boolean = true) {
        immersionBar {
            titleBar(view)
            statusBarDarkFont(isDarkFont)
        }
    }

    protected fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }


}