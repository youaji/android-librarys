package com.youaji.libs.util.extensions.permissions

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.permissionx.guolindev.dialog.RationaleDialog
import com.permissionx.guolindev.dialog.allSpecialPermissions
import com.permissionx.guolindev.dialog.permissionMapOnQ
import com.permissionx.guolindev.dialog.permissionMapOnR
import com.permissionx.guolindev.dialog.permissionMapOnS
import com.permissionx.guolindev.dialog.permissionMapOnT
import com.youaji.libs.util.databinding.LibsUtilPermissionDialogBinding
import com.youaji.libs.util.databinding.LibsUtilPermissionItemBinding

/**
 * @author youaji
 * @since 2023/7/1
 */
class PermissionDialog(
    context: Context,
    private val permissions: List<String>,
    private val message: String,
    private val positiveText: String,
    private val negativeText: String?,
    cancelListener: DialogInterface.OnCancelListener,
) : RationaleDialog(context, false, cancelListener) {

    private lateinit var binding: LibsUtilPermissionDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LibsUtilPermissionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupText()
        buildPermissionsLayout()
        setupWindow()
    }

    override fun getPositiveButton(): View {
        return binding.btnPositive
    }

    override fun getNegativeButton(): View {
        return binding.btnNegative
    }

    override fun getPermissionsToRequest(): List<String> {
        return permissions
    }

    private fun setupText() {
        binding.textContent.text = message
        binding.btnPositive.text = positiveText
        if (negativeText != null) {
            binding.btnNegative.visibility = View.VISIBLE
            binding.btnNegative.text = negativeText
        } else {
            binding.btnNegative.visibility = View.GONE
        }
    }

    private fun setupWindow() {
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        if (width < height) {
            // now we are in portrait
            window?.let {
                val param = it.attributes
                it.setGravity(Gravity.CENTER)
                param.width = (width * 0.86).toInt()
                it.attributes = param
            }
        } else {
            // now we are in landscape
            window?.let {
                val param = it.attributes
                it.setGravity(Gravity.CENTER)
                param.width = (width * 0.6).toInt()
                it.attributes = param
            }
        }
    }

    private fun buildPermissionsLayout() {
        val tempSet = HashSet<String>()
        val currentVersion = Build.VERSION.SDK_INT
        for (permission in permissions) {
            val permissionGroup = when {
                currentVersion < Build.VERSION_CODES.Q -> {
                    try {
                        val permissionInfo = context.packageManager.getPermissionInfo(permission, 0)
                        permissionInfo.group
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                        null
                    }
                }

                currentVersion == Build.VERSION_CODES.Q -> permissionMapOnQ[permission]
                currentVersion == Build.VERSION_CODES.R -> permissionMapOnR[permission]
                currentVersion == Build.VERSION_CODES.S -> permissionMapOnS[permission]
                currentVersion == Build.VERSION_CODES.TIRAMISU -> permissionMapOnT[permission]
                else -> {
                    // If currentVersion is newer than the latest version we support, we just use
                    // the latest version for temp. Will upgrade in the next release.
                    permissionMapOnT[permission]
                }
            }
            if ((permission in allSpecialPermissions && !tempSet.contains(permission))
                || (permissionGroup != null && !tempSet.contains(permissionGroup))
            ) {
                val itemBinding =
                    LibsUtilPermissionItemBinding.inflate(layoutInflater, binding.layoutPermissions, false)
                when {
                    permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        itemBinding.textPermission.text = "后台访问位置信息"
                        itemBinding.iconPermission.setImageResource(
                            context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).icon
                        )
                    }

                    permission == Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        itemBinding.textPermission.text = "显示在其他应用的上层"
//                        itemBinding.iconPermission.setImageResource(R.drawable.permissionx_ic_alert)
                    }

                    permission == Manifest.permission.WRITE_SETTINGS -> {
                        itemBinding.textPermission.text = "修改系统设置"
//                        itemBinding.iconPermission.setImageResource(R.drawable.permissionx_ic_setting)
                    }

                    permission == Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                        itemBinding.textPermission.text = "所有文件访问权限"
                        itemBinding.iconPermission.setImageResource(
                            context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).icon
                        )
                    }

                    permission == Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
                        itemBinding.textPermission.text = "安装未知应用"
//                        itemBinding.iconPermission.setImageResource(R.drawable.permissionx_ic_install)
                    }

                    permission == Manifest.permission.POST_NOTIFICATIONS
                            && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                        // When OS version is lower than Android 13, there isn't a notification icon or labelRes for us to get.
                        // So we need to handle it as special permission's way.
                        itemBinding.textPermission.text = "通知"
//                        itemBinding.iconPermission.setImageResource(R.drawable.permissionx_ic_notification)
                    }

                    permission == Manifest.permission.BODY_SENSORS_BACKGROUND -> {
                        itemBinding.textPermission.text = "后台访问身体传感器"
                        itemBinding.iconPermission.setImageResource(
                            context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).icon
                        )
                    }

                    else -> {
                        itemBinding.textPermission.text = context.getString(context.packageManager.getPermissionGroupInfo(permissionGroup!!, 0).labelRes)
                        itemBinding.iconPermission.setImageResource(
                            context.packageManager.getPermissionGroupInfo(permissionGroup, 0).icon
                        )
                    }
                }
                binding.layoutPermissions.addView(itemBinding.root)
                tempSet.add(permissionGroup ?: permission)
            }
        }
    }
}