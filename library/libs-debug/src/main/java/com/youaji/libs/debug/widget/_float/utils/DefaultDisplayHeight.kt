package com.youaji.libs.debug.widget._float.utils

import android.content.Context
import com.youaji.libs.debug.widget._float.interfaces.OnDisplayHeight

/**
 * 获取屏幕有效高度的实现类
 * @author youaji
 * @since 2024/01/05
 */
internal class DefaultDisplayHeight : OnDisplayHeight {

    override fun getDisplayRealHeight(context: Context) = DisplayUtils.rejectedNavHeight(context)

}