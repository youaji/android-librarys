package com.youaji.libs.widget.stepper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.youaji.libs.widget.R;

import java.lang.ref.WeakReference;

@SuppressWarnings("unused")
public class Stepper extends RelativeLayout implements View.OnTouchListener {
    private OnStepperChangeListener listener;
    private TextView tvStepperContent;
    private ImageView ivStepperMinus, ivStepperPlus;
    public static int ANIMATION_DURATION = 300;//恢复动画时间
    public boolean inAnimation = false;//动画中,不能进行滑动
    private UpdateRunnable updateRunnable;
    private boolean stepTouch = false;//是否按着，判断是否还要继续更新数值和界面

    //按下后多少间隔触发快速改变模式
    private static final long STEP_SPEED_CHANGE_DURATION = 1000;
    private static final long UPDATE_DURATION_SLOW = 300;//数值更新频率-慢
    private static final long UPDATE_DURATION_FAST = 100;//数值更新频率-快
    private int valueSlowStep = 1;//慢速递增值 步长

    private boolean hasStepperContentLeft = false;
    //按下时间
    private long startTime = 0;


    //当前状态
    private int status = STATUS_NORMAL;
    private static final int STATUS_MIMNUS = -1;
    private static final int STATUS_PLUS = 1;
    private static final int STATUS_NORMAL = 0;
    //当前模式
    private Mode mode = Mode.AUTO;

    public enum Mode {
        AUTO(0), CUSTOM(1);
        private final int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Mode valueOf(int value) {    //    手写的从int到enum的转换函数
            switch (value) {
                case 0:
                    return AUTO;
                case 1:
                    return CUSTOM;
            }
            return AUTO;
        }
    }

    private int value = 0;
    private int minValue = 0;
    private int maxValue = 100;

    public Stepper(Context context) {
        this(context, null);
    }

    public Stepper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews(Context context, AttributeSet attrs) {

        LayoutInflater.from(context).inflate(R.layout.libs_widget_stepper, this, true);
        tvStepperContent = (TextView) findViewById(R.id.tvStepperContent);
        ivStepperMinus = (ImageView) findViewById(R.id.ivStepperMinus);
        ivStepperPlus = (ImageView) findViewById(R.id.ivStepperPlus);

        String text = "";
        Drawable background = null;
        Drawable contentBackground = null;
        Drawable leftButtonResources = null;
        Drawable rightButtonResources = null;
        Drawable leftButtonBackground = null;
        Drawable rightButtonBackground = null;

        int contentTextColor = ContextCompat.getColor(context, R.color.libs_widget_stepper_text);
        float contentTextSize = 0;
        float leftButtonWidth = 0;
        float rightButtonWidth = 0;
        float leftButtonPadding = 0;
        float rightButtonPadding = 0;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Stepper);
            int modeValue = a.getInt(R.styleable.Stepper_stepper_mode, Mode.AUTO.getValue());
            mode = Mode.valueOf(modeValue);
            minValue = a.getInt(R.styleable.Stepper_stepper_min, minValue);
            maxValue = a.getInt(R.styleable.Stepper_stepper_max, maxValue);
            value = valueRangeCheck(a.getInt(R.styleable.Stepper_stepper_value, value));
            valueSlowStep = a.getInt(R.styleable.Stepper_stepper_step, valueSlowStep);
            if (valueSlowStep <= 0) valueSlowStep = 1;
            text = a.getString(R.styleable.Stepper_stepper_text);

            background = a.getDrawable(R.styleable.Stepper_stepper_background);

            leftButtonWidth = a.getDimensionPixelOffset(R.styleable.Stepper_stepper_leftButtonWidth, 0);
            rightButtonWidth = a.getDimensionPixelOffset(R.styleable.Stepper_stepper_rightButtonWidth, 0);
            leftButtonPadding = a.getDimensionPixelOffset(R.styleable.Stepper_stepper_leftButtonPadding, 0);
            rightButtonPadding = a.getDimensionPixelOffset(R.styleable.Stepper_stepper_rightButtonPadding, 0);
            leftButtonResources = a.getDrawable(R.styleable.Stepper_stepper_leftButtonResources);
            rightButtonResources = a.getDrawable(R.styleable.Stepper_stepper_rightButtonResources);
            leftButtonBackground = a.getDrawable(R.styleable.Stepper_stepper_leftButtonBackground);
            rightButtonBackground = a.getDrawable(R.styleable.Stepper_stepper_rightButtonBackground);

            contentBackground = a.getDrawable(R.styleable.Stepper_stepper_contentBackground);
            contentTextColor = a.getColor(R.styleable.Stepper_stepper_contentTextColor, contentTextColor);
            contentTextSize = a.getDimension(R.styleable.Stepper_stepper_contentTextSize, 0);

