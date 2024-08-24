package com.youaji.libs.picture.viewer

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.youaji.libs.picture.viewer.interfaces.IFindThumbnailView
import com.youaji.libs.picture.viewer.interfaces.ImageLoader
import com.youaji.libs.picture.viewer.interfaces.OnDismissListener
import com.youaji.libs.picture.viewer.interfaces.OnLongClickListener
import java.lang.ref.WeakReference
import java.util.Objects

/**
 * 图片预览，支持预览单张，多张图片。
 * 每个 Activity 持有同一个预览对象，
 * 因此[PhotoPreview.constructor(FragmentActivity)]、[PictureViewer.with] 对于同一个 activity 操作的是同一个对象
 */
class PictureViewer {


    private val mFragmentActivity: FragmentActivity?
    private val mFragment: Fragment?
    private val mConfig: Config

    constructor(builder: Builder) {
        Objects.requireNonNull(builder)
        mFragmentActivity = builder.activity
        mFragment = builder.fragment
        mConfig = builder.mConfig
    }

    /**
     * @param activity 当前图片预览所处Activity
     */
    constructor(activity: FragmentActivity) {
        Objects.requireNonNull(activity)
        mFragmentActivity = activity
        mFragment = null
        mConfig = Config()
    }

    /**
     * @param fragment 当前图片预览所处fragment
     */
    constructor(fragment: Fragment) {
        Objects.requireNonNull(fragment)
        mFragmentActivity = null
        mFragment = fragment
        mConfig = Config()
    }

    /**
     * 应用其它配置
     */
    fun setConfig(config: Config?) {
        mConfig.apply(config)
    }

    /**
     * 设置图片加载器
     */
    fun setImageLoader(imageLoader: ImageLoader?) {
        mConfig.imageLoader = imageLoader
    }

    /**
     * 设置图片长按监听
     */
    fun setLongClickListener(listener: OnLongClickListener?) {
        mConfig.onLongClickListener = listener
    }

    /**
     * 设置预览关闭监听
     */
    fun setOnDismissListener(listener: OnDismissListener?) {
        mConfig.onDismissListener = listener
    }

    /**
     * 设置图片数量指示器样式，默认 [IndicatorType.DOT],如果图片数量超过9，则不论设置何种模式，均为 [IndicatorType.TEXT]
     */
    fun setIndicatorType(@IndicatorType indicatorType: Int) {
        mConfig.indicatorType = indicatorType
    }

    /**
     * 多图预览时，当前预览的图片指示器颜色
     */
    fun setSelectIndicatorColor(@ColorInt color: Int) {
        mConfig.selectIndicatorColor = color
    }

    /**
     * 多图预览时，非当前预览的图片指示器颜色
     */
    fun setNormalIndicatorColor(@ColorInt color: Int) {
        mConfig.normalIndicatorColor = color
    }

    /**
     * 在调用 [ImageLoader.onLoadImage] 时延迟展示 loading 框的时间，
     * < 0:不展示，
     * =0:立即显示，
     * >0:延迟给定时间显示，
     * 默认延迟 100ms 显示，如果在此时间内加载完成则不显示，否则显示
     */
    fun setDelayShowProgressTime(delay: Long) {
        mConfig.delayShowProgressTime = delay
    }

    /**
     * 设置图片加载框的颜色，API >= 21 配置才生效
     */
    fun setProgressColor(@ColorInt progressColor: Int) {
        mConfig.progressColor = progressColor
    }

    /**
     * 设置图片加载框Drawable
     */
    fun setProgressDrawable(progressDrawable: Drawable?) {
        mConfig.progressDrawable = progressDrawable
    }

    /**
     * 是否全屏预览，如果全屏预览，在某些手机上(特别是异形屏)可能会出全屏非全屏切换顿挫
     *
     * @param fullScreen
     * null:跟随打开预览的 Activity 是否全屏决定预览界面是否全屏
     * true:全屏预览
     * null:非全屏预览
     *
     */
    fun setFullScreen(fullScreen: Boolean?) {
        mConfig.fullScreen = fullScreen
    }

    /**
     * 设置打开预览界面默认展示位置
     */
    fun setDefaultShowPosition(position: Int) {
        mConfig.defaultShowPosition = position
    }

    /**
     * 设置图片地址
     */
    fun setSource(vararg sources: Any) {
        Objects.requireNonNull<Array<Any>>(arrayOf(sources))
        setSource(listOf(*sources))
    }

