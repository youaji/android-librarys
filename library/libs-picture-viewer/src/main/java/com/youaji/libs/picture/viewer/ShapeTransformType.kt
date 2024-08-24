package com.youaji.libs.picture.viewer

import androidx.annotation.IntDef

/**
 * 图形变换类型
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@IntDef(ShapeTransformType.CIRCLE, ShapeTransformType.ROUND_RECT)
annotation class ShapeTransformType {
    companion object {
        /**
         * 切圆形，预览动画从圆形变换为矩形
         */
        const val CIRCLE = 0

        /**
         * 切圆角矩形，预览动画从圆角矩形变换为矩形
         */
        const val ROUND_RECT = 1
    }
}