            a.recycle();
        }

        if (background != null) {
            setBackground(background);
        } else {
            setContentBackground(R.color.libs_widget_stepper_button_press);
        }
        Log.d("sssssssssss", "leftButtonWidth: "+leftButtonWidth +" rightButtonWidth:"+rightButtonWidth);
        if (leftButtonWidth > 0) {
            ViewGroup.LayoutParams layoutParams = ivStepperMinus.getLayoutParams();
            layoutParams.width = (int) leftButtonWidth;
            layoutParams.height = (int) leftButtonWidth;
            ivStepperMinus.setLayoutParams(layoutParams);
        }
        if (rightButtonWidth > 0) {
            ViewGroup.LayoutParams layoutParams = ivStepperPlus.getLayoutParams();
            layoutParams.width = (int) rightButtonWidth;
            layoutParams.height = (int) rightButtonWidth;
            ivStepperPlus.setLayoutParams(layoutParams);
        }
        if (leftButtonPadding > 0)
            ivStepperMinus.setPadding((int) leftButtonPadding, (int) leftButtonPadding, (int) leftButtonPadding, (int) leftButtonPadding);
        if (rightButtonPadding > 0)
            ivStepperPlus.setPadding((int) rightButtonPadding, (int) rightButtonPadding, (int) rightButtonPadding, (int) rightButtonPadding);

        if (leftButtonResources != null)
            setLeftButtonResources(leftButtonResources);
        if (rightButtonResources != null)
            setRightButtonResources(rightButtonResources);
        if (leftButtonBackground != null)
            ivStepperMinus.setBackground(leftButtonBackground);
        if (rightButtonBackground != null)
            ivStepperPlus.setBackground(rightButtonBackground);

        if (contentBackground != null)
            setContentBackground(contentBackground);

        tvStepperContent.setTextColor(contentTextColor);

        if (contentTextSize > 0)
            setContentTextSize(contentTextSize);

        if (mode == Mode.AUTO)//AUTO模式，写数值到滑动条上
            tvStepperContent.setText(String.valueOf(value));
        else
            tvStepperContent.setText(text);

        //设置后onclick产生的点击状态会失效
        ivStepperMinus.setOnTouchListener(this);
        ivStepperPlus.setOnTouchListener(this);
        setOnTouchListener(this);

        updateRunnable = new UpdateRunnable(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stepTouch = true;
                postDelayed(updateRunnable, UPDATE_DURATION_SLOW);
                //非按钮则记录位置
                //按下的初始x值
                float startX = event.getX();
                initStartStepperContentLeft();
                startTime = System.currentTimeMillis();
                //如果是两边的按钮，分别设置为点击状态
                if (v == ivStepperMinus) {
                    ivStepperMinus.setPressed(true);
                    status = STATUS_MIMNUS;
                    break;
                } else if (v == ivStepperPlus) {
                    ivStepperPlus.setPressed(true);
                    status = STATUS_PLUS;
                    break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //是按钮则不能移动,恢复位置的动画中也不能移动
                if (v == ivStepperMinus || v == ivStepperPlus || inAnimation) break;
                //非按钮则进行移动
//                float moveX = event.getX() - startX;
//                float x = moveX + startStepperContentLeft;
//                moveStepperContent(x);
//                moveEffectStatus(moveX);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stepTouch = false;
                //如果是两边的按钮，分别设置为点击状态
                if (v == ivStepperMinus) {
                    ivStepperMinus.setPressed(false);
                    break;
                } else if (v == ivStepperPlus) {
                    ivStepperPlus.setPressed(false);
                    break;
                }
                restoreStepperContent();
                break;
        }
        return true;
    }

    private void initStartStepperContentLeft() {
        if (hasStepperContentLeft) return;
        hasStepperContentLeft = true;
        float startStepperContentLeft = tvStepperContent.getLeft();
    }

    /**
     * 中间滑条恢复原位置
     */
    private void restoreStepperContent() {
//        if (inAnimation) return;
//        inAnimation = true;
//        ValueAnimator restoreTranslateAnimation = ValueAnimator.ofFloat(tvStepperContent.getLeft(), (int) startStepperContentLeft);
//        restoreTranslateAnimation.setDuration(ANIMATION_DURATION);
//        restoreTranslateAnimation.addListener(this);
//        restoreTranslateAnimation.addUpdateListener(this);
//        restoreTranslateAnimation.setInterpolator(new AccelerateInterpolator());
//        restoreTranslateAnimation.start();
    }

    /**
     * 移动位置
     */
    private void moveStepperContent(float x) {
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int) x;
        //限制子控件移动必须在视图范围内
        if (params.leftMargin < 0 || (params.leftMargin + tvStepperContent.getWidth()) > getWidth())
            return;
        params.topMargin = 0;
        params.width = tvStepperContent.getWidth();
        params.height = tvStepperContent.getHeight();

        tvStepperContent.setLayoutParams(params);
    }