    /**
     * 设置图片地址
     */
    fun setSource(sources: List<*>) {
        Objects.requireNonNull(sources)
        mConfig.sources = sources
    }

    /**
     * 设置动画执行时间
     *
     * @param duration
     * null: 使用默认动画时间
     * <=0: 不执行动画
     *
     */
    fun setAnimDuration(duration: Long?) {
        mConfig.animDuration = duration
    }

    /**
     * 当 [setIndicatorType] 为 [IndicatorType.DOT] 时，设置 DOT 最大数量，
     * 如果 [setSource] 或 [setSource] 超出最大值，则采用 [IndicatorType.TEXT]
     */
    fun setMaxIndicatorDot(maxSize: Int) {
        mConfig.maxIndicatorDot = maxSize
    }

    /**
     * 设置缩略图图形变换类型，比如缩列图是圆形或圆角矩形
     *
     * @param shapeTransformType 目前仅提供 [ShapeTransformType.CIRCLE] 和 [ShapeTransformType.ROUND_RECT]
     */
    fun setShapeTransformType(@ShapeTransformType shapeTransformType: Int) {
        mConfig.shapeTransformType = shapeTransformType
    }

    /**
     * 仅当 [setShapeTransformType] 设置为 [ShapeTransformType.ROUND_RECT] 时，此值配置缩略图圆角矩形圆角半径
     */
    fun setShapeCornerRadius(radius: Int) {
        mConfig.shapeCornerRadius = radius
    }

    /**
     * 是否展示缩略图蒙层,如果设置为`true`,则预览动画执行时,缩略图不显示，预览更沉浸
     *
     * @param show 是否显示蒙层，默认`true`
     */
    fun setShowThumbnailViewMask(show: Boolean) {
        mConfig.showThumbnailViewMask = show
    }

    /**
     * 是否在打开预览动画执行开始的时候执行状态栏隐藏/显示操作。如果该值设置为`true`，
     * 那么预览动画打开时，由于状态栏退出/进入有动画，可能导致预览动画卡顿(预览动画时间大于状态栏动画时间时发生)。
     *
     * @param doOP 是否执行操作，默认`false`
     */
    fun setOpenAnimStartHideOrShowStatusBar(doOP: Boolean) {
        mConfig.openAnimStartHideOrShowStatusBar = doOP
    }
    // /**
    //  * 是否在关闭预览动画执行开始的时候执行状态栏显示/隐藏操作。如果该值设置为false，
    //  * 那么预览动画结束后，对于非沉浸式界面，由于要显示/隐藏状态栏，此时会有强烈的顿挫感。
    //  * 因此设置为{@code false}时，建议采用沉浸式
    //  *
    //  * @param doOP 是否执行操作，默认{@code true}
    //  */
    // public void setExitAnimStartHideOrShowStatusBar(boolean doOP) {
    //     mConfig.exitAnimStartHideOrShowStatusBar = doOP;
    // }
    /**
     * 多图预览时，左右滑动监听
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        mConfig.onPageChangeListener = listener
    }

    /**
     * 展示预览
     *
     * @param thumbnailView 缩略图 [View]，建议传 [ImageView] 对象，这样过度效果更好。
     * 如果多图预览，请使用 [show]。如果 thumbnailView 是在列表中，且预览过程可能发生 thumbnailView 变更，请使用 [show]。
     * 传`null`不设置缩略图，预览界面打开关闭将只有从中心缩放动画
     */
    @JvmOverloads
    fun show(thumbnailView: View? = null) {
        show(thumbnailView, null)
    }

    /**
     * 展示预览
     *
     * @param findThumbnailView 多图预览时，打开和关闭预览时用于提供缩略图对象，用于过度动画
     */
    fun show(findThumbnailView: IFindThumbnailView?) {
        show(null, findThumbnailView)
    }

