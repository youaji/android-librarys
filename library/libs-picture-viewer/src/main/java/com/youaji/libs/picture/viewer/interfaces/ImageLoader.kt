package com.youaji.libs.picture.viewer.interfaces

import android.widget.ImageView

/**
 * load image
 */
fun interface ImageLoader {
    /**
     * 加载图片
     *
     * @param position  图片位置
     * @param source    图片数据
     * @param imageView 展示图片的控件
     */
    fun onLoadImage(position: Int, source: Any?, imageView: ImageView)
}