//    @Override
//    public void onAnimationUpdate(ValueAnimator animation) {
//        Float value = (Float) animation.getAnimatedValue();
//        moveStepperContent(value);
//    }

    private void moveEffectStatus(float x) {
        int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        if (x > scaledTouchSlop) {
            status = STATUS_PLUS;
        } else if (x < -scaledTouchSlop) {
            status = STATUS_MIMNUS;
        } else {
            status = STATUS_NORMAL;
        }
    }

    private int getNextValue() {
        switch (status) {
            case STATUS_MIMNUS:
                return value - valueSlowStep;
            case STATUS_PLUS:
                return value + valueSlowStep;
            case STATUS_NORMAL:
                return value;
        }
        return value;
    }

    private void updateUI() {
        int nextValue = getNextValue();
        //判断是否在范围之内
        if (nextValue < minValue) {
            nextValue = minValue;
        }
        if (nextValue > maxValue) {
            nextValue = maxValue;
        }
        value = nextValue;
        if (mode == Mode.AUTO)//AUTO模式，写数值到滑动条上
            tvStepperContent.setText(String.valueOf(value));
        if (listener != null)
            listener.onValueChange(this, value);
        if (stepTouch)
            postDelayed(updateRunnable, (System.currentTimeMillis() - startTime > STEP_SPEED_CHANGE_DURATION) ? UPDATE_DURATION_FAST : UPDATE_DURATION_SLOW);
    }

//    @Override
//    public void onAnimationStart(Animator animation) {
//
//    }
//
//    @Override
//    public void onAnimationEnd(Animator animation) {
//        inAnimation = false;
//    }
//
//    @Override
//    public void onAnimationCancel(Animator animation) {
//
//    }
//
//    @Override
//    public void onAnimationRepeat(Animator animation) {
//
//    }


    static class UpdateRunnable implements Runnable {
        private final WeakReference<Stepper> view;

        public UpdateRunnable(Stepper view) {
            this.view = new WeakReference<>(view);
        }

        public void run() {
            Stepper stepper = view.get();
            if (stepper != null) {
                stepper.updateUI();
            }
        }
    }

    public void setOnValueChangeListener(OnStepperChangeListener listener) {
        this.listener = listener;
    }

    /**
     * @return 返回当前模式类型
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * 模式设置 AUTO(0) 数值写到滑动条, CUSTOM(1) 自定义文字;
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * @return 获取当前值
     */
    public int getValue() {
        return value;
    }

    /**
     * 设置当前值
     *
     * @param value 当前值
     */
    public void setValue(int value) {
        this.value = valueRangeCheck(value);
        if (mode == Mode.AUTO)//AUTO模式，写数值到滑动条上
            tvStepperContent.setText(String.valueOf(value));
    }

    public int valueRangeCheck(int value) {
        if (value > maxValue) value = maxValue;
        else if (value < minValue) value = minValue;
        return value;
    }

    /**
     * @return 获取最小值
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * 设置最小值
     */
    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    /**
     * @return 获取最大值
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * 设置最大值
     */
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * @return 获取步长
     */
    public int getValueSlowStep() {
        return valueSlowStep;
    }

    /**
     * 设置步长
     */
    public void setValueSlowStep(int valueSlowStep) {
        this.valueSlowStep = valueSlowStep;
    }

    /**
     * 设置中间内容滑条颜色
     */
    public void setContentBackground(int resId) {
        tvStepperContent.setBackgroundResource(resId);
    }

    public void setContentBackground(Drawable drawable) {
        tvStepperContent.setBackground(drawable);
    }

    /**
     * 设置中间内容文字颜色
     */
    public void setContentTextColor(int resId) {
        tvStepperContent.setTextColor(ContextCompat.getColor(getContext(), resId));
    }

    /**
     * 设置中间内容文字,mode需为Custom才支持
     */
    public void setText(String text) {
        tvStepperContent.setText(text);
    }

    /**
     * 设置中间内容文字大小
     */
    public void setContentTextSize(float px) {
        tvStepperContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
    }

    /**
     * 设置按钮背景
     */
    public void setButtonBackGround(int resId) {
        ivStepperMinus.setBackgroundResource(resId);
        ivStepperPlus.setBackgroundResource(resId);
    }

    /**
     * 设置按钮资源
     */
    public void setLeftButtonResources(int resId) {
        ivStepperMinus.setImageResource(resId);
    }

    /**
     * 设置按钮资源
     */
    public void setLeftButtonResources(Drawable drawable) {
        ivStepperMinus.setImageDrawable(drawable);
    }

    /**
     * 设置按钮资源
     */
    public void setRightButtonResources(int resId) {
        ivStepperPlus.setImageResource(resId);
    }

    /**
     * 设置按钮资源
     */
    public void setRightButtonResources(Drawable drawable) {
        ivStepperPlus.setImageDrawable(drawable);
    }
}
