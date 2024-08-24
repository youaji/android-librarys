package com.youaji.libs.picture.viewer.interfaces

import android.widget.ImageView

/**
 * 图片被长按监听
 */
fun interface OnImageLongClickListener {
    /**
     * 长按，可添加自定义处理选项，比如保存图片、分享等
     *
     * @param position  被点击图片位置
     * @param imageView 展示被点击图片的ImageView
     */
    fun onLongClick(position: Int, imageView: ImageView): Boolean
}
