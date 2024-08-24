@file:Suppress("unused")
package com.youaji.libs.widget.shine

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.youaji.libs.widget.R

/**
 * @author youaji
 * @since 2023/4/7
 */
open class ShineButton : PorterShapeImageView {

    private val tag = ShineButton::class.java.simpleName

    private var isChecked = false

    private var btnCheckedColor = 0
    private var btnUncheckedColor = 0
    private var btnCheckedDrawable: Drawable? = null
    private var btnUncheckedDrawable: Drawable? = null

    private var defaultWidth = 50
    private var defaultHeight = 50

    private var metrics = DisplayMetrics()

    private var activity: Activity? = null

    private var shineView: ShinePlusView? = null
    private var shakeAnimator: ValueAnimator? = null
    private var shineParams: ShinePlusView.ShineParams = ShinePlusView.ShineParams()

    private var checkedChangeListener: ((view: View?, checked: Boolean) -> Unit)? = null
    private var buttonClickListener: OnButtonClickListener? = null

    private var bottomHeight = 0
    private var realBottomHeight = 0

    var mFixDialog: Dialog? = null

    constructor(context: Context) : super(context) {
        if (context is Activity) {
            initByActivity(context)
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, shape: Drawable?) : super(context) {
        if (context is Activity) {
            initByActivity(context)
        }
        shape?.let { setShape(it) }
    }

    private fun init(context: Context, attrs: AttributeSet) {
        if (context is Activity) {
            initByActivity(context)
        }
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShineButton)
        btnCheckedColor = typedArray.getColor(R.styleable.ShineButton_shine_checked_color, Color.BLACK)
        btnUncheckedColor = typedArray.getColor(R.styleable.ShineButton_shine_unchecked_color, Color.GRAY)
        btnCheckedDrawable = typedArray.getDrawable(R.styleable.ShineButton_shine_checked_resource)
        btnUncheckedDrawable = typedArray.getDrawable(R.styleable.ShineButton_shine_unchecked_resource)
        shineParams.allowRandomColor = typedArray.getBoolean(R.styleable.ShineButton_shine_allow_random_color, false)
        shineParams.animDuration = typedArray.getInteger(R.styleable.ShineButton_shine_animation_duration, shineParams.animDuration.toInt()).toLong()
        shineParams.bigShineColor = typedArray.getColor(R.styleable.ShineButton_shine_big_shine_color, shineParams.bigShineColor)
        shineParams.clickAnimDuration = typedArray.getInteger(R.styleable.ShineButton_shine_click_animation_duration, shineParams.clickAnimDuration.toInt()).toLong()
        shineParams.enableFlashing = typedArray.getBoolean(R.styleable.ShineButton_shine_enable_flashing, false)
        shineParams.shineCount = typedArray.getInteger(R.styleable.ShineButton_shine_count, shineParams.shineCount)
        shineParams.shineDistanceMultiple = typedArray.getFloat(R.styleable.ShineButton_shine_distance_multiple, shineParams.shineDistanceMultiple)
        shineParams.shineTurnAngle = typedArray.getFloat(R.styleable.ShineButton_shine_turn_angle, shineParams.shineTurnAngle)
        shineParams.smallShineColor = typedArray.getColor(R.styleable.ShineButton_shine_small_shine_color, shineParams.smallShineColor)
        shineParams.smallShineOffsetAngle = typedArray.getFloat(R.styleable.ShineButton_shine_small_shine_offset_angle, shineParams.smallShineOffsetAngle)
        shineParams.shineSize = typedArray.getDimensionPixelSize(R.styleable.ShineButton_shine_size, shineParams.shineSize)
        shineParams.bigShineColor = typedArray.getColor(R.styleable.ShineButton_shine_sb_border_color, shineParams.sbBorderColor)
        typedArray.recycle()
        setUncheckedSrc()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calPixels()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        if (l is OnButtonClickListener) {
            super.setOnClickListener(l)
        } else {
            buttonClickListener?.setListener(l)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun calPixels() {
        if (activity != null) {
            activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
            val location = IntArray(2)
            getLocationInWindow(location)
            val visibleFrame = Rect()
            activity?.window?.decorView?.getWindowVisibleDisplayFrame(visibleFrame)
            realBottomHeight = visibleFrame.height() - location[1]
            bottomHeight = metrics.heightPixels - location[1]
        }
    }

    private fun onListenerUpdate(checked: Boolean) {
        checkedChangeListener?.invoke(this, checked)
    }

    private fun setCheckedSrc() {
        if (btnCheckedDrawable != null) {
            setSrcDrawable(btnCheckedDrawable!!, btnCheckedColor)
        } else {
            setSrcColor(btnCheckedColor)
        }
    }

    private fun setUncheckedSrc() {
        if (btnUncheckedDrawable != null) {
            setSrcDrawable(btnUncheckedDrawable!!, btnUncheckedColor)
        } else {
            setSrcColor(btnUncheckedColor)
        }
    }

    private fun setChecked(checked: Boolean, anim: Boolean, callBack: Boolean) {
        isChecked = checked
        if (checked) {
            setCheckedSrc()
            isChecked = true
            if (anim) showAnim()
            invalidate()
        } else {
            setUncheckedSrc()
            isChecked = false
            if (anim) setCancel()
            invalidate()
        }
        if (callBack) {
            onListenerUpdate(checked)
        }
    }

    private fun doShareAnim() {
        shakeAnimator = ValueAnimator.ofFloat(0.4f, 1f, 0.9f, 1f)
        shakeAnimator?.interpolator = LinearInterpolator()
        shakeAnimator?.duration = 500
        shakeAnimator?.startDelay = 180
        invalidate()
        shakeAnimator?.addUpdateListener { valueAnimator ->
            scaleX = valueAnimator.animatedValue as Float
            scaleY = valueAnimator.animatedValue as Float
        }
        shakeAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                setCheckedSrc()
            }

            override fun onAnimationEnd(animation: Animator) {
                if (isChecked) setCheckedSrc() else setUncheckedSrc()
            }

            override fun onAnimationCancel(animation: Animator) {
                setUncheckedSrc()
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

        })
        shakeAnimator?.start()
    }

