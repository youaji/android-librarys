package com.youaji.example.librarys

import android.Manifest
import android.os.Bundle
import android.os.Process
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import com.youaji.example.librarys.databinding.ActivityMainBinding
import com.youaji.libs.debug.DebugService
import com.youaji.libs.ui.basic.BasicBindingActivity
import com.youaji.libs.util.appName
import com.youaji.libs.util.extensions.requestPermissions
import com.youaji.libs.util.finishAllActivities
import com.youaji.libs.util.isAppDebug

class MainActivity : BasicBindingActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.black)
        setToolbar(title = appName) {
            navIcon(R.drawable.ic_menu_24_black) {
                if (binding.drawerLayout.isOpen) binding.drawerLayout.close()
                else binding.drawerLayout.open()
            }
        }
        binding.navView.setNavigationItemSelectedListener {
            updateToolbar { title = it.title as String }
            val navId = when (it.itemId) {
                R.id.nav_menu_home -> R.id.nav_home
                R.id.nav_menu_lib_socket -> R.id.nav_lib_socket
                R.id.nav_menu_lib_tcp -> R.id.nav_lib_tcp
                R.id.nav_menu_lib_opencv -> R.id.nav_lib_opencv
                R.id.nav_menu_lib_yuv -> R.id.nav_lib_yuv
                R.id.nav_menu_lib_ftp_client -> R.id.nav_lib_ftp_client
                R.id.nav_menu_wifi -> R.id.nav_wifi
                else -> throw IllegalArgumentException("未知菜单id，没实现该逻辑？")
            }
            findNavController(R.id.nav_host_fragment_content).navigate(navId)
            binding.drawerLayout.close()
            return@setNavigationItemSelectedListener true
        }
        requestPermissions()
        binding.drawerLayout.open()

        if (isAppDebug) {
            DebugService.get.checkVersionUpdate(this)
            DebugService.get.setUserInfo("")
            DebugService.get.setFloatButton(application)
        }

        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAllActivities()
                Process.killProcess(Process.myPid())
            }
        })
    }

    private fun requestPermissions() {
        requestPermissions(
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {}
    }
}