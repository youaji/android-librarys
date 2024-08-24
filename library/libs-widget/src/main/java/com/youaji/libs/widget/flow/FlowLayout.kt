@file:Suppress("unused")
package com.youaji.libs.widget.flow

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.youaji.libs.widget.R

/**
 * @author youaji
 * @since 2023/2/5
 */
open class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr) {

    /**
     * 默认折叠状态
     */
    private val DEFAULT_FOLD = false

    /**
     * 折叠的行数
     */
    private val DEFAULT_FOLD_LINES = 1

    /**
     * 左对齐
     */
    private val DEFAULT_GRAVITY_LEFT = 0

    /**
     * 右对齐
     */
    private val DEFAULT_GRAVITY_RIGHT = 1

    /**
     * 是否折叠，默认false不折叠
     */
    protected var mFold = false

    /**
     * 折叠行数
     */
    private var mFoldLines = DEFAULT_FOLD_LINES

    /**
     * 对齐 默认左对齐
     */
    private var mGravity = DEFAULT_GRAVITY_LEFT

    /**
     * 折叠状态
     */
    private var mFoldState: Boolean? = null

    /**
     * 是否平均
     */
    private var mEqually = false

    /**
     * 一行平局数量
     */
    private var mEquallyCount = 0

    /**
     * 水平距离
     */
    private var mHorizontalSpacing = 0

    /**
     * 竖直距离
     */
    private var mVerticalSpacing = 0


    private var mOnFoldChangedListener: OnFoldChangedListener? = null