    fun initByActivity(activity: Activity?) {
        this.activity = activity
        buttonClickListener = OnButtonClickListener()
        setOnClickListener(buttonClickListener)
    }

    fun setFixDialog(fixDialog: Dialog) {
        mFixDialog = fixDialog
    }

    fun setCheckedColor(checkedColor: Int) {
        this.btnCheckedColor = checkedColor
    }

    fun setUncheckedColor(uncheckedColor: Int) {
        this.btnUncheckedColor = uncheckedColor
    }

    fun setCheckedDrawable(drawable: Drawable?) {
        this.btnCheckedDrawable = drawable
    }

    fun setUncheckedDrawable(drawable: Drawable?) {
        this.btnUncheckedDrawable = drawable
    }

    fun setChecked(checked: Boolean) {
        setChecked(checked, anim = false, callBack = false)
    }

    fun setChecked(checked: Boolean, anim: Boolean) {
        setChecked(checked, anim, true)
    }

    fun getCheckedColor(): Int = btnCheckedColor

    fun getUncheckColor(): Int = btnUncheckedColor

    fun isChecked(): Boolean = isChecked

    fun setCancel() {
        setUncheckedSrc()
        shakeAnimator?.end()
        shakeAnimator?.cancel()
    }

    fun getBottomHeight(real: Boolean): Int =
        if (real) realBottomHeight
        else bottomHeight

    fun setAllowRandomColor(allowRandomColor: Boolean) {
        shineParams.allowRandomColor = allowRandomColor
    }

    fun setAnimDuration(durationMs: Long) {
        shineParams.animDuration = durationMs
    }

    fun setBigShineColor(color: Int) {
        shineParams.bigShineColor = color
    }

    fun setClickAnimDuration(durationMs: Long) {
        shineParams.clickAnimDuration = durationMs
    }

    fun enableFlashing(enable: Boolean) {
        shineParams.enableFlashing = enable
    }

    fun setShineCount(count: Int) {
        shineParams.shineCount = count
    }

    fun setShineDistanceMultiple(multiple: Float) {
        shineParams.shineDistanceMultiple = multiple
    }

    fun setShineTurnAngle(angle: Float) {
        shineParams.shineTurnAngle = angle
    }

    fun setSmallShineColor(color: Int) {
        shineParams.smallShineColor = color
    }

    fun setSmallShineOffAngle(angle: Float) {
        shineParams.smallShineOffsetAngle = angle
    }

    fun setShineSize(size: Int) {
        shineParams.shineSize = size
    }

    fun setOnCheckStateChangeListener(listener: ((view: View?, checked: Boolean) -> Unit)?) {
        this.checkedChangeListener = listener
    }

    fun showAnim() {
        if (activity != null) {
            shineView = ShinePlusView(activity, this, shineParams)
            val rootView: ViewGroup
            if (mFixDialog != null && mFixDialog?.window != null) {
                rootView = mFixDialog?.window?.decorView as ViewGroup
                val innerView = rootView.findViewById<View>(android.R.id.content)
                rootView.addView(shineView, ViewGroup.LayoutParams(innerView.width, innerView.height + 100))
            } else {
                rootView = activity?.window?.decorView as ViewGroup
                rootView.addView(shineView, ViewGroup.LayoutParams(rootView.width, rootView.height + 100))
            }
            doShareAnim()
        } else {
            Log.e(tag, "Please init.")
        }
    }

    fun removeView(view: View?) {
        if (activity != null) {
            Log.e(tag, "Removed")
            val rootView2 = activity?.window?.decorView as ViewGroup
            rootView2.removeView(view)
        } else {
            Log.e(tag, "Please init.")
        }
    }

    inner class OnButtonClickListener : OnClickListener {

        private var clickListener: OnClickListener? = null

        constructor()
        constructor(l: OnClickListener?) {
            this.clickListener = l
        }

        override fun onClick(view: View?) {
//            if (!isChecked) {
//                isChecked = true
//                showAnim()
////                showVib()
//            } else {
//                isChecked = false
//                setCancel()
//            }
//            onListenerUpdate(isChecked)
            clickListener?.onClick(view)
        }

        fun setListener(listener: OnClickListener?) {
            this.clickListener = listener
        }
    }


//    private fun showVib() {
//        val vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
//        vibrator.vibrate(100)
//    }

}