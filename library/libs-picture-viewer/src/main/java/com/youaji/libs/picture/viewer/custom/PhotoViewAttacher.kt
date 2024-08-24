package com.youaji.libs.picture.viewer.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.OverScroller
import androidx.annotation.RestrictTo
import com.youaji.libs.picture.viewer.custom.Compat.postOnAnimation
import com.youaji.libs.picture.viewer.custom.Util.checkZoomLevels
import com.youaji.libs.picture.viewer.custom.Util.hasDrawable
import com.youaji.libs.picture.viewer.custom.Util.isSupportedScaleType
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The component of [PhotoView] which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than AppCompatImageView and still
 * gain the functionality that [PhotoView] offers
 */
@Suppress("unused")
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PhotoViewAttacher @SuppressLint("ClickableViewAccessibility") constructor(
    private val mImageView: ImageView
) : OnTouchListener, OnLayoutChangeListener {

    companion object {
        const val DEFAULT_MAX_SCALE = 3.0f
        const val DEFAULT_MID_SCALE = 1.75f
        const val DEFAULT_MIN_SCALE = 1.0f

        // 图片左右边缘包含在ImageView宽度内
        const val HORIZONTAL_EDGE_INSIDE = -2

        // 图片左右边缘超出ImageView宽度
        const val HORIZONTAL_EDGE_OUTSIDE = -1

        // 图片左边缘靠近ImageView左边缘
        const val HORIZONTAL_EDGE_LEFT = 0

        // 图片右边缘靠近ImageView右边缘
        const val HORIZONTAL_EDGE_RIGHT = 1

        // 图片左右边缘靠近ImageView左右边缘，此时图片宽度等于ImageView宽度
        const val HORIZONTAL_EDGE_BOTH = 2

        // 图片上下边缘包含在ImageView高度内
        const val VERTICAL_EDGE_INSIDE = -2

        // 图片上下边缘超出ImageView高度
        const val VERTICAL_EDGE_OUTSIDE = -1

        // 图片上边缘靠近ImageView上边缘
        const val VERTICAL_EDGE_TOP = 0

        // 图片下边缘靠近ImageView下边缘
        const val VERTICAL_EDGE_BOTTOM = 1

        // 图片上下边缘靠近ImageView上下边缘，此时图片高度等于ImageView高度
        const val VERTICAL_EDGE_BOTH = 2

        private const val DEFAULT_ZOOM_DURATION = 200
        private const val SINGLE_TOUCH = 1
    }

    private var mInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private var mZoomDuration = DEFAULT_ZOOM_DURATION
    private var mMinScale = DEFAULT_MIN_SCALE
    private var mMidScale = DEFAULT_MID_SCALE
    private var mMaxScale = DEFAULT_MAX_SCALE
    private var mAllowParentInterceptOnEdge = true
    private var mBlockParentIntercept = false

    // Gesture Detectors
    private var mGestureDetector: GestureDetector? = null
    private var mScaleDragDetector: CustomGestureDetector? = null

    // These are set so we don't keep allocating them on the heap
    private val mBaseMatrix = Matrix()
    val imageMatrix = Matrix()
    private val mSuppMatrix = Matrix()
    private val mDisplayRect = RectF()
    private val mMatrixValues = FloatArray(9)

    // Listeners
    private var mMatrixChangeListener: OnMatrixChangedListener? = null
    private var mPhotoTapListener: OnPhotoTapListener? = null
    private var mOutsidePhotoTapListener: OnOutsidePhotoTapListener? = null
    private var mViewTapListener: OnViewTapListener? = null
    private var mOnClickListener: View.OnClickListener? = null
    private var mLongClickListener: View.OnLongClickListener? = null
    private var mScaleChangeListener: OnScaleChangedListener? = null
    private var mSingleFlingListener: OnSingleFlingListener? = null
    private var mOnViewDragListener: OnViewDragListener? = null

    private var mCurrentFlingRunnable: FlingRunnable? = null
    var horizontalScrollEdge = HORIZONTAL_EDGE_BOTH
        private set
    var verticalScrollEdge = VERTICAL_EDGE_BOTH
        private set
    private var mBaseRotation: Float = 0.0f

    private var isZoomEnabled = true
    private var mScaleType = ImageView.ScaleType.FIT_CENTER
    private val onGestureListener = object : OnGestureListener {
        override fun onDrag(dx: Float, dy: Float) {
            // 该逻辑已经调整到 CustomGestureDetector 处理
            // if (mScaleDragDetector.isScaling()) {
            //     return; // Do not drag if we are already scaling
            // }
            mSuppMatrix.postTranslate(dx, dy)
            checkAndDisplayMatrix()

            if (mOnViewDragListener?.onDrag(dx, dy) == true) {// consume
                return
            }

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             */
            val parent = mImageView.parent
            if (mAllowParentInterceptOnEdge
                && mScaleDragDetector?.isScaling == false
                && !mBlockParentIntercept
            ) {
                // 增加 mHorizontalScrollEdge == HORIZONTAL_EDGE_INSIDE 时也让父类拦截
                // 说明图片实际宽度小于View的宽度
                if (horizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                    || horizontalScrollEdge == HORIZONTAL_EDGE_INSIDE
                    || horizontalScrollEdge == HORIZONTAL_EDGE_LEFT && dx >= 1f
                    || horizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f // 本项目只结合ViewPager,只有左右滑动冲突，因此不做垂直处理
                // || mVerticalScrollEdge == VERTICAL_EDGE_BOTH
                // || (mVerticalScrollEdge == VERTICAL_EDGE_TOP && dy >= 1f)
                // || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f)
                ) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            } else {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
        }

        override fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float) {
            mCurrentFlingRunnable = FlingRunnable(mImageView.context)
            mCurrentFlingRunnable?.fling(getImageViewWidth(mImageView), getImageViewHeight(mImageView), velocityX.toInt(), velocityY.toInt())
            mImageView.post(mCurrentFlingRunnable)
        }

        override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
            if (scale < mMaxScale || scaleFactor < 1f) {
                mScaleChangeListener?.onScaleChange(scaleFactor, focusX, focusY)
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
                checkAndDisplayMatrix()
            }
        }
    }

    init {
        mImageView.setOnTouchListener(this)
        mImageView.addOnLayoutChangeListener(this)

        if (!mImageView.isInEditMode) {
            // Create Gesture Detectors...
            mScaleDragDetector = CustomGestureDetector(mImageView.context, onGestureListener)
            mGestureDetector = GestureDetector(mImageView.context, object : SimpleOnGestureListener() {
                // forward long click listener
                override fun onLongPress(e: MotionEvent) {
                    mLongClickListener?.onLongClick(mImageView)
                }

                override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                    mSingleFlingListener?.let { listener ->
                        e1?.let { event1 ->
                            if (scale > DEFAULT_MIN_SCALE)
                                return false
                            return if (event1.pointerCount > SINGLE_TOUCH || e2.pointerCount > SINGLE_TOUCH) false
                            else listener.onFling(event1, e2, velocityX, velocityY)
                        }
                    }
                    return false
                }
            })
            mGestureDetector?.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    mOnClickListener?.onClick(mImageView)
                    val displayRect = displayRect
                    val x = e.x
                    val y = e.y
                    mViewTapListener?.onViewTap(mImageView, x, y)
                    displayRect?.let {
                        // Check to see if the user tapped on the photo
                        if (displayRect.contains(x, y)) {
                            val xResult = ((x - displayRect.left) / displayRect.width())
                            val yResult = ((y - displayRect.top) / displayRect.height())
                            mPhotoTapListener?.onPhotoTap(mImageView, xResult, yResult)
                            return true
                        } else {
                            mOutsidePhotoTapListener?.onOutsidePhotoTap(mImageView)
                        }
                    }
                    return false
                }

                override fun onDoubleTap(ev: MotionEvent): Boolean {
                    try {
                        val scale: Float = scale
                        val x = ev.x
                        val y = ev.y
                        if (scale < mediumScale) {
                            setScale(mediumScale, x, y, true)
                        } else if (scale >= mediumScale && scale < maximumScale) {
                            setScale(maximumScale, x, y, true)
                        } else {
                            setScale(minimumScale, x, y, true)
                        }
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        // Can sometimes happen when getX() and getY() is called
                        e.printStackTrace()
                    }
                    return true
                }

                // Wait for the confirmed onDoubleTap() instead
                override fun onDoubleTapEvent(e: MotionEvent): Boolean = false
            })
        }
    }

    fun setOnDoubleTapListener(newOnDoubleTapListener: GestureDetector.OnDoubleTapListener?) {
        mGestureDetector?.setOnDoubleTapListener(newOnDoubleTapListener)
    }

    fun setOnScaleChangeListener(onScaleChangeListener: OnScaleChangedListener?) {
        mScaleChangeListener = onScaleChangeListener
    }

    fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener?) {
        mSingleFlingListener = onSingleFlingListener
    }

    fun setDisplayMatrix(finalMatrix: Matrix): Boolean {
        if (mImageView.getDrawable() == null) {
            return false
        }
        mSuppMatrix.set(finalMatrix)
        checkAndDisplayMatrix()
        return true
    }

    fun setBaseRotation(degrees: Float) {
        mBaseRotation = degrees % 360
        update()
        setRotationBy(mBaseRotation)
        checkAndDisplayMatrix()
    }

    fun setRotationTo(degrees: Float) {
        mSuppMatrix.setRotate(degrees % 360)
        checkAndDisplayMatrix()
    }

    fun setRotationBy(degrees: Float) {
        mSuppMatrix.postRotate(degrees % 360)
        checkAndDisplayMatrix()
    }

    val displayRect: RectF?
        get() {
            checkMatrixBounds()
            return getDisplayRect(drawMatrix)
        }

    var minimumScale: Float
        get() = mMinScale
        set(minimumScale) {
            checkZoomLevels(minimumScale, mMidScale, mMaxScale)
            mMinScale = minimumScale
        }
    var mediumScale: Float
        get() = mMidScale
        set(mediumScale) {
            checkZoomLevels(mMinScale, mediumScale, mMaxScale)
            mMidScale = mediumScale
        }
    var maximumScale: Float
        get() = mMaxScale
        set(maximumScale) {
            checkZoomLevels(mMinScale, mMidScale, maximumScale)
            mMaxScale = maximumScale
        }
    var scale: Float
        get() = sqrt((getValue(mSuppMatrix, Matrix.MSCALE_X).pow(2f) + getValue(mSuppMatrix, Matrix.MSKEW_Y).pow(2f)).toDouble()).toFloat()
        set(scale) {
            setScale(scale, animate = false)
        }
    var scaleType: ImageView.ScaleType
        get() = mScaleType
        set(scaleType) {
            if (isSupportedScaleType(scaleType) && scaleType != mScaleType) {
                mScaleType = scaleType
                update()
            }
        }

    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        // Update our base matrix, as the bounds have changed
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            updateBaseMatrix(mImageView.getDrawable())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        var handled = false
        if (isZoomEnabled && hasDrawable((v as ImageView))) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    val parent = v.getParent()
                    // First, disable the Parent from intercepting the touch event
                    parent?.requestDisallowInterceptTouchEvent(true)
                    // If we're flinging, and the user presses down, cancel fling
                    cancelFling()
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                    // If the user has zoomed less than min scale, zoom back to min scale
                    if (scale < mMinScale) {
                        displayRect?.let { rect ->
                            v.post(AnimatedZoomRunnable(scale, mMinScale, rect.centerX(), rect.centerY()))
                            handled = true
                        }
                    } else if (scale > mMaxScale) {
                        displayRect?.let { rect ->
                            v.post(AnimatedZoomRunnable(scale, mMaxScale, rect.centerX(), rect.centerY()))
                            handled = true
                        }
                    }
            }
            // Try the Scale/Drag detector
            mScaleDragDetector?.let { detector ->
                val wasScaling = detector.isScaling
                val wasDragging = detector.isDragging
                handled = detector.onTouchEvent(ev)
                val didntScale = !wasScaling && !detector.isScaling
                val didntDrag = !wasDragging && !detector.isDragging
                mBlockParentIntercept = didntScale && didntDrag
            }
            // Check to see if the user double tapped
            if (mGestureDetector != null && mGestureDetector?.onTouchEvent(ev) == true) {
                handled = true
            }
        } else if (ev.action == MotionEvent.ACTION_DOWN) {
            handled = mOnClickListener != null
        } else if (ev.action == MotionEvent.ACTION_UP) {
            mOnClickListener?.let {
                it.onClick(v)
                handled = true
            }
        }
        return handled
    }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        mAllowParentInterceptOnEdge = allow
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        checkZoomLevels(minimumScale, mediumScale, maximumScale)
        mMinScale = minimumScale
        mMidScale = mediumScale
        mMaxScale = maximumScale
    }

    fun setOnLongClickListener(listener: View.OnLongClickListener?) {
        mLongClickListener = listener
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        mOnClickListener = listener
    }

    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?) {
        mMatrixChangeListener = listener
    }

    fun setOnPhotoTapListener(listener: OnPhotoTapListener?) {
        mPhotoTapListener = listener
    }

    fun setOnOutsidePhotoTapListener(mOutsidePhotoTapListener: OnOutsidePhotoTapListener?) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener
    }

    fun setOnViewTapListener(listener: OnViewTapListener?) {
        mViewTapListener = listener
    }

    fun setOnViewDragListener(listener: OnViewDragListener?) {
        mOnViewDragListener = listener
    }

    fun setScale(
        scale: Float,
        focalX: Float = (mImageView.right / 2).toFloat(),
        focalY: Float = (mImageView.bottom / 2).toFloat(),
        animate: Boolean = false,
    ) {
        // Check to see if the scale is within bounds
        // 预览需要设置倍率为 0~1，因此不做倍率限制
        // if (scale < mMinScale || scale > mMaxScale) {
        //     throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
        // }
        if (animate) {
            mImageView.post(AnimatedZoomRunnable(this.scale, scale, focalX, focalY))
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY)
            checkAndDisplayMatrix()
        }
    }

    /**
     * Set the zoom interpolator
     *
     * @param interpolator the zoom interpolator
     */
    fun setZoomInterpolator(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    var isZoomable: Boolean
        get() = isZoomEnabled
        set(zoomable) {
            isZoomEnabled = zoomable
            update()
        }

    fun update() {
        if (isZoomEnabled) {
            // Update the base matrix using the current drawable
            updateBaseMatrix(mImageView.getDrawable())
        } else {
            // Reset the Matrix...
            resetMatrix()
        }
    }

    /**
     * Get the display matrix
     *
     * @param matrix target matrix to copy to
     */
    fun getDisplayMatrix(matrix: Matrix) {
        matrix.set(drawMatrix)
    }

    /**
     * Get the current support matrix
     */
    fun getSuppMatrix(matrix: Matrix) {
        matrix.set(mSuppMatrix)
    }

    private val drawMatrix: Matrix
        get() {
            imageMatrix.set(mBaseMatrix)
            imageMatrix.postConcat(mSuppMatrix)
            return imageMatrix
        }

    fun setZoomTransitionDuration(milliseconds: Int) {
        mZoomDuration = milliseconds
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    private fun resetMatrix() {
        mSuppMatrix.reset()
        setRotationBy(mBaseRotation)
        setImageViewMatrix(drawMatrix)
        checkMatrixBounds()
    }

    private fun setImageViewMatrix(matrix: Matrix) {
        mImageView.setImageMatrix(matrix)
        // Call MatrixChangedListener if needed
        if (mMatrixChangeListener != null) {
            getDisplayRect(matrix)?.let { rect ->
                mMatrixChangeListener?.onMatrixChanged(rect)
            }
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private fun checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(drawMatrix)
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private fun getDisplayRect(matrix: Matrix): RectF? {
        return mImageView.getDrawable()?.let { d ->
            mDisplayRect[0f, 0f, d.intrinsicWidth.toFloat()] = d.intrinsicHeight.toFloat()
            matrix.mapRect(mDisplayRect)
            mDisplayRect
        }
    }

    /**
     * Calculate Matrix for FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     */
    private fun updateBaseMatrix(drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        val viewWidth = getImageViewWidth(mImageView).toFloat()
        val viewHeight = getImageViewHeight(mImageView).toFloat()
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        mBaseMatrix.reset()
        val widthScale = viewWidth / drawableWidth
        val heightScale = viewHeight / drawableHeight
        when (mScaleType) {
            ImageView.ScaleType.CENTER -> {
                mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2f, (viewHeight - drawableHeight) / 2f)
            }

            ImageView.ScaleType.CENTER_CROP -> {
                val scale = max(widthScale.toDouble(), heightScale.toDouble()).toFloat()
                mBaseMatrix.postScale(scale, scale)
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeight * scale) / 2f)
            }

            ImageView.ScaleType.CENTER_INSIDE -> {
                val scale = min(1.0, min(widthScale.toDouble(), heightScale.toDouble())).toFloat()
                mBaseMatrix.postScale(scale, scale)
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeight * scale) / 2f)
            }

            else -> {
                var mTempSrc = RectF(0f, 0f, drawableWidth.toFloat(), drawableHeight.toFloat())
                val mTempDst = RectF(0f, 0f, viewWidth, viewHeight)
                if (mBaseRotation.toInt() % 180 != 0) {
                    mTempSrc = RectF(0f, 0f, drawableHeight.toFloat(), drawableWidth.toFloat())
                }
                when (mScaleType) {
                    ImageView.ScaleType.FIT_CENTER -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER)
                    ImageView.ScaleType.FIT_START -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START)
                    ImageView.ScaleType.FIT_END -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END)
                    ImageView.ScaleType.FIT_XY -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL)
                    else -> Unit
                }
            }
        }
        resetMatrix()
    }

    private fun checkMatrixBounds(): Boolean {
        val rect = getDisplayRect(drawMatrix) ?: return false
        val height = rect.height()
        val width = rect.width()
        var deltaX = 0f
        var deltaY = 0f
        val viewHeight = getImageViewHeight(mImageView)
        if (height <= viewHeight) {
            deltaY = when (mScaleType) {
                ImageView.ScaleType.FIT_START -> -rect.top
                ImageView.ScaleType.FIT_END -> viewHeight - height - rect.top
                else -> (viewHeight - height) / 2 - rect.top
            }

            // 只有图片高度等于 view 高度才设置为 VERTICAL_EDGE_BOTH
            verticalScrollEdge =
                if (height == viewHeight.toFloat()) VERTICAL_EDGE_BOTH
                else VERTICAL_EDGE_INSIDE
        } else if (rect.top > 0) {
            verticalScrollEdge = VERTICAL_EDGE_TOP
            deltaY = -rect.top
        } else if (rect.bottom < viewHeight) {
            verticalScrollEdge = VERTICAL_EDGE_BOTTOM
            deltaY = viewHeight - rect.bottom
        } else {
            verticalScrollEdge = VERTICAL_EDGE_OUTSIDE
        }
        val viewWidth = getImageViewWidth(mImageView)
        if (width <= viewWidth) {
            deltaX = when (mScaleType) {
                ImageView.ScaleType.FIT_START -> -rect.left
                ImageView.ScaleType.FIT_END -> viewWidth - width - rect.left
                else -> (viewWidth - width) / 2 - rect.left
            }

            // 只有图片宽度等于 view 宽度才设置为 HORIZONTAL_EDGE_BOTH
            horizontalScrollEdge =
                if (width == viewWidth.toFloat()) HORIZONTAL_EDGE_BOTH
                else HORIZONTAL_EDGE_INSIDE
        } else if (rect.left > 0) {
            horizontalScrollEdge = HORIZONTAL_EDGE_LEFT
            deltaX = -rect.left
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right
            horizontalScrollEdge = HORIZONTAL_EDGE_RIGHT
        } else {
            horizontalScrollEdge = HORIZONTAL_EDGE_OUTSIDE
        }
        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY)
        return true
    }

    private fun getImageViewWidth(imageView: ImageView): Int =
        imageView.width - imageView.getPaddingLeft() - imageView.getPaddingRight()

    private fun getImageViewHeight(imageView: ImageView): Int =
        imageView.height - imageView.paddingTop - imageView.paddingBottom

    private fun cancelFling() {
        mCurrentFlingRunnable?.cancelFling()
        mCurrentFlingRunnable = null
    }

    private inner class AnimatedZoomRunnable(
        private val mZoomStart: Float, private val mZoomEnd: Float,
        private val mFocalX: Float, private val mFocalY: Float
    ) : Runnable {

        private val mStartTime: Long = System.currentTimeMillis()

        override fun run() {
            val t = interpolate()
            val s = mZoomStart + t * (mZoomEnd - mZoomStart)
            val deltaScale: Float = s / scale
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY)
            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                postOnAnimation(mImageView, this)
            }
        }

        private fun interpolate(): Float {
            var t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration
            t = min(1.0, t.toDouble()).toFloat()
            t = mInterpolator.getInterpolation(t)
            return t
        }
    }

    private inner class FlingRunnable(context: Context) : Runnable {
        private val mScroller: OverScroller
        private var mCurrentX = 0
        private var mCurrentY = 0

        init {
            mScroller = OverScroller(context)
        }

        fun cancelFling() {
            mScroller.forceFinished(true)
        }

        fun fling(viewWidth: Int, viewHeight: Int, velocityX: Int, velocityY: Int) {
            val rect: RectF = displayRect ?: return
            val startX = Math.round(-rect.left)

            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int

            if (viewWidth < rect.width()) {
                minX = 0
                maxX = Math.round(rect.width() - viewWidth)
            } else {
                maxX = startX
                minX = maxX
            }

            val startY = Math.round(-rect.top)
            if (viewHeight < rect.height()) {
                minY = 0
                maxY = Math.round(rect.height() - viewHeight)
            } else {
                maxY = startY
                minY = maxY
            }

            mCurrentX = startX
            mCurrentY = startY
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0)
            }
        }

        override fun run() {
            if (mScroller.isFinished) {
                return  // remaining post that should not be handled
            }
            if (mScroller.computeScrollOffset()) {
                val newX = mScroller.currX
                val newY = mScroller.currY
                mSuppMatrix.postTranslate((mCurrentX - newX).toFloat(), (mCurrentY - newY).toFloat())
                checkAndDisplayMatrix()
                mCurrentX = newX
                mCurrentY = newY
                // Post On animation
                postOnAnimation(mImageView, this)
            }
        }
    }
}
