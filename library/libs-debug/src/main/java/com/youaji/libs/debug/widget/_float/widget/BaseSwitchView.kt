package com.youaji.libs.debug.widget._float.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.youaji.libs.debug.widget._float.interfaces.OnTouchRangeListener

/**
 * @author youaji
 * @since 2024/01/05
 */
abstract class BaseSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    abstract fun setTouchRangeListener(event: MotionEvent, listener: OnTouchRangeListener? = null)

}