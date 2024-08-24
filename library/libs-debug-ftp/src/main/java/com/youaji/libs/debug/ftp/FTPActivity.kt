package com.youaji.libs.debug.ftp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.youaji.libs.debug.ftp.databinding.LibsDebugFtpActivityFtpBinding
import com.youaji.libs.ftp.client.FTPClient
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.topActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FTPActivity : AppCompatActivity() {

    companion object {
        fun start() {
            try {
                val intent = Intent()
                intent.setClass(topActivity, FTPActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                topActivity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private lateinit var binding: LibsDebugFtpActivityFtpBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LibsDebugFtpActivityFtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        window.statusBarColor = Color.BLACK
//        binding.toolbar.setTitleTextColor(Color.WHITE)
//        binding.toolbar.setBackgroundColor(Color.BLACK)
//        binding.toolbar.setNavigationOnClickListener {
//            @Suppress("DEPRECATION")
//            onBackPressed()
//        }

        window.statusBarColor = Color.BLACK
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setBackgroundColor(Color.BLACK)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val client = FTPClient.Builder(this@FTPActivity)
                    .setFTP()
                    .setServer("192.168.230.1")
//                    .setServer("192.168.254.31")
//                    .setUsername("ftpUser")
//                    .setPassword("ftpUser")
                    .setPort(21)
                    .build()
//                runOnUiThread {
                    logDebug(
                        "client.isConnected:${client.isConnected}\n" +
                                "client.list:${client.list()}"
                    )
//                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}