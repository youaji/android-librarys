package com.youaji.libs.debug.widget._float.interfaces;

import android.view.View;

/**
 * 设置浮窗内容的接口，由于kotlin暂不支持SAM，所以使用Java接口
 * @author youaji
 * @since 2024/01/05
 */
public interface OnInvokeView {

    /**
     * 设置浮窗布局的具体内容
     *
     * @param view 浮窗布局
     */
    void invoke(View view);
}
