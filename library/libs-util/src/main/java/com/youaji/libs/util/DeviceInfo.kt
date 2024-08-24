@file:Suppress("unused")
package com.youaji.libs.util

import android.content.res.Resources
import android.os.Build

/** 屏幕宽度 */
inline val screenWidth: Int get() = Resources.getSystem().displayMetrics.widthPixels

/** 屏幕高度 */
inline val screenHeight: Int get() = Resources.getSystem().displayMetrics.heightPixels

/**
 * 获取设备系统版本名称
 */
inline val sdkVersionName: String get() = Build.VERSION.RELEASE

/**
 * 获取设备系统版本号
 */
inline val sdkVersionCode: Int get() = Build.VERSION.SDK_INT

/**
 * 获取设备厂商
 */
inline val deviceManufacturer: String get() = Build.MANUFACTURER

/**
 * 获取设备型号
 */
inline val deviceModel: String get() = Build.MODEL

/**
 * 获取设备品牌
 */
inline val deviceBrand: String get() = Build.BRAND