    private fun show(thumbnailView: View?, findThumbnailView: IFindThumbnailView?) {
        correctConfig()
        val fragment =
            if (mFragmentActivity != null) getDialog(mFragmentActivity, true)
            else if (mFragment != null) getDialog(mFragment, true)
            else return
        val lifecycle = mFragment?.lifecycle ?: mFragmentActivity?.lifecycle
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.CREATED) == true) {
            val context = mFragmentActivity ?: mFragment?.context ?: return
            val fragmentManager = mFragment?.getChildFragmentManager() ?: mFragmentActivity?.supportFragmentManager ?: return
            if (thumbnailView != null) {
                fragment?.show(context, fragmentManager, mConfig, thumbnailView)
            } else {
                fragment?.show(context, fragmentManager, mConfig, findThumbnailView)
            }
        } else if (lifecycle?.currentState != Lifecycle.State.DESTROYED) {
            lifecycle?.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
                fun onCreate() {
                    lifecycle.removeObserver(this)
                    val context = mFragmentActivity ?: mFragment?.context ?: return
                    val fragmentManager = mFragment?.getChildFragmentManager() ?: mFragmentActivity?.supportFragmentManager ?: return
                    if (thumbnailView != null) {
                        fragment?.show(context, fragmentManager, mConfig, thumbnailView)
                    } else {
                        fragment?.show(context, fragmentManager, mConfig, findThumbnailView)
                    }
                }
            })
        }
    }

    /**
     * 纠正可能的错误配置
     */
    private fun correctConfig() {
        val sourceSize = mConfig.sources?.size ?: 0
        if (sourceSize == 0) {
            mConfig.defaultShowPosition = 0
        } else if (mConfig.defaultShowPosition >= sourceSize) {
            mConfig.defaultShowPosition = sourceSize - 1
        } else if (mConfig.defaultShowPosition < 0) {
            mConfig.defaultShowPosition = 0
        }
        if (mConfig.imageLoader == null) {
            mConfig.imageLoader = globalImageLoader
        }
        if (mConfig.shapeTransformType != null && mConfig.shapeTransformType != ShapeTransformType.CIRCLE && mConfig.shapeTransformType != ShapeTransformType.ROUND_RECT) {
            mConfig.shapeTransformType = null
        }
    }

    /**
     * 关闭预览界面
     *
     * @param callBack 是否需要执行 [OnDismissListener] 回调
     */
    @JvmOverloads
    fun dismiss(callBack: Boolean = true) {
        val fragment =
            if (mFragmentActivity != null) getDialog(mFragmentActivity, false)
            else if (mFragment != null) getDialog(mFragment, false)
            else return
        fragment?.dismiss(callBack)
    }

    class Builder {
        val activity: FragmentActivity?
        val fragment: Fragment?
        var mConfig: Config

        constructor(activity: FragmentActivity) {
            this.activity = activity
            fragment = null
            mConfig = Config()
        }

        constructor(fragment: Fragment) {
            this.fragment = fragment
            activity = null
            mConfig = Config()
        }

        /**
         * 应用其它配置
         */
        fun config(config: Config?): Builder {
            mConfig.apply(config)
            return this
        }

        /**
         * 图片加载器
         */
        fun imageLoader(imageLoader: ImageLoader?): Builder {
            mConfig.imageLoader = imageLoader
            return this
        }

        /**
         * 多图预览时，指示器类型
         *
         * @param indicatorType [IndicatorType.DOT]、[IndicatorType.TEXT]
         */
        fun indicatorType(@IndicatorType indicatorType: Int): Builder {
            mConfig.indicatorType = indicatorType
            return this
        }

        /**
         * 多图预览时，当前预览的图片指示器颜色
         */
        fun selectIndicatorColor(@ColorInt color: Int): Builder {
            mConfig.selectIndicatorColor = color
            return this
        }

        /**
         * 多图预览时，非当前预览的图片指示器颜色
         */
        fun normalIndicatorColor(@ColorInt color: Int): Builder {
            mConfig.normalIndicatorColor = color
            return this
        }

        /**
         * 设置图片加载loading drawable
         */
        fun progressDrawable(progressDrawable: Drawable?): Builder {
            mConfig.progressDrawable = progressDrawable
            return this
        }

        /**
         * 设置图片加载loading颜色，该颜色作用于[.setProgressDrawable]上
         */
        @SuppressLint("ObsoleteSdkInt")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun progressColor(@ColorInt color: Int): Builder {
            mConfig.progressColor = color
            return this
        }

        /**
         * 在调用[ImageLoader.onLoadImage]时延迟展示loading框的时间，
         * < 0:不展示，=0:立即显示，>0:延迟给定时间显示，默认延迟100ms显示，如果在此时间内加载完成则不显示，否则显示
         */
        fun delayShowProgressTime(delay: Long): Builder {
            mConfig.delayShowProgressTime = delay
            return this
        }

        /**
         * 设置预览界面长按点检监听
         */
        fun onLongClickListener(listener: OnLongClickListener?): Builder {
            mConfig.onLongClickListener = listener
            return this
        }

        /**
         * 设置预览关闭监听
         */
        fun onDismissListener(listener: OnDismissListener?): Builder {
            mConfig.onDismissListener = listener
            return this
        }

        /**
         * 是否全屏预览，如果全屏预览，在某些手机上(特别是异形屏)可能会出全屏非全屏切换顿挫
         *
         * @param fullScreen
         *  * null:跟随打开预览的Activity是否全屏决定预览界面是否全屏
         *  * true:全屏预览
         *  * null:非全屏预览
         *
         */
        fun fullScreen(fullScreen: Boolean?): Builder {
            mConfig.fullScreen = fullScreen
            return this
        }

        /**
         * 数据源
         */
        fun sources(vararg sources: Any): Builder {
            Objects.requireNonNull<Array<Any>>(arrayOf(sources))
            return sources(listOf(*sources))
        }

        /**
         * 数据源
         */
        fun sources(sources: List<*>): Builder {
            Objects.requireNonNull(sources)
            mConfig.sources = sources
            return this
        }

        /**
         * 设置打开预览界面初始展示位置
         */
        fun defaultShowPosition(position: Int): Builder {
            mConfig.defaultShowPosition = position
            return this
        }

        /**
         * 设置动画执行时间
         *
         * @param duration
         *  * null: 使用默认动画时间
         *  * <=0: 不执行动画
         *
         */
        fun animDuration(duration: Long?): Builder {
            mConfig.animDuration = duration
            return this
        }

        /**
         * 当[.indicatorType]为[IndicatorType.DOT]时，设置DOT最大数量，
         * 如果[.sources]或[.sources]超出最大值，则采用[IndicatorType.TEXT]
         */
        fun maxIndicatorDot(maxSize: Int): Builder {
            mConfig.maxIndicatorDot = maxSize
            return this
        }

        /**
         * 设置缩略图图形变换类型，比如缩列图是圆形或圆角矩形
         *
         * @param shapeTransformType 目前仅提供[ShapeTransformType.CIRCLE]和[ShapeTransformType.ROUND_RECT]
         */
        fun shapeTransformType(@ShapeTransformType shapeTransformType: Int): Builder {
            mConfig.shapeTransformType = shapeTransformType
            return this
        }

        /**
         * 仅当[.shapeTransformType]设置为[ShapeTransformType.ROUND_RECT]时，此值配置缩略图圆角矩形圆角半径
         */
        fun shapeCornerRadius(radius: Int): Builder {
            mConfig.shapeCornerRadius = radius
            return this
        }

        /**
         * 是否展示缩略图蒙层,如果设置为`true`,则预览动画执行时,缩略图不显示，预览更沉浸
         *
         * @param show 是否显示蒙层，默认`true`
         */
        fun showThumbnailViewMask(show: Boolean): Builder {
            mConfig.showThumbnailViewMask = show
            return this
        }

        /**
         * 是否在打开预览动画执行开始的时候执行状态栏隐藏/显示操作。如果该值设置为true，
         * 那么预览动画打开时，由于状态栏退出/进入有动画，可能导致预览动画卡顿(预览动画时间大于状态栏动画时间时发生)。
         *
         * @param doOP 是否执行操作，默认`false`
         */
        fun openAnimStartHideOrShowStatusBar(doOP: Boolean): Builder {
            mConfig.openAnimStartHideOrShowStatusBar = doOP
            return this
        }
        // /**
        //  * 是否在关闭预览动画执行开始的时候执行状态栏显示/隐藏操作。如果该值设置为false，
        //  * 那么预览动画结束后，对于非沉浸式界面，由于要显示/隐藏状态栏，此时会有强烈的顿挫感。
        //  * 因此设置为{@code false}时，建议采用沉浸式
        //  *
        //  * @param doOP 是否执行操作，默认{@code true}
        //  */
        // public Builder exitAnimStartHideOrShowStatusBar(boolean doOP) {
        //     mConfig.exitAnimStartHideOrShowStatusBar = doOP;
        //     return this;
        // }
        /**
         * 多图预览时，左右滑动监听
         */
        fun onPageChangeListener(listener: OnPageChangeListener?): Builder {
            mConfig.onPageChangeListener = listener
            return this
        }

        fun build(): PictureViewer {
            return PictureViewer(this)
        }

        /**
         * 不设置缩略图，预览界面打开关闭将只有从中心缩放动画
         */
        fun show() {
            build().show()
        }

        /**
         * 展示预览
         *
         * @param thumbnailView 缩略图 [View]，建议传 [ImageView] 对象，这样过度效果更好。
         * 如果多图预览，请使用 [show]。如果thumbnailView
         * 是在列表中，且预览过程可能发生 thumbnailView 变更，请使用 [show]。
         */
        fun show(thumbnailView: View?) {
            build().show(thumbnailView, null)
        }

        /**
         * 展示预览
         *
         * @param findThumbnailView 多图预览时，打开和关闭预览时用于提供缩略图对象，用于过度动画
         */
        fun show(findThumbnailView: IFindThumbnailView?) {
            build().show(null, findThumbnailView)
        }
    }

    companion object {
        /**
         * 全局图片加载器
         */
        private var globalImageLoader: ImageLoader? = null

        /**
         * 图片预览池，一个 Activity 持有一个预览对象
         */
        private val DIALOG_POOL: MutableMap<String, WeakReference<PictureViewerDialogFragment>> = HashMap()

        /**
         * 设置图片全局加载器
         */
        fun setGlobalImageLoader(imageLoader: ImageLoader?) {
            globalImageLoader = imageLoader
        }

        private fun getDialog(activity: FragmentActivity, noneCreate: Boolean): PictureViewerDialogFragment? {
            val fragmentByTag = activity.supportFragmentManager.findFragmentByTag(PictureViewerDialogFragment.FRAGMENT_TAG)
            if (fragmentByTag is PictureViewerDialogFragment) {
                return fragmentByTag
            }
            val name = activity.toString()
            var reference = DIALOG_POOL[name]
            var fragment = reference?.get()
            if (fragment == null) {
                if (noneCreate) {
                    fragment = PictureViewerDialogFragment()
                    reference = WeakReference(fragment)
                    DIALOG_POOL[name] = reference
                    activity.lifecycle.addObserver(object : LifecycleObserver {
                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                        fun onDestroy() {
                            activity.lifecycle.removeObserver(this)
                            DIALOG_POOL.remove(name)
                        }
                    })
                } else {
                    DIALOG_POOL.remove(name)
                }
            }
            return fragment
        }

        private fun getDialog(parentFragment: Fragment, noneCreate: Boolean): PictureViewerDialogFragment? {
            val fragmentByTag = parentFragment.getChildFragmentManager().findFragmentByTag(PictureViewerDialogFragment.FRAGMENT_TAG)
            if (fragmentByTag is PictureViewerDialogFragment) {
                return fragmentByTag
            }
            val name = parentFragment.toString()
            var reference = DIALOG_POOL[name]
            var fragment = reference?.get()
            if (fragment == null) {
                if (noneCreate) {
                    fragment = PictureViewerDialogFragment()
                    reference = WeakReference(fragment)
                    DIALOG_POOL[name] = reference
                    parentFragment.lifecycle.addObserver(object : LifecycleObserver {
                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                        fun onDestroy() {
                            parentFragment.lifecycle.removeObserver(this)
                            DIALOG_POOL.remove(name)
                        }
                    })
                } else {
                    DIALOG_POOL.remove(name)
                }
            }
            return fragment
        }

        /**
         * 创建构建器，链式调用
         */
        fun with(activity: FragmentActivity): Builder {
            Objects.requireNonNull(activity)
            return Builder(activity)
        }

        /**
         * 创建构建器，链式调用
         */
        fun with(fragment: Fragment): Builder {
            Objects.requireNonNull(fragment)
            return Builder(fragment)
        }

        /**
         * 创建构建器，链式调用
         */
        fun with(activityOrFragment: Any): Builder {
            Objects.requireNonNull(activityOrFragment)
            if (activityOrFragment is FragmentActivity) {
                return Builder(activityOrFragment)
            } else if (activityOrFragment is Fragment) {
                return Builder(activityOrFragment)
            }
            throw IllegalArgumentException("activityOrFragment must be FragmentActivity or Fragment")
        }
    }
}
