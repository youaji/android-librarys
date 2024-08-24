package com.youaji.libs.picture.viewer

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatImageView

/**
 * 仅用于辅助加载，获取图片
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class PreloadImageView @JvmOverloads constructor(
    context: Context, attr: AttributeSet? = null, defStyle: Int = 0
) : AppCompatImageView(context, attr, defStyle) {

    private var listener: DrawableLoadListener? = null

    override fun onDraw(canvas: Canvas) {}
    override fun setImageDrawable(drawable: Drawable?) {
        listener?.onLoad(drawable)
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
    }

    fun setDrawableLoadListener(listener: DrawableLoadListener?) {
        this.listener = listener
    }

    fun interface DrawableLoadListener {
        fun onLoad(drawable: Drawable?)
    }
}
