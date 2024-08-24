package com.youaji.libs.debug.widget._float.interfaces;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

/**
 * 通过接口获取屏幕的有效显示高度
 * @author youaji
 * @since 2024/01/05
 */
public interface OnDisplayHeight {

    /**
     * 获取屏幕有效的显示高度，不包含虚拟导航栏
     *
     * @param context ApplicationContext
     * @return 高度值（int类型）
     */
    int getDisplayRealHeight(@NotNull Context context);
}
