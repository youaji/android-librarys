@file:Suppress("unused")
package com.youaji.libs.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.*
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.text.inSpans

private const val IMAGE_SPAN_TEXT = "<img/>"
private const val SPACE_SPAN_TEXT = "<space/>"

/**
 * 设置大小
 */
inline fun SpannableStringBuilder.size(
    size: Float,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = size(size.toInt(), builderAction)

inline fun SpannableStringBuilder.size(
    size: Int,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(AbsoluteSizeSpan(size), builderAction)

/**
 * 设置模糊
 */
inline fun SpannableStringBuilder.blur(
    radius: Float,
    style: BlurMaskFilter.Blur = BlurMaskFilter.Blur.NORMAL,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = maskFilter(BlurMaskFilter(radius, style), builderAction)

/**
 *
 */
inline fun SpannableStringBuilder.maskFilter(
    filter: MaskFilter,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(MaskFilterSpan(filter), builderAction)

/**
 * 设置字体系列
 */
inline fun SpannableStringBuilder.fontFamily(
    family: String?,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(TypefaceSpan(family), builderAction)

/**
 * 设置字体
 */
inline fun SpannableStringBuilder.typeface(
    typeface: Typeface,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(TypefaceSpanCompat(typeface), builderAction)

/**
 * 	设置超链接
 */
inline fun SpannableStringBuilder.url(
    url: String,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(URLSpan(url), builderAction)

/**
 * 设置段落居中
 */
inline fun SpannableStringBuilder.alignCenter(
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = alignment(Layout.Alignment.ALIGN_CENTER, builderAction)

/**
 * 设置段落居右
 */
inline fun SpannableStringBuilder.alignOpposite(
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = alignment(Layout.Alignment.ALIGN_OPPOSITE, builderAction)

/**
 *
 */
inline fun SpannableStringBuilder.alignment(
    alignment: Layout.Alignment,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(AlignmentSpan.Standard(alignment), builderAction)

/**
 * 设置缩进
 */
inline fun SpannableStringBuilder.leadingMargin(
    first: Float,
    rest: Float = first,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = leadingMargin(first.toInt(), rest.toInt(), builderAction)

inline fun SpannableStringBuilder.leadingMargin(
    first: Int,
    rest: Int = first,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = inSpans(LeadingMarginSpan.Standard(first, rest), builderAction)

/**
 * 设置段落的列表标记
 */
inline fun SpannableStringBuilder.bullet(
    gapWidth: Float,
    @ColorInt color: Int? = null,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder = bullet(gapWidth.toInt(), color, builderAction)

inline fun SpannableStringBuilder.bullet(
    gapWidth: Int = BulletSpan.STANDARD_GAP_WIDTH,
    @ColorInt color: Int? = null,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder =
    inSpans(if (color == null) BulletSpan(gapWidth) else BulletSpan(gapWidth, color), builderAction)

/**
 * 设置段落的引用线颜色
 */
inline fun SpannableStringBuilder.quote(
    @ColorInt color: Int? = null,
    builderAction: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder =
    inSpans(if (color == null) QuoteSpan() else QuoteSpan(color), builderAction)

/**
 * 追加图片
 */
fun SpannableStringBuilder.append(
    drawable: Drawable,
    width: Int = drawable.intrinsicWidth,
    height: Int = drawable.intrinsicHeight
): SpannableStringBuilder {
    drawable.setBounds(0, 0, width, height)
    return inSpans(ImageSpan(drawable)) { append(IMAGE_SPAN_TEXT) }
}

fun SpannableStringBuilder.append(
    @DrawableRes resourceId: Int,
    context: Context = application
): SpannableStringBuilder = inSpans(ImageSpan(context, resourceId)) { append(IMAGE_SPAN_TEXT) }

fun SpannableStringBuilder.append(
    bitmap: Bitmap,
    context: Context = application
): SpannableStringBuilder = inSpans(ImageSpan(context, bitmap)) { append(IMAGE_SPAN_TEXT) }

/**
 * 追加可点击的文字
 */
fun SpannableStringBuilder.appendClickable(
    text: CharSequence?,
    @ColorInt color: Int? = null,
    isUnderlineText: Boolean = true,
    onClick: (View) -> Unit
): SpannableStringBuilder = inSpans(ClickableSpan(color, isUnderlineText, onClick)) { append(text) }

/**
 * 追加可点击的图片
 */
fun SpannableStringBuilder.appendClickable(
    drawable: Drawable,
    width: Int = drawable.intrinsicWidth,
    height: Int = drawable.intrinsicHeight,
    onClick: (View) -> Unit
): SpannableStringBuilder = inSpans(ClickableSpan(onClick = onClick)) { append(drawable, width, height) }

fun SpannableStringBuilder.appendClickable(
    @DrawableRes resourceId: Int,
    context: Context = application,
    onClick: (View) -> Unit
): SpannableStringBuilder = inSpans(ClickableSpan(onClick = onClick)) { append(resourceId, context) }

fun SpannableStringBuilder.appendClickable(
    bitmap: Bitmap,
    context: Context = application,
    onClick: (View) -> Unit
): SpannableStringBuilder = inSpans(ClickableSpan(onClick = onClick)) { append(bitmap, context) }

/**
 * 	追加空白
 */
fun SpannableStringBuilder.appendSpace(
    @FloatRange(from = 0.0) size: Float,
    @ColorInt color: Int = Color.TRANSPARENT
): SpannableStringBuilder = appendSpace(size.toInt(), color)

fun SpannableStringBuilder.appendSpace(
    @IntRange(from = 0) size: Int,
    @ColorInt color: Int = Color.TRANSPARENT
): SpannableStringBuilder = inSpans(SpaceSpan(size, color)) { append(SPACE_SPAN_TEXT) }

fun ClickableSpan(
    @ColorInt color: Int? = null,
    isUnderlineText: Boolean = true,
    onClick: (View) -> Unit,
): ClickableSpan = object : ClickableSpan() {
    override fun onClick(widget: View) = onClick(widget)

    override fun updateDrawState(ds: TextPaint) {
        ds.color = color ?: ds.linkColor
        ds.isUnderlineText = isUnderlineText
    }
}

class SpaceSpan constructor(private val width: Int, color: Int = Color.TRANSPARENT) : ReplacementSpan() {
    private val paint = Paint().apply {
        this.color = color
        style = Paint.Style.FILL
    }

    override fun getSize(
        paint: Paint, text: CharSequence?,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        fm: Paint.FontMetricsInt?
    ): Int = width

    override fun draw(
        canvas: Canvas, text: CharSequence?,
        @IntRange(from = 0) start: Int,
        @IntRange(from = 0) end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) = canvas.drawRect(x, top.toFloat(), x + width, bottom.toFloat(), this.paint)
}

class TypefaceSpanCompat(private val newType: Typeface) : TypefaceSpan(null) {
    override fun updateDrawState(ds: TextPaint) {
        ds.applyTypeFace(newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.applyTypeFace(newType)
    }

    private fun TextPaint.applyTypeFace(tf: Typeface) {
        val oldStyle: Int
        val old = typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            textSkewX = -0.25f
        }
        typeface = tf
    }
}
