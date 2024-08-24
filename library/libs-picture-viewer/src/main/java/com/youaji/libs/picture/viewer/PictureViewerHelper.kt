package com.youaji.libs.picture.viewer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RestrictTo
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.youaji.libs.picture.viewer.PreloadImageView.DrawableLoadListener
import com.youaji.libs.picture.viewer.util.MatrixUtils
import kotlin.math.min

/**
 * 图片预览辅助类，主要处理预览打开和关闭时过渡动画
 */
@SuppressLint("ObsoleteSdkInt")
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PictureViewerHelper(
    private val mFragment: PictureViewerDialogFragment, // 当前界面显示预览图位置
    private var mPosition: Int
) {

    companion object {
        /*
     此预览库动画采用androidx.transition.Transition库实现，使用此库有以下几个点需要注意
        说明：适配不同缩放类型的逻辑都是基于Glide图片加载库
             srcView 指定缩略图，且是ImageView类型
             helperView 指定实现从缩略图到预览图过度动画辅助类
             photoView 指定预览大图

        1. 如果srcView 的缩放类型为ScaleType.CENTER_CROP，那么helperView 设置的drawable 必须为photoView drawable，
           否则过度动画不能无缝衔接。比如以下情况：

           // srcView并非加载的原图，缩放类型为ScaleType.CENTER_CROP
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(srcView);

           // 此时不管photoView是否加载原图，如果helperView设置为srcView的drawable，那么过度动画不能无缝衔接
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(photoView);

        2. 如果srcView 的缩放类型为非ScaleType.CENTER_CROP，那么helperView 设置的drawable 必须为srcView drawable，
           否则过度动画不能无缝衔接。比如以下情况：

           // srcView缩放类型非ScaleType.CENTER_CROP
           Glide.with(mContext)
            .load(item)
            // 无论是否加载原图
            // .override(Target.SIZE_ORIGINAL)
            .into(srcView);

           // 此时不管photoView是否加载原图，如果helperView设置为photoView的drawable，那么过度动画不能无缝衔接
           Glide.with(mContext)
            .load(item)
            // .override(Target.SIZE_ORIGINAL)
            .into(photoView);

        3. 由于存在srcView实际显示大小并非布局或代码指定的固定大小，因此在helperView外部包裹一层父布局，用于裁剪
     */
        private const val OPEN_AND_EXIT_ANIM_DURATION: Long = 200
        private const val OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE: Long = 350
        private val ARGB_EVALUATOR = ArgbEvaluator()
        private val INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()
        private const val ANIM_START_PRE = 0
        private const val ANIM_START = 1
        private const val ANIM_END = 2
    }

    // 占位图，辅助执行过度动画
    // 为什么不直接使用mPhotoView进行过度动画，主要有一下几个问题
    // 1. 使用mPhotoView，某些情况下预览打开后图片某一部分自动被放大。导致这个的原因可能是使用Glide加载库，设置了placeholder导致
    // 2. 预览动画开始时，需要对图片进行位移，裁减等操作，而预览大图加载时，不能调整其大小和缩放类型，否则预览大图显示将出现问题
    private val mHelperView: ImageView
    private val mHelperViewParent: FrameLayout
    val mRootViewBgMask: FrameLayout
    private val mShareData: ShareData = mFragment.mShareData
    private var mThumbnailView: View? = null
    private var mThumbnailViewVisibility = View.VISIBLE
    private var mThumbnailViewScaleType: ImageView.ScaleType? = null

    // 当前界面是否需要执行动画
    private var mNeedInAnim = false

    // 根据动画时间可决定整个预览是否需要执行动画
    private var mAnimDuration: Long = 0
    private val mIntTemp = IntArray(2)

    // 缩略图设定大小
    private val mSrcViewSize = IntArray(2)

    // 缩略图实际可显示大小
    private val mSrcViewParentSize = IntArray(2)
    private val mSrcImageLocation = IntArray(2)
    private val mFloatTemp = FloatArray(2)

    // 辅助图是否可接收新图片
    private var mHelpViewCanSetImage = true

    /**
     * 预览打开动画执行结束
     */
    // 预览打开动画执行结束
    var isOpenAnimEnd = false
        private set
    private var mOpenListenerList: MutableList<OnOpenListener?>? = null
    private var mExitListenerList: MutableList<OnExitListener?>? = null

    // 上一次缩放倍率是否小于1
    private var oldScaleLessOne = false

    init {
        mFragment.mRootView?.setFocusableInTouchMode(true)
        mFragment.mRootView?.requestFocus()
        mRootViewBgMask = mFragment.mRootView ?: throw NullPointerException("RootView为空！")
        mHelperView = mFragment.mRootView?.findViewById(R.id.iv_anim) ?: throw NullPointerException("HelperView为空！")
        mHelperViewParent = mFragment.mRootView?.findViewById(R.id.fl_parent) ?: throw NullPointerException("HelperViewParent为空！")
        mRootViewBgMask.setBackgroundColor(Color.TRANSPARENT)
        mHelperViewParent.visibility = View.INVISIBLE
        mHelperViewParent.translationX = 0f
        mHelperViewParent.translationY = 0f
        mHelperView.scaleX = 1f
        mHelperView.scaleY = 1f
        mHelperView.setImageDrawable(null)
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mHelperView.setOutlineProvider(null)
        }
        setViewSize(mHelperViewParent, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setViewSize(mHelperView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        initEvent()
        initData()
    }

    private fun initEvent() {
        mFragment.mRootView?.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent? ->
            if (keyCode == KeyEvent.KEYCODE_BACK
                && (event == null || event.action == KeyEvent.ACTION_UP)
                && isOpenAnimEnd
            ) {
                exit()
                return@setOnKeyListener true
            }
            false
        }
        mFragment.mRootView?.setOnClickListener {
            if (!isOpenAnimEnd) return@setOnClickListener
            exit()
        }
    }

    private fun initData() {
        val thumbnailView = getThumbnailView(mShareData)
        if (thumbnailView !== mThumbnailView) {
            mThumbnailView = thumbnailView
            mThumbnailView?.let { mThumbnailViewVisibility = it.visibility }
            mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView, mShareData)
            initThumbnailViewScaleType()
        }
        mNeedInAnim = mAnimDuration > 0 && mShareData.showNeedAnim
        doPreviewAnim()
    }

    /**
     * 初始化缩略图的缩放类型
     */
    private fun initThumbnailViewScaleType() {
        if (mThumbnailView is ImageView) {
            mThumbnailViewScaleType = (mThumbnailView as ImageView).scaleType
            if (mThumbnailViewScaleType == ImageView.ScaleType.CENTER ||
                mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE
            ) {
                val drawable = (mThumbnailView as ImageView).getDrawable()
                if (drawable != null) {
                    val width = mThumbnailView?.width ?: return
                    val height = mThumbnailView?.height ?: return
                    if (drawable.intrinsicWidth >= width && drawable.intrinsicHeight >= height) {
                        // ScaleType.CENTER：不缩放图片，如果图片大于图片控件，效果与ScaleType.CENTER_CROP一致
                        if (mThumbnailViewScaleType == ImageView.ScaleType.CENTER) {
                            mThumbnailViewScaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                } else if (mAnimDuration > 0) {
                    // 当图片小于控件大小时，对于缩放类型ScaleType.CENTER 和 ScaleType.CENTER_INSIDE，需要以动画的方式打开
                    // 除非整个预览不需要动画，否则再推出时，可能不会无缝衔接
                    mNeedInAnim = true
                }
            }
        } else {
            mThumbnailViewScaleType = null
        }
    }

    /**
     * 执行预览动画
     */
    private fun doPreviewAnim() {
        if (!mNeedInAnim) {
            initNoAnim()
            // 整个预览无需执行动画，因此第一个执行的预览界面执行一次预览打开回调
            callOnOpen(ANIM_START_PRE, ANIM_START, ANIM_END)
            mShareData.showNeedAnim = false
            return
        }

        // 处理进入时的动画
        mNeedInAnim = false
        mShareData.showNeedAnim = false
        mHelpViewCanSetImage = true
        mShareData.preDrawableLoadListener = DrawableLoadListener { drawable ->
            if (mHelpViewCanSetImage) {
                mHelperView.setImageDrawable(drawable)
            }
        }
        mHelperView.setImageDrawable(mShareData.preLoadDrawable)
        mShareData.preLoadDrawable = null
        mThumbnailView?.let { enterAnimByTransition(it) } ?: enterAnimByScale()
    }

    /**
     * 无动画进入
     */
    private fun initNoAnim() {
        mRootViewBgMask.setBackgroundColor(Color.BLACK)
        mHelperViewParent.visibility = View.INVISIBLE
    }

    /**
     * 仅缩放动画
     */
    private fun enterAnimByScale() {
        val scaleOx = ObjectAnimator.ofFloat(mHelperView, "scaleX", 0f, 1f)
        val scaleOy = ObjectAnimator.ofFloat(mHelperView, "scaleY", 0f, 1f)
        callOnOpen(ANIM_START_PRE)
        val set = AnimatorSet()
        set.setDuration(mAnimDuration)
        set.interpolator = INTERPOLATOR
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                callOnOpen(ANIM_START)
                mHelperViewParent.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                mHelperViewParent.visibility = View.INVISIBLE
                callOnOpen(ANIM_END)
            }
        })
        set.playTogether(scaleOx, scaleOy, getViewBgAnim(Color.BLACK, mAnimDuration, null))
        set.start()
    }

    /**
     * 使用Transition库实现过度动画
     */
    private fun enterAnimByTransition(thumbnailView: View) {
        val delay = mShareData.openAnimDelayTime
        callOnOpen(ANIM_START_PRE)
        if (delay > 0) {
            viewPostDelayed({ doEnterAnimByTransition() }, delay, mHelperView)
            initEnterAnimByTransition(thumbnailView)
        } else {
            initEnterAnimByTransition(thumbnailView)
            viewPost({ doEnterAnimByTransition() }, mHelperView)
        }
    }

    /**
     * 初始化Transition所需内容
     */
    private fun initEnterAnimByTransition(thumbnailView: View) {
        getSrcViewSize(thumbnailView)
        getSrcViewLocation(thumbnailView)
        mHelperViewParent.translationX = mSrcImageLocation[0].toFloat()
        mHelperViewParent.translationY = mSrcImageLocation[1].toFloat()
        setViewSize(mHelperViewParent, mSrcViewParentSize[0], mSrcViewParentSize[1])
        setHelperViewDataByThumbnail()
    }

    /**
     * 展示/隐藏缩略图蒙层数据
     */
    fun showThumbnailViewMask(show: Boolean) {
        if (!mShareData.config.showThumbnailViewMask) {
            return
        }
        mThumbnailView?.visibility = if (show) View.INVISIBLE else mThumbnailViewVisibility
    }

    /**
     * 根据缩列图数据设置预览占位View大小、位置和缩放模式
     */
    private fun setHelperViewDataByThumbnail() {
        if (mThumbnailViewScaleType != null) {
            mHelperView.setScaleType(mThumbnailViewScaleType)
        } else {
            mHelperView.setScaleType(ImageView.ScaleType.FIT_CENTER)
        }
        mHelperView.translationX = 0f
        mHelperView.translationY = 0f
        if (mShareData.config.shapeTransformType == null || mThumbnailViewScaleType == null) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1])
            return
        }

        // 需要进行图片裁剪，部分缩放类型不能使图片充满控件，而裁剪基于控件，因此设置控件大小与图片大小一致
        val drawable = (mThumbnailView as ImageView?)?.getDrawable()
        if (drawable == null) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1])
            return
        }
        var intrinsicWidth = drawable.intrinsicWidth
        var intrinsicHeight = drawable.intrinsicHeight
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            setViewSize(mHelperView, mSrcViewSize[0], mSrcViewSize[1])
            return
        }
        var width = mSrcViewSize[0]
        var height = mSrcViewSize[1]
        if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
            var minSize = min(width.toDouble(), height.toDouble()).toInt()
            when (mThumbnailViewScaleType) {
                ImageView.ScaleType.FIT_START, ImageView.ScaleType.MATRIX -> {
                    width = minSize
                    height = minSize
                }

                ImageView.ScaleType.FIT_END -> {
                    mHelperView.translationX = (width - minSize).toFloat()
                    mHelperView.translationY = (height - minSize).toFloat()
                    width = minSize
                    height = minSize
                }

                ImageView.ScaleType.FIT_CENTER -> {
                    mHelperView.translationX = (width - minSize) / 2f
                    mHelperView.translationY = (height - minSize) / 2f
                    width = minSize
                    height = minSize
                }

                ImageView.ScaleType.CENTER_INSIDE -> {
                    if (intrinsicWidth < width && intrinsicHeight < height) {
                        minSize = min(intrinsicWidth.toDouble(), intrinsicHeight.toDouble()).toInt()
                    }
                    mHelperView.translationX = (width - minSize) / 2f
                    mHelperView.translationY = (height - minSize) / 2f
                    width = minSize
                    height = minSize
                }

                ImageView.ScaleType.CENTER -> {
                    val w = min(intrinsicWidth.toDouble(), width.toDouble()).toInt()
                    val h = min(intrinsicHeight.toDouble(), height.toDouble()).toInt()
                    minSize = min(w.toDouble(), h.toDouble()).toInt()
                    mHelperView.translationX = (width - minSize) / 2f
                    mHelperView.translationY = (height - minSize) / 2f
                    width = minSize
                    height = minSize
                }

                else -> Unit
            }

            // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
            // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
            mHelperView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            setViewSize(mHelperView, width, height)
            return
        }
        when (mThumbnailViewScaleType) {
            ImageView.ScaleType.FIT_START, ImageView.ScaleType.FIT_END, ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE -> {
                if (intrinsicWidth < width && intrinsicHeight < height) {
                    if (mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE) {
                        mHelperView.translationX = (width - intrinsicWidth) / 2f
                        mHelperView.translationY = (height - intrinsicHeight) / 2f
                        width = intrinsicWidth
                        height = intrinsicHeight
                    } else {
                        val widthScale = width * 1f / intrinsicWidth
                        val heightScale = height * 1f / intrinsicHeight
                        if (widthScale < heightScale) {
                            // 根据宽度缩放值缩放高度，放大
                            intrinsicHeight = (widthScale * intrinsicHeight).toInt()
                            if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_END) {
                                mHelperView.translationY = (height - intrinsicHeight).toFloat()
                            } else if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER) {
                                mHelperView.translationY = (height - intrinsicHeight) / 2f
                            }
                            height = intrinsicHeight
                        } else if (widthScale > heightScale) {
                            // 根据高度缩放值缩放宽度，放大
                            intrinsicWidth = (heightScale * intrinsicWidth).toInt()
                            if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_END) {
                                mHelperView.translationX = (width - intrinsicWidth).toFloat()
                            } else if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER) {
                                mHelperView.translationX = (width - intrinsicWidth) / 2f
                            }
                            width = intrinsicWidth
                        }
                    }
                } else if (intrinsicWidth > width && intrinsicHeight > height) {
                    val widthScale = intrinsicWidth * 1f / width
                    val heightScale = intrinsicHeight * 1f / height
                    if (widthScale > heightScale) {
                        // 根据宽度缩放值缩放高度，缩小
                        intrinsicHeight = (intrinsicHeight / widthScale).toInt()
                        if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_END) {
                            mHelperView.translationY = (height - intrinsicHeight).toFloat()
                        } else if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER
                            || mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE
                        ) {
                            mHelperView.translationY = (height - intrinsicHeight) / 2f
                        }
                        height = intrinsicHeight
                    } else if (widthScale < heightScale) {
                        // 根据高度缩放值缩放宽度，缩小
                        intrinsicWidth = (intrinsicWidth / heightScale).toInt()
                        if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_END) {
                            mHelperView.translationX = (width - intrinsicWidth).toFloat()
                        } else if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER
                            || mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE
                        ) {
                            mHelperView.translationX = (width - intrinsicWidth) / 2f
                        }
                        width = intrinsicWidth
                    }
                } else if (intrinsicWidth < width) {
                    if (intrinsicHeight > height) {
                        // 根据高度缩放值缩放宽度，缩小
                        val heightScale = intrinsicHeight * 1f / height
                        intrinsicWidth = (intrinsicWidth / heightScale).toInt()
                    }
                    if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_END) {
                        mHelperView.translationX = (width - intrinsicWidth).toFloat()
                    } else if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER
                        || mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE
                    ) {
                        mHelperView.translationX = (width - intrinsicWidth) / 2f
                    }
                    width = intrinsicWidth
                } else {
                    if (intrinsicWidth > width) {
                        // 根据宽度缩放值缩放高度，缩小
                        val widthScale = intrinsicWidth * 1f / width
                        intrinsicHeight = (intrinsicHeight / widthScale).toInt()
                    }
                    if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_END) {
                        mHelperView.translationY = (height - intrinsicHeight).toFloat()
                    } else if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER
                        || mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE
                    ) {
                        mHelperView.translationY = (height - intrinsicHeight) / 2f
                    }
                    height = intrinsicHeight
                }
                if (mThumbnailViewScaleType == ImageView.ScaleType.FIT_CENTER
                    || mThumbnailViewScaleType == ImageView.ScaleType.CENTER_INSIDE
                ) {
                    // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
                    // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
                    mHelperView.setScaleType(ImageView.ScaleType.CENTER_CROP)
                }
            }

            ImageView.ScaleType.CENTER -> {
                if (width > intrinsicWidth) {
                    mHelperView.translationX = (width - intrinsicWidth) / 2f
                }
                if (height > intrinsicHeight) {
                    mHelperView.translationY = (height - intrinsicHeight) / 2f
                }
                width = min(intrinsicWidth.toDouble(), width.toDouble()).toInt()
                height = min(intrinsicHeight.toDouble(), height.toDouble()).toInt()
                // 需要重新设置缩放类型为CENTER_CROP，否则动画不会无缝衔接
                // 重新调整缩放类型，全部都是为了适配 Transition动画ChangeImageTransform过渡
                mHelperView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            }

            ImageView.ScaleType.MATRIX -> {
                width = min(intrinsicWidth.toDouble(), width.toDouble()).toInt()
                height = min(intrinsicHeight.toDouble(), height.toDouble()).toInt()
                mHelperView.setScaleType(ImageView.ScaleType.FIT_START)
            }

            else -> Unit
        }
        setViewSize(mHelperView, width, height)
    }

    /**
     * 执行预览打开过渡动画
     */
    private fun doEnterAnimByTransition() {
        val transitionSet = TransitionSet()
            .setDuration(mAnimDuration)
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .setInterpolator(INTERPOLATOR)
            .addListener(object : TransitionListenerAdapter() {
                override fun onTransitionStart(transition: Transition) {
                    callOnOpen(ANIM_START)
                    mHelpViewCanSetImage = false
                    doViewBgAnim(Color.BLACK, mAnimDuration, null)
                    mHelperViewParent.visibility = View.VISIBLE
                    // 不延迟会有闪屏
                    mThumbnailView?.let { viewPostDelayed({ showThumbnailViewMask(true) }, mAnimDuration / 10, it, mHelperView) }
                }

                override fun onTransitionEnd(transition: Transition) {
                    showThumbnailViewMask(false)
                    mHelpViewCanSetImage = true
                    mHelperViewParent.visibility = View.INVISIBLE
                    callOnOpen(ANIM_END)
                }
            })
        if (mShareData.config.shapeTransformType != null) {
            if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
                transitionSet.addTransition(
                    ChangeShape((min(mSrcViewSize[0].toDouble(), mSrcViewSize[1].toDouble()) / 2f).toFloat(), 0f)
                        .addTarget(mHelperView)
                )
            } else {
                transitionSet.addTransition(
                    ChangeShape(mShareData.config.shapeCornerRadius.toFloat(), 0f)
                        .addTarget(mHelperView)
                )
            }
        }
        if (mHelperView.getDrawable() != null) {
            mHelpViewCanSetImage = false
            // ChangeImageTransform执行MATRIX变换，因此一定需要最终加载完成图片
            transitionSet.addTransition(ChangeImageTransform().addTarget(mHelperView))
        }
        TransitionManager.beginDelayedTransition((mHelperViewParent.parent as ViewGroup), transitionSet)
        mHelperViewParent.translationX = 0f
        mHelperViewParent.translationY = 0f
        setViewSize(mHelperViewParent, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mHelperView.translationX = 0f
        mHelperView.translationY = 0f
        mHelperView.setScaleType(ImageView.ScaleType.FIT_CENTER)
        setViewSize(mHelperView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 设置View的大小
     */
    private fun setViewSize(target: View, width: Int, height: Int) {
        val params = target.layoutParams
        params.width = width
        params.height = height
        target.setLayoutParams(params)
    }

    /**
     * 退出预览
     *
     * @return `false`:未执行退出逻辑，可能当前界面已关闭或还未创建完成
     */
    fun exit(): Boolean {
        if (!mFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return false
        }
        if (mAnimDuration <= 0) {
            callOnExit(ANIM_START_PRE, ANIM_START, ANIM_END)
            return true
        }
        val view = mFragment.mViewPager?.findViewWithTag<View>(mFragment.mViewPager?.currentItem)
        if (view == null) {
            callOnExit(ANIM_START_PRE, ANIM_START, ANIM_END)
            return true
        }
        val tag = view.getTag(R.id.view_holder)
        if (tag !is ImagePagerAdapter.ViewHolder) {
            callOnExit(ANIM_START_PRE, ANIM_START, ANIM_END)
            return true
        }
        val photoView = tag.pictureView
        tag.loading.visibility = View.GONE
        if (photoView.getDrawable() == null) {
            callOnExit(ANIM_START_PRE, ANIM_START)
            doViewBgAnim(Color.TRANSPARENT, mAnimDuration, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    callOnExit(ANIM_END)
                }
            })
            return true
        }

        // 记录预览打开时，缩略图对象
        val openThumbnailView = mThumbnailView
        mThumbnailView = getThumbnailView(mShareData)
        if (openThumbnailView !== mThumbnailView) {
            mThumbnailView?.let { mThumbnailViewVisibility = it.visibility }
            mAnimDuration = getOpenAndExitAnimDuration(mThumbnailView, mShareData)
            initThumbnailViewScaleType()
        }

        // 关闭时，最小缩放比设置为0f，否则手指放开，预览图又会回到1倍图大小，导致计算不准确
        photoView.minimumScale = 0f
        resetHelpViewSize(tag)
        if (mThumbnailView == null) {
            callOnOpen(ANIM_START_PRE)
            val scaleOx = ObjectAnimator.ofFloat(mHelperView, "scaleX", 1f, 0f)
            val scaleOy = ObjectAnimator.ofFloat(mHelperView, "scaleY", 1f, 0f)
            val set = AnimatorSet()
            set.setDuration(mAnimDuration)
            set.interpolator = INTERPOLATOR
            set.playTogether(scaleOx, scaleOy, getViewBgAnim(Color.TRANSPARENT, mAnimDuration, null))
            set.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    callOnExit(ANIM_START)
                    mHelperViewParent.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    callOnExit(ANIM_END)
                }
            })
            set.start()
            return true
        }
        mThumbnailView?.let { exitAnimByTransition(it, photoView, openThumbnailView) }
        return true
    }

    /**
     * 重置辅助View的大小
     */
    private fun resetHelpViewSize(viewHolder: ImagePagerAdapter.ViewHolder) {
        val photoView = viewHolder.pictureView
        val noScaleImageActualSize = viewHolder.noScaleImageActualSize
        val rootView = mFragment.mRootView ?: return
        if (mThumbnailViewScaleType == ImageView.ScaleType.MATRIX || photoView.scale != 1f) {
            // thumbnailView是ScaleType.MATRIX需要设置ImageView与drawable大小一致,且如果是下拉后缩小后关闭预览，则肯能无法与缩略图无缝衔接
            // 得到关闭时预览图片真实绘制大小
            val imageActualSize = mFloatTemp
            getImageActualSize(photoView, imageActualSize)
            if (noScaleImageActualSize[0] == 0f && noScaleImageActualSize[1] == 0f) {
                // 此时只是降低偏移值，但是这是不准确的
                getImageActualSize(mHelperView, noScaleImageActualSize)
            }
            if (photoView.scale < 1 || mThumbnailViewScaleType == ImageView.ScaleType.MATRIX && photoView.scale == 1f) {
                // 计算关闭时预览图片真实X,Y坐标
                // mPhotoView向下移动后缩放比例误差值，该值是手动调出来的，不清楚为什么超出了这么多
                // 只有mPhotoView.getScale() < 1时才会出现此误差
                val errorRatio = if (photoView.scale < 1) 0.066f else 0f
                // 此处取mRoot.getHeight()而不是mIvAnim.getHeight()是因为如果当前界面非默认预览的界面,
                // 那么mIvAnim.getHeight()获取的并不是可显示区域高度，只是图片实际绘制的高度，因此使用mRoot.getHeight()
                val y = (// 预览图片未移动未缩放时实际绘制drawable左上角Y轴值
                        rootView.height / 2f - noScaleImageActualSize[1] / 2 - photoView.scrollY // 向下移动的距离，向上移动不会触发关闭
                                + imageActualSize[1] * (1 - photoView.scale - errorRatio)) // 由于在向下移动时，伴随图片缩小，因此需要加上缩小高度
                val x = (// 预览图片未移动未缩放时实际绘制drawable左上角X轴值
                        rootView.width / 2f - noScaleImageActualSize[0] / 2 - photoView.scrollX // 向左或向右移动的距离
                                + imageActualSize[0] * (1 - photoView.scale - errorRatio)) // 由于在向下移动时，伴随图片缩小，因此需要加上缩小宽度
                mHelperViewParent.translationX = x
                mHelperViewParent.translationY = y
            } else if (photoView.scale > 1) {
                val imageMatrix = photoView.imageMatrix
                val scrollX = MatrixUtils.getValue(imageMatrix, Matrix.MTRANS_X)
                val scrollY = MatrixUtils.getValue(imageMatrix, Matrix.MTRANS_Y)
                val y = if (imageActualSize[1] > rootView.height) scrollY else rootView.height / 2f - imageActualSize[1] / 2f
                val x = if (imageActualSize[0] > rootView.width) scrollX else rootView.width / 2f - imageActualSize[0] / 2f
                mHelperViewParent.translationX = x
                mHelperViewParent.translationY = y
            }
            setViewSize(mHelperViewParent, imageActualSize[0].toInt(), imageActualSize[1].toInt())
            setViewSize(mHelperView, imageActualSize[0].toInt(), imageActualSize[1].toInt())
        }
    }

    /**
     * 使用Transition库实现过度动画
     */
    private fun exitAnimByTransition(thumbnailView: View, pictureView: PictureView, openThumbnailView: View?) {
        callOnExit(ANIM_START_PRE)
        mHelperView.setScaleType(ImageView.ScaleType.FIT_CENTER)
        mHelperView.setImageDrawable(pictureView.getDrawable())
        mThumbnailView?.let {
            viewPostDelayed({

                // 延迟100毫秒后计算缩略图位置，因为关闭时存在全屏->非全屏或非全屏->全屏的转换，此时缩略图位置可能发生了改变
                if (thumbnailView === openThumbnailView) {
                    // 进入和退出缩略图位置不变
                    // 获取退出时缩略图位置
                    getSrcViewLocation(thumbnailView)
                } else {
                    getSrcViewLocation(thumbnailView)
                    getSrcViewSize(thumbnailView)
                }
                viewPost({
                    val transitionSet = TransitionSet()
                        .setDuration(mAnimDuration)
                        .addTransition(ChangeBounds())
                        .addTransition(ChangeTransform())
                        .addTransition(ChangeImageTransform().addTarget(mHelperView))
                        .setInterpolator(INTERPOLATOR)
                        .addListener(object : TransitionListenerAdapter() {
                            override fun onTransitionEnd(transition: Transition) {
                                showThumbnailViewMask(false)
                                if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                                    // android13需要先将预览图隐藏再关闭预览的dialog，否则出现预览图发生位移闪屏
                                    mHelperViewParent.visibility = View.INVISIBLE
                                    viewPost({ callOnExit(ANIM_END) }, mHelperViewParent)
                                } else {
                                    callOnExit(ANIM_END)
                                }
                            }

                            override fun onTransitionStart(transition: Transition) {
                                callOnExit(ANIM_START)
                                if (pictureView.scale >= 1) {
                                    showThumbnailViewMask(true)
                                }
                                mHelperViewParent.visibility = View.VISIBLE
                                doViewBgAnim(Color.TRANSPARENT, mAnimDuration, null)
                            }
                        })
                    if (mShareData.config.shapeTransformType != null) {
                        if (mShareData.config.shapeTransformType == ShapeTransformType.CIRCLE) {
                            transitionSet.addTransition(
                                ChangeShape(0f, (min(mSrcViewSize[0].toDouble(), mSrcViewSize[1].toDouble()) / 2f).toFloat())
                                    .addTarget(mHelperView)
                            )
                        } else {
                            transitionSet.addTransition(
                                ChangeShape(0f, mShareData.config.shapeCornerRadius.toFloat())
                                    .addTarget(mHelperView)
                            )
                        }
                    }
                    TransitionManager.beginDelayedTransition((mHelperViewParent.parent as ViewGroup), transitionSet)
                    mHelperViewParent.translationX = mSrcImageLocation[0].toFloat()
                    mHelperViewParent.translationY = mSrcImageLocation[1].toFloat()
                    setViewSize(mHelperViewParent, mSrcViewParentSize[0], mSrcViewParentSize[1])
                    setHelperViewDataByThumbnail()
                }, mHelperView)
            }, 100, it, mHelperView)
        }
    }

    private fun viewPostDelayed(runnable: Runnable, delayMillis: Long, vararg views: View) {
        for (view in views) {
            if (view.postDelayed(runnable, delayMillis)) {
                return
            }
        }
        runnable.run()
    }

    private fun viewPost(runnable: Runnable, vararg views: View) {
        for (view in views) {
            if (view.post(runnable)) {
                return
            }
        }
        runnable.run()
    }

    /**
     * 执行背景过渡动画
     */
    fun doViewBgAnim(endColor: Int, duration: Long, listenerAdapter: AnimatorListenerAdapter?) {
        getViewBgAnim(endColor, duration, listenerAdapter).start()
    }

    /**
     * 返回背景过渡动画
     */
    fun getViewBgAnim(endColor: Int, duration: Long, listenerAdapter: AnimatorListenerAdapter?): Animator {
        val start = (mRootViewBgMask.background as ColorDrawable).color
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation: ValueAnimator -> mRootViewBgMask.setBackgroundColor(ARGB_EVALUATOR.evaluate(animation.animatedFraction, start, endColor) as Int) }
        animator.setDuration(duration)
        animator.interpolator = INTERPOLATOR
        if (listenerAdapter != null) {
            animator.addListener(listenerAdapter)
        }
        return animator
    }

    /**
     * 获取动画时间
     */
    private fun getOpenAndExitAnimDuration(thumbnailView: View?, shareData: ShareData?): Long {
        shareData?.config?.animDuration?.let { return it }
        return if (thumbnailView is ImageView) OPEN_AND_EXIT_ANIM_DURATION_FOR_IMAGE
        else OPEN_AND_EXIT_ANIM_DURATION
    }

    /**
     * 获取ImageView实际绘制的图片大小,如果没有设置图片，则返回数据为0。
     * 该方法调用时机不同，返回值有很大差别，如果刚设置imageView drawable，
     * 则可能返回的是drawable原图大小，而不是在imageView中实际绘制出来的大小
     */
    private fun getImageActualSize(imageView: ImageView?, size: FloatArray) {
        size[0] = 0f
        size[1] = 0f
        if (imageView?.getDrawable() == null) {
            return
        }
        val drawable = imageView.getDrawable()
        // 获得ImageView中Image的真实宽高，
        val dw = drawable.getBounds().width()
        val dh = drawable.getBounds().height()

        // 获得ImageView中Image的变换矩阵
        val m = imageView.getImageMatrix()
        // Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        val sx = MatrixUtils.getValue(m, Matrix.MSCALE_X)
        val sy = MatrixUtils.getValue(m, Matrix.MSCALE_Y)

        // 计算Image在屏幕上实际绘制的宽高
        size[0] = dw * sx
        size[1] = dh * sy
    }

    /**
     * 获取当前预览图对应的缩略图View,如果未找到则查找默认位置View
     */
    private fun getThumbnailView(shareData: ShareData): View? {
        val view = getThumbnailViewNotDefault(shareData, mPosition)
        if (view == null) {
            if (mPosition != shareData.config.defaultShowPosition) {
                return getThumbnailViewNotDefault(shareData, shareData.config.defaultShowPosition)
            }
        }
        return view
    }

    /**
     * 获取指定位置的缩略图
     */
    private fun getThumbnailViewNotDefault(shareData: ShareData, position: Int): View? {
        if (shareData.thumbnailView != null) {
            return shareData.thumbnailView
        } else if (shareData.findThumbnailView != null) {
            return shareData.findThumbnailView?.findView(position)
        }
        return null
    }

    /**
     * 获取指定位置的略图的大小.
     * 结果存储在[.mSrcViewSize]
     */
    private fun getSrcViewSize(view: View?) {
        mSrcViewSize[0] = 0
        mSrcViewSize[1] = 0
        mSrcViewParentSize[0] = 0
        mSrcViewParentSize[1] = 0
        if (view == null) {
            return
        }
        mSrcViewSize[0] = view.width
        mSrcViewSize[1] = view.height
        mSrcViewParentSize[0] = mSrcViewSize[0]
        mSrcViewParentSize[1] = mSrcViewSize[1]
        // 暂时不处理缩略图父类比自己小的情况，主要原因是无法确定缩略图在父类布局方式
        // getSrcViewParentSize(view.getParent());
    }

    /**
     * 获取父类最小宽高，可能View设置了固定宽高，但是父类被裁剪，因此实际绘制区域可能没有设定宽高那么大
     */
    private fun getSrcViewParentSize(parent: ViewParent) {
        if (parent is View) {
            val width = (parent as View).width
            if (width < mSrcViewParentSize[0]) {
                mSrcViewParentSize[0] = width
            }
            val height = (parent as View).height
            if (height < mSrcViewParentSize[1]) {
                mSrcViewParentSize[1] = height
            }
            if (width > mSrcViewParentSize[0] && height > mSrcViewParentSize[1]) {
                return
            }
            getSrcViewParentSize(parent.getParent())
        }
    }

    /**
     * 获取指定位置缩略图的位置
     * 结果存储在[.mSrcImageLocation]
     */
    private fun getSrcViewLocation(view: View?) {
        mSrcImageLocation[0] = 0
        mSrcImageLocation[1] = 0
        if (view == null) {
            return
        }
        view.getLocationOnScreen(mSrcImageLocation)
        // 预览界面采用沉浸式全屏显示模式，如果手机系统支持，横竖屏都绘制到耳朵区域
        // 以下逻辑防止部分手机横屏时，耳朵区域不显示内容，此时设置的预览坐标不能采用OnScreen坐标
        mFragment.mRootView?.getLocationOnScreen(mIntTemp)
        mSrcImageLocation[0] -= mIntTemp[0]
        mSrcImageLocation[1] -= mIntTemp[1]
    }

    /**
     * 获取指定位置的略图的背景颜色
     */
    private fun getSrcViewBg(view: View): Drawable? {
        val drawable = getSrcParentBg(view)
        if (drawable != null) {
            val colorDrawable = ColorDrawable(drawable.color)
            if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                colorDrawable.colorFilter = drawable.colorFilter
            }
            colorDrawable.setAlpha(drawable.alpha)
            colorDrawable.setState(drawable.state)
            return colorDrawable
        }
        return null
    }

    /**
     * 获取指定位置的略图的背景颜色
     */
    private fun getSrcParentBg(view: View): ColorDrawable? {
        val background = view.background
        if (background is ColorDrawable) {
            return background
        }
        val parent = view.parent
        return if (parent !is View) null
        else getSrcParentBg(parent as View)
    }

    /**
     * 预览界面打开时执行回调
     */
    private fun callOnOpen(vararg animTypes: Int) {
        val list = mutableListOf<OnOpenListener?>()
        mShareData.onOpenListener?.let { list.add(it) }
        mOpenListenerList?.let { list.addAll(it) }
        for (type in animTypes) {
            if (type == ANIM_END) {
                isOpenAnimEnd = true
                break
            }
        }
        for (type in animTypes) {
            for (onOpenListener in list) {
                when (type) {
                    ANIM_START_PRE -> onOpenListener?.onStartPre()
                    ANIM_START -> onOpenListener?.onStart()
                    ANIM_END -> onOpenListener?.onEnd()
                }
            }
        }
    }

    /**
     * 预览图拖拽导致缩放倍率改变
     */
    fun dragScaleChange(scale: Float) {
        oldScaleLessOne =
            if (scale < 1) {
                if (!oldScaleLessOne) mFragment.initFullScreen(false)
                true
            } else {
                if (oldScaleLessOne) mFragment.initFullScreen(true)
                false
            }
    }

    /**
     * 预览界面关闭时执行回调
     */
    private fun callOnExit(vararg animTypes: Int) {
        val list = mutableListOf<OnExitListener?>()
        mShareData.onExitListener?.let { list.add(it) }
        mExitListenerList?.let { list.addAll(it) }
        var isAnimEnd = false
        for (type in animTypes) {
            if (type == ANIM_END) {
                isAnimEnd = true
            }
            for (onExitListener in list) {
                when (type) {
                    ANIM_START_PRE -> onExitListener?.onStartPre()
                    ANIM_START -> onExitListener?.onStart()
                    ANIM_END -> onExitListener?.onExit()
                }
            }
        }
        if (isAnimEnd) {
            mOpenListenerList?.clear()
            mExitListenerList?.clear()
        }
    }

    /**
     * 设置当前预览图片的位置
     */
    fun setPosition(position: Int) {
        mPosition = position
    }

    /**
     * 增加预览动画打开监听
     */
    fun addOnOpenListener(openListener: OnOpenListener?) {
        if (openListener == null) {
            return
        }
        if (mOpenListenerList == null) {
            mOpenListenerList = ArrayList()
        }
        mOpenListenerList?.add(openListener)
    }

    /**
     * 移除预览动画打开监听
     */
    fun removeOnOpenListener(openListener: OnOpenListener?) {
        if (openListener == null || mOpenListenerList == null) {
            return
        }
        mOpenListenerList?.remove(openListener)
    }

    /**
     * 增加预览动画关闭监听
     */
    fun addOnExitListener(onExitListener: OnExitListener?) {
        if (onExitListener == null) {
            return
        }
        if (mExitListenerList == null) {
            mExitListenerList = ArrayList()
        }
        mExitListenerList?.add(onExitListener)
    }

    /**
     * 移除预览动画关闭监听
     */
    fun removeOnExitListener(onExitListener: OnExitListener?) {
        if (onExitListener == null || mExitListenerList == null) {
            return
        }
        mExitListenerList?.remove(onExitListener)
    }

    /**
     * 预览退出监听
     */
    interface OnExitListener {
        /**
         * 动画开始之前数据准备阶段
         */
        fun onStartPre()

        /**
         * 退出动作开始执行
         */
        fun onStart()

        /**
         * 完全退出
         */
        fun onExit()
    }

    /**
     * 预览打开监听
     */
    interface OnOpenListener {
        /**
         * 动画开始之前数据准备阶段
         */
        fun onStartPre()

        /**
         * 进入动画开始执行
         */
        fun onStart()

        /**
         * 进入动画开始执行结束
         */
        fun onEnd()
    }


}
