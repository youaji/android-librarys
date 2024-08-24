package com.youaji.libs.picture.viewer

import androidx.annotation.IntDef

/**
 * 图片指示器类型
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(IndicatorType.DOT, IndicatorType.TEXT)
annotation class IndicatorType {
    companion object {
        /**
         * 圆点,如果图片多于 [Config.maxIndicatorDot] 则采用 [TEXT]
         */
        const val DOT = 0

        /**
         * 文本
         */
        const val TEXT = 1
    }
}
