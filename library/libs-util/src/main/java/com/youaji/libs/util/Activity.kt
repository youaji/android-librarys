@file:Suppress("unused")
package com.youaji.libs.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import java.util.*

internal val activityCache = LinkedList<Activity>()
internal val activityResume = LinkedList<Activity>()

/**
 * 启动 Activity
 */
fun startActivity(intent: Intent) = topActivity.startActivity(intent)

inline fun <reified T : Activity> startActivity(
    vararg pairs: Pair<String, Any?>,
    crossinline block: Intent.() -> Unit = {},
) = topActivity.startActivity<T>(pairs = pairs, block = block)

inline fun <reified T : Activity> Context.startActivity(
    vararg pairs: Pair<String, Any?>,
    crossinline block: Intent.() -> Unit = {},
) = startActivity(intentOf<T>(*pairs).apply(block))

/**
 * 关闭并设置返回值
 */
fun Activity.finishWithResult(vararg pairs: Pair<String, *>) {
    setResult(Activity.RESULT_OK, Intent().putExtras(bundleOf(*pairs)))
    finish()
}

/**
 * 获取 Activity 栈链表
 */
val activityList: List<Activity> get() = activityCache.toList()

/**
 * 获取栈顶 Activity
 * 注意：如果应用处于后台时，activityCache 将是 empty，因此 topActivity 也将异常
 */
val topActivity: Activity get() = activityCache.last()

/** app处于前台 */
val isAppOnForeground: Boolean get() = activityResume.isNotEmpty()

/**
 * 判断 Activity 是否存在栈中
 */
inline fun <reified T : Activity> isActivityExistsInStack(): Boolean = isActivityExistsInStack(T::class.java)
fun <T : Activity> isActivityExistsInStack(clazz: Class<T>): Boolean =
    activityCache.any { it.javaClass.name == clazz.name }

/**
 * 结束某个 Activity
 */
inline fun <reified T : Activity> finishActivity(): Boolean = finishActivity(T::class.java)
fun <T : Activity> finishActivity(clazz: Class<T>): Boolean =
    activityCache.removeAll {
        if (it.javaClass.name == clazz.name) it.finish()
        it.javaClass.name == clazz.name
    }

/**
 * 结束到某个 Activity
 */
inline fun <reified T : Activity> finishToActivity(): Boolean = finishToActivity(T::class.java)
fun <T : Activity> finishToActivity(clazz: Class<T>): Boolean {
    for (i in activityCache.count() - 1 downTo 0) {
        if (clazz.name == activityCache[i].javaClass.name) {
            return true
        }
        activityCache.removeAt(i).finish()
    }
    return false
}

/**
 * 结束所有 Activity
 */
fun finishAllActivities(): Boolean =
    activityCache.removeAll {
        it.finish()
        true
    }

/**
 * 结束除某个 Activity 之外的所有 Activity
 */
inline fun <reified T : Activity> finishAllActivitiesExcept(): Boolean = finishAllActivitiesExcept(T::class.java)
fun <T : Activity> finishAllActivitiesExcept(clazz: Class<T>): Boolean =
    activityCache.removeAll {
        if (it.javaClass.name != clazz.name) it.finish()
        it.javaClass.name != clazz.name
    }

/**
 * 结束除最新 Activity 之外的所有 Activity
 */
fun finishAllActivitiesExceptNewest(): Boolean =
    finishAllActivitiesExcept(topActivity.javaClass)

/**
 * 双击返回退出 App
 */
fun ComponentActivity.pressBackTwiceToExitApp(toastText: String, delayMillis: Long = 2000, owner: LifecycleOwner = this) =
    pressBackTwiceToExitApp(delayMillis, owner) { toast(toastText) }

fun ComponentActivity.pressBackTwiceToExitApp(@StringRes toastText: Int, delayMillis: Long = 2000, owner: LifecycleOwner = this) =
    pressBackTwiceToExitApp(delayMillis, owner) { toast(toastText) }

/**
 * 双击返回退出 App，自定义 Toast
 */
private var lastBackTime: Long = 0
fun ComponentActivity.pressBackTwiceToExitApp(
    delayMillis: Long = 2000,
    owner: LifecycleOwner = this,
    onFirstBackPressed: () -> Unit,
) =
    onBackPressedDispatcher.addCallback(owner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackTime > delayMillis) {
                onFirstBackPressed()
                lastBackTime = currentTime
            } else {
                finishAllActivities()
            }
        }
    })

/**
 * 点击返回不退出 App
 */
fun ComponentActivity.pressBackToNotExitApp(owner: LifecycleOwner = this) =
    doOnBackPressed(owner) { moveTaskToBack(false) }

/**
 * 监听手机的返回事件
 */
fun ComponentActivity.doOnBackPressed(owner: LifecycleOwner = this, onBackPressed: () -> Unit) =
    onBackPressedDispatcher.addCallback(owner, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = onBackPressed()
    })

/**
 * 判断是否有权限
 */
fun Context.isPermissionGranted(permission: String): Boolean =
    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.arePermissionsGranted(vararg permissions: String): Boolean =
    permissions.all { isPermissionGranted(it) }

fun Context.asActivity(): Activity? =
    this as? Activity ?: (this as? ContextWrapper)?.baseContext?.asActivity()

var Activity.decorFitsSystemWindows: Boolean
    @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR)
    get() = noGetter()
    set(value) = WindowCompat.setDecorFitsSystemWindows(window, value)

/**
 * 获取布局的根视图
 */
inline val Activity.contentView: View
    get() = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)

/**
 * 把 Context 作为 Activity 使用
 */
@Deprecated("Use `Context.asActivity()` instead.", ReplaceWith("asActivity()"))
val Context.activity: Activity?
    get() = asActivity()

/**
 * 作用域的 this 不是 Activity 时获取 Context
 */
inline val Context.context: Context get() = this

/**
 * 作用域的 this 不是 Activity 时获取 Activity
 */
inline val Activity.activity: Activity get() = this

/**
 * 作用域的 this 不是 Activity 时获取 FragmentActivity
 */
inline val FragmentActivity.fragmentActivity: FragmentActivity get() = this

/**
 * 作用域的 this 不是 Activity 时获取 LifecycleOwner
 */
inline val ComponentActivity.lifecycleOwner: LifecycleOwner get() = this
