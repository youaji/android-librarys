@file:Suppress("unused")
package com.youaji.libs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import java.lang.reflect.Method

/**
 * @author youaji
 * @since 2023/6/21
 */
class YouajiImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val pressPaint: Paint
    private var width = 0
    private var height = 0

    // default bitmap config
    private val bitmapConfig = Bitmap.Config.ARGB_8888
    private val colorDrawableDimension = 1

    // border color
    private var borderColor = 0

    // width of border
    private var borderWidth = 0

    // alpha when pressed
    private var pressAlpha = 0

    // color when pressed
    private var pressColor = 0

    // radius
    private var radius = 0

    // rectangle or round, 1 is circle, 2 is rectangle
    private var shapeType = ShapeType.NONE

    init {
        // init the value
        borderWidth = 0
        borderColor = -0x22000001
        pressAlpha = 0x42
        pressColor = 0x42000000
        radius = 16
        shapeType = ShapeType.NONE

        // get attribute of AvatarImageView
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.YouajiImageView)
            borderColor = array.getColor(R.styleable.YouajiImageView_image_border_color, borderColor)
            borderWidth = array.getDimensionPixelOffset(R.styleable.YouajiImageView_image_border_width, borderWidth)
            pressAlpha = array.getInteger(R.styleable.YouajiImageView_image_press_alpha, pressAlpha)
            pressColor = array.getColor(R.styleable.YouajiImageView_image_press_color, pressColor)
            radius = array.getDimensionPixelOffset(R.styleable.YouajiImageView_image_radius, radius)
            val shapeTypeInt = array.getInteger(R.styleable.YouajiImageView_image_shape_type, shapeType.ordinal)
            shapeType = when (shapeTypeInt) {
                1 -> ShapeType.ROUND
                2 -> ShapeType.RECTANGLE
                else -> ShapeType.NONE
            }
            array.recycle()
        }

        // set paint when pressed
        pressPaint = Paint()
        pressPaint.isAntiAlias = true
        pressPaint.style = Paint.Style.FILL
        pressPaint.color = pressColor
        pressPaint.alpha = 0
        pressPaint.flags = Paint.ANTI_ALIAS_FLAG
        isDrawingCacheEnabled = true
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        if (shapeType == ShapeType.NONE) {
            super.onDraw(canvas)
            return
        }
        val drawable = drawable ?: return
        // the width and height is in xml file
        if (getWidth() == 0 || getHeight() == 0) {
            return
        }

        val bitmap = getBitmapFromDrawable(drawable)
        bitmap?.let { drawDrawable(canvas, bitmap) }
        if (isClickable) {
            drawPress(canvas)
        }
        drawBorder(canvas)
    }

    /** monitor the size change */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
    }

    /** monitor if touched */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressPaint.alpha = pressAlpha
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                pressPaint.alpha = 0
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {}
            else -> {
                pressPaint.alpha = 0
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * draw Rounded Rectangle
     *
     * @param canvas
     * @param bitmap
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun drawDrawable(canvas: Canvas, bitmap: Bitmap) {
        var b = bitmap
        val paint = Paint()
        paint.color = -0x1
        paint.isAntiAlias = true //smooths out the edges of what is being drawn
        val xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // set flags
            val saveFlags: Int = (
                    CanvasLegacy.MATRIX_SAVE_FLAG
                            or CanvasLegacy.CLIP_SAVE_FLAG
                            or CanvasLegacy.HAS_ALPHA_LAYER_SAVE_FLAG
                            or CanvasLegacy.FULL_COLOR_LAYER_SAVE_FLAG
                            or CanvasLegacy.CLIP_TO_LAYER_SAVE_FLAG
                    )
            CanvasLegacy.saveLayer(canvas, 0f, 0f, width.toFloat(), height.toFloat(), null, saveFlags)
        } else {
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        }
        if (shapeType == ShapeType.ROUND) {
            canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                (width / 2 - 1).toFloat(),
                paint
            )
        } else if (shapeType == ShapeType.RECTANGLE) {
            val rectF = RectF(
                1f,
                1f,
                (getWidth() - 1).toFloat(),
                (getHeight() - 1).toFloat()
            )
            canvas.drawRoundRect(
                rectF,
                (radius + 1).toFloat(),
                (radius + 1).toFloat(),
                paint
            )
        }
        paint.xfermode = xfermode
        val scaleWidth = getWidth().toFloat() / b.width
        val scaleHeight = getHeight().toFloat() / b.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        // bitmap scale
        b = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
        canvas.drawBitmap(b, 0f, 0f, paint)
        canvas.restore()
    }

    /** draw the effect when pressed */
    private fun drawPress(canvas: Canvas) {
        // check is rectangle or circle
        if (shapeType == ShapeType.ROUND) {
            canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                (width / 2 - 1).toFloat(),
                pressPaint,
            )
        } else if (shapeType == ShapeType.RECTANGLE) {
            val rectF = RectF(
                1f,
                1f,
                (width - 1).toFloat(),
                (height - 1).toFloat(),
            )
            canvas.drawRoundRect(
                rectF,
                (radius + 1).toFloat(),
                (radius + 1).toFloat(),
                pressPaint,
            )
        }
    }

    /** draw customized border */
    private fun drawBorder(canvas: Canvas) {
        if (borderWidth > 0) {
            val paint = Paint()
            paint.strokeWidth = borderWidth.toFloat()
            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            paint.isAntiAlias = true
            // check is rectangle or circle
            if (shapeType == ShapeType.ROUND) {
                canvas.drawCircle(
                    (width / 2).toFloat(),
                    (height / 2).toFloat(),
                    ((width - borderWidth) / 2).toFloat(),
                    paint
                )
            } else if (shapeType == ShapeType.RECTANGLE) {
                val rectF = RectF(
                    (borderWidth / 2).toFloat(),
                    (borderWidth / 2).toFloat(),
                    (getWidth() - borderWidth / 2).toFloat(),
                    (getHeight() - borderWidth / 2).toFloat()
                )
                canvas.drawRoundRect(
                    rectF,
                    radius.toFloat(),
                    radius.toFloat(),
                    paint
                )
            }
        }
    }

    /** @return */
    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var bitmap: Bitmap?
        val width = drawable.intrinsicWidth.coerceAtLeast(2)
        val height = drawable.intrinsicHeight.coerceAtLeast(2)
        try {
            bitmap = Bitmap.createBitmap(width, height, bitmapConfig)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            bitmap = null
        }
        return bitmap
    }

    /** set border color */
    fun setBorderColor(borderColor: Int) {
        this.borderColor = borderColor
        invalidate()
    }

    /** set border width */
    fun setBorderWidth(borderWidth: Int) {
        this.borderWidth = borderWidth
    }

    /** set alpha when pressed */
    fun setPressAlpha(pressAlpha: Int) {
        this.pressAlpha = pressAlpha
    }

    /** set color when pressed */
    fun setPressColor(pressColor: Int) {
        this.pressColor = pressColor
    }

    /** set radius */
    fun setRadius(radius: Int) {
        this.radius = radius
        invalidate()
    }

    /** set shape type */
    fun setShapeType(shapeType: ShapeType) {
        this.shapeType = shapeType
        invalidate()
    }

    /** 形状类型 */
    enum class ShapeType {
        /**方形*/
        NONE,

        /**圆型*/
        ROUND,

        /**方形*/
        RECTANGLE,
    }
}

