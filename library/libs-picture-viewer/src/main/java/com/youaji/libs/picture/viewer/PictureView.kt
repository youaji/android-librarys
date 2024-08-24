package com.youaji.libs.picture.viewer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.Scroller
import com.youaji.libs.picture.viewer.custom.OnScaleChangedListener
import com.youaji.libs.picture.viewer.custom.OnViewDragListener
import com.youaji.libs.picture.viewer.custom.PhotoView
import com.youaji.libs.picture.viewer.custom.PhotoViewAttacher
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A zoomable ImageView.
 * See [PhotoViewAttacher] for most of the details on how the zooming is accomplished.
 */
 class PictureView @JvmOverloads constructor(
    context: Context, attr: AttributeSet? = null, defStyle: Int = 0
) : PhotoView(context, attr, defStyle), OnScaleChangedListener, OnViewDragListener {

    companion object {
        private const val RESET_ANIM_TIME = 100
    }

    private val mScroller: Scroller

    // 是否是预览的第一个View
    private var mStartView = false

    // 是否是预览的最后一个View
    private var mEndView = false
    private var mHelper: PictureViewerHelper? = null
    private var mImageChangeListener: ImageChangeListener? = null
    private val mViewConfiguration: ViewConfiguration

    // 当前是否正在拖拽
    private var mDragging = false
    private var mBackgroundAnimStart = false

    // 透明度
    private var mIntAlpha = 255

    // 记录缩放后垂直方向边界判定值
    private var mScaleVerticalScrollEdge = PhotoViewAttacher.VERTICAL_EDGE_INSIDE

    // 记录缩放后水平方向边界判定值
    private var mScaleHorizontalScrollEdge = PhotoViewAttacher.HORIZONTAL_EDGE_INSIDE
    private var mOnScaleChangedListener: OnScaleChangedListener? = null

    init {
        super.setOnScaleChangeListener(this)
        setOnViewDragListener(this)
        mScroller = Scroller(context)
        mViewConfiguration = ViewConfiguration.get(context)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            postInvalidate()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> onFingerUp()
        }
        return super.dispatchTouchEvent(event)
    }

    private fun onFingerUp() {
        mDragging = false
        if (scale > 1) {
            if (abs(scrollX.toDouble()) > 0 || abs(scrollY.toDouble()) > 0) {
                reset()
            }
            return
        }

        // 这里恢复位置和透明度
        if (mIntAlpha != 255 && scale < 0.8) {
            mHelper?.exit()
        } else {
            reset()
        }
    }

    private fun reset() {
        mIntAlpha = 255
        mBackgroundAnimStart = true
        mHelper?.dragScaleChange(1f)
        mHelper?.doViewBgAnim(Color.BLACK, RESET_ANIM_TIME.toLong(), object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mBackgroundAnimStart = false
            }
        })
        mScroller.startScroll(scrollX, scrollY, -scrollX, -scrollY, RESET_ANIM_TIME)
        invalidate()
    }

    override fun setOnScaleChangeListener(onScaleChangedListener: OnScaleChangedListener?) {
        mOnScaleChangedListener = onScaleChangedListener
    }

    override fun onScaleChange(scaleFactor: Float, focusX: Float, focusY: Float) {
        mScaleVerticalScrollEdge = attacher.verticalScrollEdge
        mScaleHorizontalScrollEdge = attacher.horizontalScrollEdge
        mOnScaleChangedListener?.onScaleChange(scaleFactor, focusX, focusY)
    }

    override fun onDrag(dx: Float, dy: Float): Boolean {
        val intercept = mBackgroundAnimStart || sqrt((dx * dx + dy * dy).toDouble()) < mViewConfiguration.scaledTouchSlop || !hasVisibleDrawable()

        if (!mDragging && intercept)
            return false
        if (scale > 1)
            return dragWhenScaleThanOne(dx, dy)
        if (!mDragging && abs(dx.toDouble()) > abs(dy.toDouble()))
            return false

        if (!mDragging) {
            // 执行拖拽操作，请求父类不要拦截请求
            val parent = parent
            parent?.requestDisallowInterceptTouchEvent(true)
        }

        mDragging = true
        var s = scale
        // 移动图像
        scrollBy(-dx.toInt(), -dy.toInt())
        val scrollY = scrollY.toFloat()

        if (scrollY >= 0) {
            s = 1f
            mIntAlpha = 255
        } else {
            s -= dy * 0.001f
            mIntAlpha = (mIntAlpha - dy * 0.03).toInt()
        }

        if (s > 1) {
            s = 1f
        } else if (s < 0) {
            s = 0f
        }

        if (mIntAlpha < 200) {
            mIntAlpha = 200
        } else if (mIntAlpha > 255) {
            mIntAlpha = 255
        }

        mHelper?.mRootViewBgMask?.background?.alpha = mIntAlpha
        mHelper?.showThumbnailViewMask(mIntAlpha >= 255)
        if (scrollY < 0 && s >= 0.6) {
            // 更改大小
            setScale(s)
            mHelper?.dragScaleChange(s)
        }
        return true
    }

    /**
     * 处理图片如果超出控件大小时的滑动
     */
    private fun dragWhenScaleThanOne(dxValue: Float, dyValue: Float): Boolean {
        var dx = dxValue
        var dy = dyValue
        val dxBigDy = abs(dx.toDouble()) > abs(dy.toDouble())
        if (mDragging) {
            dx *= 0.2f
            dy *= 0.2f
            val scrollX = (scrollX - dx).toInt()
            val scrollY = (scrollY - dy).toInt()
            val width = (width * 0.2).toInt()
            val height = (height * 0.2).toInt()

            if (abs(scrollX.toDouble()) > width)
                dx = 0f

            if (abs(scrollY.toDouble()) > height)
                dy = 0f

            if (dxBigDy) dy = 0f
            else dx = 0f

            // 移动图像
            scrollBy(-dx.toInt(), -dy.toInt())
            return true
        } else {
            val verticalScrollEdge = attacher.verticalScrollEdge
            val horizontalScrollEdge = attacher.horizontalScrollEdge
            val isTop = (verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_TOP || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH)
            val isBottom = (verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTTOM || verticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_BOTH)
            val isStart = (horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_LEFT || horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_BOTH)
            val isEnd = (horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_RIGHT || horizontalScrollEdge == PhotoViewAttacher.HORIZONTAL_EDGE_BOTH)
            val isVerticalScroll = !dxBigDy && (isTop && dy > 0 || isBottom && dy < 0)
            val isHorizontalScroll = dxBigDy && (mStartView && isStart && dx > 0 || mEndView && isEnd && dx < 0)
            if (isVerticalScroll && mScaleVerticalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE
                || isHorizontalScroll && mScaleHorizontalScrollEdge == PhotoViewAttacher.VERTICAL_EDGE_OUTSIDE
            ) {
                // 执行拖拽操作，请求父类不要拦截请求
                parent?.requestDisallowInterceptTouchEvent(true)
                mDragging = true
                // 移动图像
                scrollBy(-dx.toInt(), -dy.toInt())
                return true
            }
        }
        return false
    }

    /**
     * 是否存在可观察的图像
     */
    private fun hasVisibleDrawable(): Boolean {
        if (getDrawable() == null) {
            return false
        }
        val drawable = getDrawable()
        // 获得ImageView中Image的真实宽高，
        val dw = drawable.getBounds().width()
        val dh = drawable.getBounds().height()
        return dw > 0 && dh > 0
    }

    override fun getAlpha(): Float {
        return mIntAlpha.toFloat()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        mImageChangeListener?.onChange(getDrawable())
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        mImageChangeListener?.onChange(getDrawable())
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        mImageChangeListener?.onChange(getDrawable())

    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        mImageChangeListener?.onChange(getDrawable())
    }

    fun setPhotoPreviewHelper(helper: PictureViewerHelper?) {
        mHelper = helper
    }

    fun setImageChangeListener(listener: ImageChangeListener?) {
        mImageChangeListener = listener
    }

    fun setStartView(isStartView: Boolean) {
        mStartView = isStartView
    }

    fun setEndView(isEndView: Boolean) {
        mEndView = isEndView
    }

    /**
     * 设置的图片发生更改
     */
    fun interface ImageChangeListener {
        /**
         * 图片发生更改，但是此时并不一定绘制到界面
         */
        fun onChange(drawable: Drawable?)
    }
}
