package com.youaji.libs.picture.viewer.custom

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
//import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatImageView

/**
 * A zoomable ImageView. See [PhotoViewAttacher] for most of the details on how the zooming is accomplished
 * 本来打算引用库的，但是有些逻辑不修改源码，难以实现。仅限本框架使用，有些逻辑只适用于当前库
 */
@Suppress("unused")
//@RestrictTo(RestrictTo.Scope.LIBRARY)
open class PhotoView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attr, defStyle) {

    /**
     * Get the current [PhotoViewAttacher] for this view.
     * Be wary of holding on to references to this attacher,
     * as it has a reference to this view, which,
     * if a reference is held in the wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    protected var attacher: PhotoViewAttacher = PhotoViewAttacher(this)
    private var pendingScaleType: ScaleType? = null

    var isZoomable: Boolean
        get() = attacher.isZoomable
        set(zoomable) {
            attacher.isZoomable = zoomable
        }
    val displayRect: RectF?
        get() = attacher.displayRect

    var minimumScale: Float
        get() = attacher.minimumScale
        set(minimumScale) {
            attacher.minimumScale = minimumScale
        }
    var mediumScale: Float
        get() = attacher.mediumScale
        set(mediumScale) {
            attacher.mediumScale = mediumScale
        }
    var maximumScale: Float
        get() = attacher.maximumScale
        set(maximumScale) {
            attacher.maximumScale = maximumScale
        }

    val scale: Float
        get() = attacher.scale

    init {
        // We always pose as a Matrix scale type, though we can change to another scale type
        // via the attacher
        super.setScaleType(ScaleType.MATRIX)
        // apply the previously applied scale type
        pendingScaleType?.let {
            scaleType = it
            pendingScaleType = null
        }
    }

    override fun getScaleType(): ScaleType {
        return attacher.scaleType
    }

    override fun getImageMatrix(): Matrix {
        return attacher.imageMatrix
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        attacher.setOnLongClickListener(l)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        attacher.setOnClickListener(l)
    }

    override fun setScaleType(scaleType: ScaleType) {
//        if (attacher == null) {
//            pendingScaleType = scaleType
//        } else {
        attacher.scaleType = scaleType
//        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // setImageBitmap calls through to this method
        attacher.update()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        attacher.update()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        attacher.update()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            attacher.update()
        }
        return changed
    }

    fun setRotationTo(rotationDegree: Float) {
        attacher.setRotationTo(rotationDegree)
    }

    fun setRotationBy(rotationDegree: Float) {
        attacher.setRotationBy(rotationDegree)
    }

    fun getDisplayMatrix(matrix: Matrix) {
        attacher.getDisplayMatrix(matrix)
    }

    fun setDisplayMatrix(finalRectangle: Matrix): Boolean {
        return attacher.setDisplayMatrix(finalRectangle)
    }

    fun getSuppMatrix(matrix: Matrix) {
        attacher.getSuppMatrix(matrix)
    }

    fun setSuppMatrix(matrix: Matrix): Boolean {
        return attacher.setDisplayMatrix(matrix)
    }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        attacher.setAllowParentInterceptOnEdge(allow)
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        attacher.setScaleLevels(minimumScale, mediumScale, maximumScale)
    }

    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?) {
        attacher.setOnMatrixChangeListener(listener)
    }

    fun setOnPhotoTapListener(listener: OnPhotoTapListener?) {
        attacher.setOnPhotoTapListener(listener)
    }

    fun setOnOutsidePhotoTapListener(listener: OnOutsidePhotoTapListener?) {
        attacher.setOnOutsidePhotoTapListener(listener)
    }

    fun setOnViewTapListener(listener: OnViewTapListener?) {
        attacher.setOnViewTapListener(listener)
    }

    fun setOnViewDragListener(listener: OnViewDragListener?) {
        attacher.setOnViewDragListener(listener)
    }

    fun setScale(scale: Float) {
        attacher.setScale(scale)
    }

    fun setScale(scale: Float, animate: Boolean) {
        attacher.setScale(scale, animate = animate)
    }

    fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        attacher.setScale(scale, focalX, focalY, animate)
    }

    fun setZoomTransitionDuration(milliseconds: Int) {
        attacher.setZoomTransitionDuration(milliseconds)
    }

    fun setOnDoubleTapListener(onDoubleTapListener: GestureDetector.OnDoubleTapListener?) {
        attacher.setOnDoubleTapListener(onDoubleTapListener)
    }

    open fun setOnScaleChangeListener(onScaleChangedListener: OnScaleChangedListener?) {
        attacher.setOnScaleChangeListener(onScaleChangedListener)
    }

    fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener?) {
        attacher.setOnSingleFlingListener(onSingleFlingListener)
    }
}
