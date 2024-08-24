package com.youaji.libs.picture.viewer.interfaces

import android.view.View

/**
 * 查找预览图指定下标对应的缩略图控件
 */
fun interface IFindThumbnailView {
    /**
     * 查找指定位置缩略图
     *
     * @param position 预览位置
     * @return 预览图对应的缩略图控件
     */
    fun findView(position: Int): View?
}
