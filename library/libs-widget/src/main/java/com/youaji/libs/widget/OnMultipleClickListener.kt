@file:Suppress("unused")
package com.youaji.libs.widget

import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * 单击、双击
 * @author youaji
 * @since 2023/4/7
 */
abstract class OnMultipleClickListener(private val isSingle: Boolean = false) : View.OnClickListener {

    private val timeout: Long = 200
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var clickCount = 0

    override fun onClick(v: View) {
        if (isSingle) {
            onSingleClick(v)
            return
        }
        clickCount++
        handler.postDelayed({
            if (clickCount == 1) {
                onSingleClick(v)
            } else if (clickCount == 2) {
                onDoubleClick(v)
            }
            handler.removeCallbacksAndMessages(null)
            clickCount = 0
        }, timeout)
    }

    /**
     * 单击
     * @param v 视图
     */
    abstract fun onSingleClick(v: View)

    /**
     * 双击
     * @param v 视图
     */
    abstract fun onDoubleClick(v: View)
}