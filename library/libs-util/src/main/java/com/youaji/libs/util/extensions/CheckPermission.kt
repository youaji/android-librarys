package com.youaji.libs.util.extensions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import com.youaji.libs.util.extensions.permissions.PermissionDialog

/**
 * 是否授予权限
 * @return true：已授予；false：未授予；
 */
fun Context.isGrantedPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * 是否显示申请权限弹窗
 * @return true：显示（权限未授予时）；false：不显示（未申请时、已授予时、不再提示）；
 */
fun Activity.isShowRequestPermissionRationale(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

/**
 * 请求权限
 * @param permissions   权限
 * @param callback      回调
 */
fun FragmentActivity.requestPermissions(
    whyMessage: String = "",
    vararg permissions: String,
    forwardEnable: Boolean = false,
    callback: (isGranted: Boolean) -> Unit,
) {
    requestPermissions(whyMessage, listOf(*permissions), forwardEnable, callback)
}

/**
 * 请求权限
 * @param permissions   权限
 * @param callback      回调
 */
fun FragmentActivity.requestPermissions(
    whyMessage: String = "",
    permissions: List<String>,
    forwardEnable: Boolean = false,
    callback: (isGranted: Boolean) -> Unit,
) {
    val builder = PermissionX.init(this).permissions(permissions).onExplainRequestReason { scope, deniedList ->
        val dialog = PermissionDialog(
            this,
            deniedList,
            whyMessage,
            "允许",
            "取消"
        ) {
            // 此处不可能回调
            callback.invoke(false)
        }
//        scope.showRequestReasonDialog(deniedList, whyMessage, "允许", "取消")
        scope.showRequestReasonDialog(dialog)
    }
    if (forwardEnable) {
        builder.onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(deniedList, whyMessage, "去设置", "取消")
        }
    }
    builder.request { allGranted, _, _ ->
        callback.invoke(allGranted)
    }
}