    init {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
        mFold = a.getBoolean(R.styleable.FlowLayout_flow_fold, DEFAULT_FOLD)
        mFoldLines = a.getInt(R.styleable.FlowLayout_flow_foldLines, DEFAULT_FOLD_LINES)
        mGravity = a.getInt(R.styleable.FlowLayout_flow_gravity, DEFAULT_GRAVITY_LEFT)
        mEqually = a.getBoolean(R.styleable.FlowLayout_flow_equally, true)
        mEquallyCount = a.getInt(R.styleable.FlowLayout_flow_equally_count, 0)
        mHorizontalSpacing = a.getDimensionPixelOffset(R.styleable.FlowLayout_flow_horizontalSpacing, dp2px(4))
        mVerticalSpacing = a.getDimensionPixelOffset(R.styleable.FlowLayout_flow_verticalSpacing, dp2px(4))
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //当设置折叠 折叠数设置小于0直接隐藏布局
        if (mFold && mFoldLines <= 0) {
            visibility = GONE
            changeFold(true, true, 0, 0)
            return
        }
        //获取mode 和 size
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val layoutWidth = widthSize - paddingLeft - paddingRight
        //判断如果布局宽度抛去左右padding小于0，也不能处理了
        if (layoutWidth <= 0) {
            return
        }

        //这里默认宽高默认值默认把左右，上下padding加上
        var width = paddingLeft + paddingRight
        var height = paddingTop + paddingBottom

        //初始一行的宽度
        var lineWidth = 0
        //初始一行的高度
        var lineHeight = 0

        //测量子View
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        var wh: IntArray? = null
        var childWidth: Int
        var childHeight: Int
        var childWidthMeasureSpec = 0
        var childHeightMeasureSpec = 0
        //行数
        var line = 0
        //折叠的状态
        var newFoldState = false
        //发生折叠的索引
        var foldIndex = 0
        //剩余空间
        var surplusWidth = widthSize
        val count = childCount
        for (i in 0 until count) {
            val view: View = getChildAt(i)
            //这里需要先判断子view是否被设置了GONE
            if (view.visibility == GONE) {
                continue
            }
            //如果设置是平局显示
            if (mEqually) {
                //这里只要计算一次就可以了
                if (wh == null) {
                    //取子view最大的宽高
                    wh = getMaxWidthHeight()
                    //求一行能显示多少个
                    var oneRowItemCount = (layoutWidth + mHorizontalSpacing) / (mHorizontalSpacing + wh[0])
                    //当你设置了一行平局显示多少个
                    if (mEquallyCount > 0) {
                        //判断当你设定的数量小于计算的数量时，使用设置的，所以说当我们计算的竖直小于设置的值的时候这里并没有强制设置设定的值
                        //如果需求要求必须按照设定的来，这里就不要做if判断，直接使用设定的值，但是布局显示会出现显示不全或者...的情况。
                        //if (oneRowItemCount > mEquallyCount) {
                        /**
                         * 这里使用固定，设置了一行几个就是集合 显示不全,显示"..."
                         */
                        oneRowItemCount = mEquallyCount
                        //}
                    }
                    // 根据上面计算的一行显示的数量来计算一个的宽度
                    val newWidth = (layoutWidth - (oneRowItemCount - 1) * mHorizontalSpacing) / oneRowItemCount
                    wh[0] = newWidth
                    //重新获取子view的MeasureSpec
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(wh[0], MeasureSpec.EXACTLY)
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(wh[1], MeasureSpec.EXACTLY)
                }
                childWidth = wh[0]
                childHeight = wh[1]
                //重新测量子view的大小
                getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec)
            } else {
                childWidth = view.measuredWidth
                childHeight = view.measuredHeight
            }

            //第一行
            if (i == 0) {
                lineWidth = paddingLeft + paddingRight + childWidth
                lineHeight = childHeight
            } else {
                //判断是否需要换行
                //换行
                if (lineWidth + mHorizontalSpacing + childWidth > widthSize) {
                    line++ //行数增加
                    // 取最大的宽度
                    width = Math.max(lineWidth, width)
                    //这里判断是否设置折叠及行数是否超过了设定值
                    if (mFold && line >= mFoldLines) {
                        line++
                        height += lineHeight
                        newFoldState = true
                        surplusWidth = widthSize - lineWidth - mHorizontalSpacing
                        break
                    }
                    //重新开启新行，开始记录
                    lineWidth = paddingLeft + paddingRight + childWidth
                    //叠加当前高度，
                    height += mVerticalSpacing + lineHeight
                    //开启记录下一行的高度
                    lineHeight = childHeight
                } else {
                    lineWidth += mHorizontalSpacing + childWidth
                    lineHeight = lineHeight.coerceAtLeast(childHeight)
                }
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == count - 1) {
                line++
                width = Math.max(width, lineWidth)
                height += lineHeight
            }
            foldIndex = i
        }
        //根据计算的值重新设置
        setMeasuredDimension(
            if (widthMode == MeasureSpec.EXACTLY) widthSize else width,
            if (heightMode == MeasureSpec.EXACTLY) heightSize else height
        )
        //折叠状态
        changeFold(line > mFoldLines, newFoldState, foldIndex, surplusWidth)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val layoutWidth = measuredWidth - paddingLeft - paddingRight
        if (layoutWidth <= 0) {
            return
        }
        var childWidth: Int
        var childHeight: Int
        //需要加上top padding
        var top = paddingTop
        val wh = getMaxWidthHeight()
        var lineHeight = 0
        var line = 0
        //左对齐
        if (mGravity == DEFAULT_GRAVITY_LEFT) {
            //左侧需要先加上左边的padding
            var left = paddingLeft
            var i = 0
            val count = childCount
            while (i < count) {
                val view: View = getChildAt(i)
                //这里一样判断下显示状态
                if (view.visibility == GONE) {
                    i++
                    continue
                }
                //如果设置的平均 就使用最大的宽度和高度 否则直接自适宽高
                if (mEqually) {
                    childWidth = wh[0]
                    childHeight = wh[1]
                } else {
                    childWidth = view.measuredWidth
                    childHeight = view.measuredHeight
                }
                //第一行开始摆放
                if (i == 0) {
                    view.layout(left, top, left + childWidth, top + childHeight)
                    lineHeight = childHeight
                } else {
                    //判断是否需要换行
                    if (left + mHorizontalSpacing + childWidth > layoutWidth + paddingLeft) {
                        line++
                        if (mFold && line >= mFoldLines) {
                            line++
                            break
                        }
                        //重新起行
                        left = paddingLeft
                        top += mVerticalSpacing + lineHeight
                        lineHeight = childHeight
                    } else {
                        left += mHorizontalSpacing
                        lineHeight = lineHeight.coerceAtLeast(childHeight)
                    }
                    view.layout(left, top, left + childWidth, top + childHeight)
                }
                //累加left
                left += childWidth
                i++
            }
        } else {
            val paddingLeft = paddingLeft
            // 相当于getMeasuredWidth() -  getPaddingRight();
            var right = layoutWidth + paddingLeft
            var i = 0
            val count = childCount
            while (i < count) {
                val view: View = getChildAt(i)
                if (view.visibility == GONE) {
                    i++
                    continue
                }
                //如果设置的平均 就使用最大的宽度和高度 否则直接自适宽高
                if (mEqually) {
                    childWidth = wh[0]
                    childHeight = wh[1]
                } else {
                    childWidth = view.measuredWidth
                    childHeight = view.measuredHeight
                }
                if (i == 0) {
                    view.layout(right - childWidth, top, right, top + childHeight)
                    lineHeight = childHeight
                } else {
                    //判断是否需要换行
                    if (right - childWidth - mHorizontalSpacing < paddingLeft) {
                        line++
                        if (mFold && line >= mFoldLines) {
                            line++
                            break
                        }
                        //重新起行
                        right = layoutWidth + paddingLeft
                        top += mVerticalSpacing + lineHeight
                        lineHeight = childHeight
                    } else {
                        right -= mHorizontalSpacing
                        lineHeight = lineHeight.coerceAtLeast(childHeight)
                    }
                    view.layout(right - childWidth, top, right, top + childHeight)
                }
                right -= childWidth
                i++
            }
        }
    }

    /**
     * 取最大的子view的宽度和高度
     *
     * @return
     */
    private fun getMaxWidthHeight(): IntArray {
        var maxWidth = 0
        var maxHeight = 0
        var i = 0
        val count = childCount
        while (i < count) {
            val view: View = getChildAt(i)
            if (view.visibility == GONE) {
                i++
                continue
            }
            maxWidth = maxWidth.coerceAtLeast(view.measuredWidth)
            maxHeight = maxHeight.coerceAtLeast(view.measuredHeight)
            i++
        }
        return intArrayOf(maxWidth, maxHeight)
    }

    /**
     * 折叠状态改变回调
     *
     * @param canFold
     * @param newFoldState
     */
    private fun changeFold(canFold: Boolean, newFoldState: Boolean, index: Int, surplusWidth: Int) {
        if (mFoldState == null || mFoldState != newFoldState) {
            if (canFold) {
                mFoldState = newFoldState
            }
            mOnFoldChangedListener?.onFoldChange(canFold, newFoldState, index, surplusWidth)
        }
    }

    /**
     * 设置是否折叠
     *
     * @param fold
     */
    fun setFold(fold: Boolean) {
        mFold = fold
        if (mFoldLines <= 0) {
            visibility = if (fold) GONE else VISIBLE
            changeFold(true, fold, 0, 0)
        } else {
            requestLayout()
        }
    }

    /**
     * 折叠切换，如果之前是折叠状态就切换为未折叠状态，否则相反
     */
    fun toggleFold() {
        setFold(!mFold)
    }


    /**
     * dp->px
     *
     * @param dp
     * @return
     */
    private fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp).toInt()
    }


    /**
     * 设置折叠状态回调
     *
     * @param listener
     */
    fun setOnFoldChangedListener(listener: OnFoldChangedListener) {
        mOnFoldChangedListener = listener
    }

    interface OnFoldChangedListener {
        /**
         * 折叠状态时时回调
         *
         * @param canFold      是否可以折叠，true为可以折叠，false为不可以折叠
         * @param fold         当前折叠状态，true为折叠，false为未折叠
         * @param index        当前显示的view索引数量
         * @param surplusWidth 折叠状态下 剩余空间
         */
        fun onFoldChange(canFold: Boolean, fold: Boolean, index: Int, surplusWidth: Int)
    }
}