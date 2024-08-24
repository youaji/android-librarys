package com.youaji.libs.picture.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 捕获触摸异常,主要是 [PictureView] 与 Viewpager 结合使用有 bug，目前作者未修复，给出捕获异常解决方案
 */
class NoTouchExceptionViewPager @JvmOverloads constructor(
    context: Context, attr: AttributeSet? = null
) : ViewPager(context, attr) {

    private var isTouchEnable = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return try {
            if (!isTouchEnable) false
            else super.dispatchTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            if (ev.pointerCount > 1) false
            else super.onTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setTouchEnable(touchEnable: Boolean) {
        isTouchEnable = touchEnable
    }
}
