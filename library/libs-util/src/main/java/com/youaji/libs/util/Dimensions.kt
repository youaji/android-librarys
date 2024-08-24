@file:Suppress("unused")
package com.youaji.libs.util

import android.content.res.Resources
import android.util.TypedValue

inline val Int.dp: Float get() = toFloat().dp

inline val Long.dp: Float get() = toFloat().dp

inline val Double.dp: Float get() = toFloat().dp

inline val Float.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

inline val Int.sp: Float get() = toFloat().sp

inline val Long.sp: Float get() = toFloat().sp

inline val Double.sp: Float get() = toFloat().sp

inline val Float.sp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)


fun Int.dp2Px(): Int = toFloat().dp2Px()

fun Long.dp2Px(): Int = toFloat().dp2Px()

fun Double.dp2Px(): Int = toFloat().dp2Px()

fun Float.dp2Px(): Int = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

fun Int.px2Dp(): Int = toFloat().px2Dp()

fun Long.px2Dp(): Int = toFloat().px2Dp()

fun Double.px2Dp(): Int = toFloat().px2Dp()

fun Float.px2Dp(): Int = (this / Resources.getSystem().displayMetrics.density + 0.5f).toInt()

fun Int.px2Sp(): Int = toFloat().px2Sp()

fun Long.px2Sp(): Int = toFloat().px2Sp()

fun Double.px2Sp(): Int = toFloat().px2Sp()

fun Float.px2Sp(): Int = (this / Resources.getSystem().displayMetrics.scaledDensity + 0.5f).toInt()