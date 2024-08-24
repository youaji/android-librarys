package com.youaji.libs.picture.viewer

import android.view.View
import android.graphics.drawable.Drawable
import com.youaji.libs.picture.viewer.PictureViewerHelper.OnExitListener
import com.youaji.libs.picture.viewer.PictureViewerHelper.OnOpenListener
import com.youaji.libs.picture.viewer.PreloadImageView.DrawableLoadListener
import com.youaji.libs.picture.viewer.interfaces.IFindThumbnailView
import com.youaji.libs.picture.viewer.interfaces.OnImageLongClickListener

/**
 * 整个预览库都需要共享的数据
 */
class ShareData {

    @JvmField
    val config = Config()

    /**
     * 打开预览时的缩略图
     */
    @JvmField
    var thumbnailView: View? = null

    /**
     * 获取指定位置的缩略图
     */
    @JvmField
    var findThumbnailView: IFindThumbnailView? = null

    /**
     * 图片长按监听
     */
    @JvmField
    var onLongClickListener: OnImageLongClickListener? = null

    /**
     * 预览退出监听
     */
    @JvmField
    var onExitListener: OnExitListener? = null

    /**
     * 预览打开监听
     */
    @JvmField
    var onOpenListener: OnOpenListener? = null

    /**
     * 是否需要执行进入动画
     */
    @JvmField
    var showNeedAnim = false

    /**
     * 预览界面是否第一次创建
     */
    var isFirstCreate = true

    /**
     * 预览动画延迟执行时间
     */
    @JvmField
    var openAnimDelayTime: Long = 0

    /**
     * 预加载图片，加载内容为默认打开数据
     */
    @JvmField
    var preLoadDrawable: Drawable? = null

    /**
     * 预加载图片监听
     */
    @JvmField
    var preDrawableLoadListener: DrawableLoadListener? = null

    fun applyConfig(config: Config?) {
        this.config.apply(config)
    }

    fun release() {
        config.release()
        thumbnailView = null
        findThumbnailView = null
        onLongClickListener = null
        onExitListener = null
        onOpenListener = null
        showNeedAnim = false
        isFirstCreate = true
        openAnimDelayTime = 0
        preLoadDrawable = null
        preDrawableLoadListener = null
    }
}
