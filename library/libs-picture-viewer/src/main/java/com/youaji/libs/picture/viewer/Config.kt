package com.youaji.libs.picture.viewer

import android.graphics.drawable.Drawable
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.youaji.libs.picture.viewer.interfaces.ImageLoader
import com.youaji.libs.picture.viewer.interfaces.OnDismissListener
import com.youaji.libs.picture.viewer.interfaces.OnLongClickListener

/**
 * 预览配置
 *
 * @author Created by wanggaowan on 11/20/20 10:33 PM
 */
class Config {
    @JvmField
    var imageLoader: ImageLoader? = null
    @JvmField
    var indicatorType = IndicatorType.DOT
    @JvmField
    var maxIndicatorDot = 9
    @JvmField
    var selectIndicatorColor = -0x1 /*白色*/
    @JvmField
    var normalIndicatorColor = -0x555556 /*灰色*/
    @JvmField
    var progressDrawable: Drawable? = null /*ProgressBar默认样式*/
    @JvmField
    var progressColor: Int? = null
    @JvmField
    var delayShowProgressTime: Long = 100
    @JvmField
    var onLongClickListener: OnLongClickListener? = null
    @JvmField
    var onDismissListener: OnDismissListener? = null
    @JvmField
    var fullScreen: Boolean? = null /*默认跟随打开预览的界面显示模式*/
    @JvmField
    var sources: List<*>? = null
    @JvmField
    var defaultShowPosition = 0
    @JvmField
    var animDuration: Long? = null /*打开和退出预览时的过度动画时间*/

    /**
     * 图形变换类型，可选值参考[ShapeTransformType]
     */
    @JvmField
    var shapeTransformType: Int? = null

    /**
     * 图形变换设置为[ShapeTransformType.ROUND_RECT]时圆角半径
     */
    @JvmField
    var shapeCornerRadius = 0

    /**
     * 是否展示缩略图蒙层,如果设置为`true`,则预览动画执行时,缩略图不显示，预览更沉浸
     */
    @JvmField
    var showThumbnailViewMask = true

    /**
     * 是否在打开预览动画执行开始的时候执行状态栏隐藏/显示操作。如果该值设置为true，
     * 那么预览动画打开时，由于状态栏退出/进入有动画，可能导致预览动画卡顿(预览动画时间大于状态栏动画时间时发生)。
     */
    @JvmField
    var openAnimStartHideOrShowStatusBar = false

    /**
     * 是否在关闭预览动画执行开始的时候执行状态栏显示/隐藏操作。如果该值设置为false，
     * 那么预览动画结束后，对于非沉浸式界面，由于要显示/隐藏状态栏，此时会有强烈的顿挫感。
     * 因此设置为`false`时，建议采用沉浸式
     */
    @JvmField
    var exitAnimStartHideOrShowStatusBar = true

    /**
     * 图片切换监听
     */
    @JvmField
    var onPageChangeListener: OnPageChangeListener? = null

    fun apply(config: Config?) {
        if (config == null)
            return

        imageLoader = config.imageLoader
        indicatorType = config.indicatorType
        maxIndicatorDot = config.maxIndicatorDot
        selectIndicatorColor = config.selectIndicatorColor
        normalIndicatorColor = config.normalIndicatorColor
        progressDrawable = config.progressDrawable
        progressColor = config.progressColor
        delayShowProgressTime = config.delayShowProgressTime
        onLongClickListener = config.onLongClickListener
        onDismissListener = config.onDismissListener
        fullScreen = config.fullScreen
        sources = config.sources
        defaultShowPosition = config.defaultShowPosition
        animDuration = config.animDuration
        shapeTransformType = config.shapeTransformType
        shapeCornerRadius = config.shapeCornerRadius
        showThumbnailViewMask = config.showThumbnailViewMask
        openAnimStartHideOrShowStatusBar = config.openAnimStartHideOrShowStatusBar
        exitAnimStartHideOrShowStatusBar = config.exitAnimStartHideOrShowStatusBar
        onPageChangeListener = config.onPageChangeListener
    }

    fun release() {
        imageLoader = null
        indicatorType = IndicatorType.DOT
        maxIndicatorDot = 9
        selectIndicatorColor = -0x1
        normalIndicatorColor = -0x555556
        progressDrawable = null
        progressColor = null
        delayShowProgressTime = 100
        onLongClickListener = null
        onDismissListener = null
        fullScreen = null
        sources = null
        defaultShowPosition = 0
        animDuration = null
        shapeTransformType = null
        shapeCornerRadius = 0
        showThumbnailViewMask = true
        openAnimStartHideOrShowStatusBar = false
        exitAnimStartHideOrShowStatusBar = true
        onPageChangeListener = null
    }
}
