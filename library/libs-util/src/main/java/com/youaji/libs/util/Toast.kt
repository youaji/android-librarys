@file:Suppress("unused")
package com.youaji.libs.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.toast(message: CharSequence?): Toast =
    requireActivity().toast(message)

fun Fragment.toast(@StringRes message: Int): Toast =
    requireActivity().toast(message)

fun Context.toast(message: CharSequence?): Toast =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).fixBadTokenException().apply { show() }

fun Context.toast(@StringRes message: Int): Toast =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).fixBadTokenException().apply { show() }

fun Fragment.longToast(message: CharSequence?): Toast =
    requireActivity().longToast(message)

fun Fragment.longToast(@StringRes message: Int): Toast =
    requireActivity().longToast(message)

fun Context.longToast(message: CharSequence?): Toast =
    Toast.makeText(this, message, Toast.LENGTH_LONG).fixBadTokenException().apply { show() }

fun Context.longToast(@StringRes message: Int): Toast =
    Toast.makeText(this, message, Toast.LENGTH_LONG).fixBadTokenException().apply { show() }

/**
 * 修复 7.1 的 BadTokenException
 */
fun Toast.fixBadTokenException(): Toast = apply {
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
        try {
            @SuppressLint("DiscouragedPrivateApi")
            val tnField = Toast::class.java.getDeclaredField("mTN")
            tnField.isAccessible = true
            val tn = tnField.get(this)

            val handlerField = tnField.type.getDeclaredField("mHandler")
            handlerField.isAccessible = true
            val handler = handlerField.get(tn) as Handler

            val looper = checkNotNull(Looper.myLooper()) {
                "Can't toast on a thread that has not called Looper.prepare()"
            }
            handlerField.set(tn, object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    try {
                        handler.handleMessage(msg)
                    } catch (ignored: WindowManager.BadTokenException) {
                    }
                }
            })
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: NoSuchFieldException) {
        }
    }
}

//fun Fragment.toastNormal(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastNormal(message, duration)
//
//fun Fragment.toastNormal(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastNormal(message, duration)
//
//fun Fragment.toastInfo(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastInfo(message, duration)
//
//fun Fragment.toastInfo(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastInfo(message, duration)
//
//fun Fragment.toastWarn(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastWarn(message, duration)
//
//fun Fragment.toastWarn(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastWarn(message, duration)
//
//fun Fragment.toastError(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastError(message, duration)
//
//fun Fragment.toastError(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastError(message, duration)
//
//fun Fragment.toastSuccess(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastSuccess(message, duration)
//
//fun Fragment.toastSuccess(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    requireActivity().toastSuccess(message, duration)
//
//fun Context.toastNormal(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.normal(this, message, duration).show()
//
//fun Context.toastNormal(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.normal(this, message, duration).show()
//
//fun Context.toastInfo(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.info(this, message, duration, false).show()
//
//fun Context.toastInfo(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.info(this, message, duration, false).show()
//
//fun Context.toastWarn(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.warning(this, message, duration, false).show()
//
//fun Context.toastWarn(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.warning(this, message, duration, false).show()
//
//fun Context.toastError(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.error(this, message, duration, false).show()
//
//fun Context.toastError(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.error(this, message, duration, false).show()
//
//fun Context.toastSuccess(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.success(this, message, duration, false).show()
//
//fun Context.toastSuccess(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) =
//    Toasty.success(this, message, duration, false).show()

