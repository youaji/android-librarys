package com.youaji.example.librarys.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.youaji.example.librarys.databinding.FragmentWifiBinding
import com.youaji.libs.ui.basic.BasicBindingFragment
import com.youaji.libs.util.extensions.requestPermissions
import com.youaji.libs.util.logger.logDebug

class WiFiFragment : BasicBindingFragment<FragmentWifiBinding>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private lateinit var wifiManager: WifiManager

    private fun init() {
        wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.setWifiEnabled(true)

        wifiManager.startScan()
    }

    private fun test() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireActivity().requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION) {
                get()
            }
        } else {
            get()
        }
    }

    private fun get() {
        wifiManager = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return
        val connectionInfo = wifiManager.connectionInfo
        logDebug("当前连接信息\n$connectionInfo")

        val scanResults = wifiManager.scanResults
        var log = "扫描到的WiFi信息"
        scanResults.forEach {
            log += "\n$it"
        }
        logDebug(log)

    }

}