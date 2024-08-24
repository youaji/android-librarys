package com.youaji.libs.picture.viewer.util.notch

import androidx.annotation.IntDef

/**
 * 异形屏全屏适配方案
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(CutOutMode.DEFAULT, CutOutMode.SHORT_EDGES, CutOutMode.NEVER, CutOutMode.ALWAYS)
annotation class CutOutMode {
    companion object {
        /**
         * 默认模式，在全屏状态下，效果与[.NEVER]一致，在非全屏状态下，竖屏时绘制到耳朵区域，横屏时禁用耳朵区
         */
        const val DEFAULT = 0

        /**
         * 耳朵区域绘制模式，在androidP(包含)以上机型，横竖屏都绘制到耳朵区域。
         * 此版本之下，小米手机只竖屏绘制到耳朵区域
         */
        const val SHORT_EDGES = 1

        /**
         * 耳朵区域不绘制模式，此时全屏时，状态栏呈现黑条
         */
        const val NEVER = 2

        /**
         * 横竖屏都绘制到耳朵区域,如果可以单独设置的情况下
         */
        const val ALWAYS = 3
    }
}