@SuppressWarnings("JavaReflectionMemberAccess")
object CanvasLegacy {
    var MATRIX_SAVE_FLAG = 0
    var CLIP_SAVE_FLAG = 0
    var HAS_ALPHA_LAYER_SAVE_FLAG = 0
    var FULL_COLOR_LAYER_SAVE_FLAG = 0
    var CLIP_TO_LAYER_SAVE_FLAG = 0
    private var SAVE: Method? = null

    init {
        try {
            MATRIX_SAVE_FLAG = Canvas::class.java.getField("MATRIX_SAVE_FLAG")[null] as Int
            CLIP_SAVE_FLAG = Canvas::class.java.getField("CLIP_SAVE_FLAG")[null] as Int
            HAS_ALPHA_LAYER_SAVE_FLAG = Canvas::class.java.getField("HAS_ALPHA_LAYER_SAVE_FLAG")[null] as Int
            FULL_COLOR_LAYER_SAVE_FLAG = Canvas::class.java.getField("FULL_COLOR_LAYER_SAVE_FLAG")[null] as Int
            CLIP_TO_LAYER_SAVE_FLAG = Canvas::class.java.getField("CLIP_TO_LAYER_SAVE_FLAG")[null] as Int
            SAVE = Canvas::class.java.getMethod(
                "saveLayer",
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                Paint::class.java,
                Int::class.javaPrimitiveType
            )
        } catch (e: Throwable) {
            throw sneakyThrow(e)
        }
    }

    fun saveLayer(canvas: Canvas?, left: Float, top: Float, right: Float, bottom: Float, paint: Paint?, saveFlags: Int) {
        try {
            SAVE?.invoke(canvas, left, top, right, bottom, paint, saveFlags)
        } catch (e: Throwable) {
            throw sneakyThrow(e)
        }
    }

    private fun sneakyThrow(t: Throwable): RuntimeException = sneakyThrow0(t)

    @SuppressWarnings("unchecked")
    private fun <T : Throwable> sneakyThrow0(t: Throwable): T {
        @Suppress("UNCHECKED_CAST")
        throw t as T
    }
}