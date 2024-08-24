package com.youaji.libs.debug.widget._float

/**
 * 浮窗显示类别
 * @author youaji
 * @since 2024/01/05
 */
enum class FloatDisplayType {
    /** 只在当前 Activity 显示 */
    OnlyCurrent,

    /** 仅应用前台时显示 */
    OnlyForeground,

    /** 仅应用后台时显示 */
    OnlyBackground,

    /** 一直显示（不分前后台） */
    Always
}

/**
 * 浮窗的贴边模式
 * @author youaji
 * @since 2024/01/05
 */
enum class FloatSideMode {

    /** 默认不贴边，跟随手指移动 */
    Default,

    /*
     * 左、右、上、下四个方向固定
     * 一直吸附在该方向边缘，只能在该方向的边缘移动
     */
    FixedLeft,
    FixedRight,
    FixedTop,
    FixedBottom,

    /*
     * 根据手指到屏幕边缘的距离
     * 自动选择水平方向的贴边、垂直方向的贴边、四周方向的贴边
     */
    AutoHorizontal,
    AutoVertical,
    AutoSide,

    /*
     * 拖拽时跟随手指移动
     * 结束时贴边
     */
    Ending2Left,
    Ending2Right,
    Ending2Top,
    Ending2Bottom,
    Ending2Horizontal,
    Ending2Vertical,
    Ending2Side
}