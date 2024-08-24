package com.youaji.libs.debug.crash.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.TextUtils
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import java.io.BufferedReader
import java.io.Closeable
import java.io.DataOutputStream
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.Collections

/**
 * 获取设备相关信息
 * 用户收集相关信息。比如：机型、系统、厂商、CPU、ABI、Linux 版本等。
 * @author youaji
 * @since 2024/01/05
 */
object NetDeviceUtils {

    /**
     * 返回的命令结果
     */
    data class CommandResult(
        /** 结果码 */
        var result: Int,
        /** 成功信息 */
        var successMsg: String = "",
        /** 错误信息 */
        var errorMsg: String = ""
    )

    private val LINE_SEP = System.getProperty("line.separator")

    /** @return 判断设备是否 root */
    val isDeviceRooted: Boolean
        get() = try {
            val su = "su"
            val locations = arrayOf(
                "/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/"
            )
            var rooted = false
            for (location in locations) {
                if (File(location + su).exists()) {
                    rooted = true
                    break
                }
            }
            rooted
        } catch (e: Exception) {
            false
        }

    /** @return 设备系统版本号 */
    val sDKVersionName: String
        get() = Build.VERSION.RELEASE

    /** @return 设备系统版本码 */
    val sDKVersionCode: Int
        get() = Build.VERSION.SDK_INT

    /** @return 设备 AndroidID*/
    @SuppressLint("HardwareIds")
    fun getAndroidID(context: Context): String {
        return Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    /** @return 设备厂商 */
    val manufacturer: String
        get() = Build.MANUFACTURER

    /** @return 设备的品牌 */
    val brand: String
        get() = Build.BRAND

    /** @return 设备版本号 */
    val id: String
        get() = Build.ID

    /**  @return CPU的类型 */
    val cpuType: String
        get() = Build.CPU_ABI

    /** @return 设备型号 */
    val model: String
        get() {
            var model = Build.MODEL
            model = model?.trim { it <= ' ' }?.replace("\\s*".toRegex(), "") ?: ""
            return model
        }

    /** @return 获取wifi的强弱 */
    fun getWifiState(context: Context): String {
        if (isWifiConnect(context)) {
            val mWifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            var mWifiInfo: WifiInfo? = null
            if (mWifiManager != null) {
                mWifiInfo = mWifiManager.connectionInfo
                val wifi = mWifiInfo.rssi //获取wifi信号强度
                return if (wifi > -50 && wifi < 0) { //最强
                    "最强"
                } else if (wifi > -70 && wifi < -50) { //较强
                    "较强"
                } else if (wifi > -80 && wifi < -70) { //较弱
                    "较弱"
                } else if (wifi > -100 && wifi < -80) { //微弱
                    "微弱"
                } else {
                    "微弱"
                }
            }
        }
        return "无wifi连接"
    }

    fun isWifiConnect(context: Context): Boolean {
        val connManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var mWifiInfo: NetworkInfo? = null
        if (connManager != null) {
            mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWifiInfo!!.isConnected
        }
        return false
    }

    /** @return 通过域名获取真实的ip地址 (此方法需要在线程中调用) */
    fun getHostIP(domain: String?): String {
        var ipAddress = ""
        var iAddress: InetAddress? = null
        try {
            iAddress = InetAddress.getByName(domain)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
        if (iAddress != null) {
            ipAddress = iAddress.hostAddress
        }
        return ipAddress
    }

    /**  @return 通过域名获取真实的ip地址 (此方法需要在线程中调用) */
    fun getHostName(domain: String?): String {
        var ipAddress = ""
        var iAddress: InetAddress? = null
        try {
            iAddress = InetAddress.getByName(domain)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        }
        if (iAddress != null) {
            ipAddress = iAddress.hostName
        }
        return ipAddress
    }

    /** 获取wifi的名称 */
    fun getWifiName(context: Context): String {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo: WifiInfo? = null
        if (wifiManager != null) {
            wifiInfo = wifiManager.connectionInfo
            logI_LDC("--- WIFI info ---\n$wifiInfo \n ")
            return wifiInfo.ssid
        }
        return "无网络"
    }

    /** @return 获取wifi的ip */
    fun getWifiIp(context: Context): Int {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo: WifiInfo? = null
        if (wifiManager != null) {
            wifiInfo = wifiManager.connectionInfo
            logI_LDC("--- WIFI info ---\n$wifiInfo \n ")
            return wifiInfo.ipAddress
        }
        return -1
    }

    /** @return 获取wifi的信息 */
    fun getWifiInfo(context: Context): WifiInfo? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiInfo: WifiInfo? = null
        if (wifiManager != null) {
            wifiInfo = wifiManager.connectionInfo
            return wifiInfo
        }
        return null
    }

    /** @return 获取dhcp信息 */
    fun getDhcpInfo(context: Context): DhcpInfo? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        var dhcpInfo: DhcpInfo? = null
        if (wifiManager != null) {
            dhcpInfo = wifiManager.dhcpInfo
            return dhcpInfo
        }
        return null
    }

    fun intToIp(paramInt: Int): String {
        return ((paramInt and 0xFF).toString() + "." + (0xFF and (paramInt shr 8)) + "." + (0xFF and (paramInt shr 16)) + "."
                + (0xFF and (paramInt shr 24)))
    }

    fun getSDCardSpace(context: Context): String {
        return try {
            val free = getSDAvailableSize(context)
            val total = getSDTotalSize(context)
            "$free/$total"
        } catch (e: Exception) {
            "-/-"
        }
    }

    /** @return 获得SD卡总大小 */
    private fun getSDTotalSize(context: Context): String {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSize.toLong()
        val totalBlocks = stat.blockCount.toLong()
        return Formatter.formatFileSize(context, blockSize * totalBlocks)
    }

    /** @return 获得sd卡剩余容量，即可用大小 */
    private fun getSDAvailableSize(context: Context): String {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSize.toLong()
        val availableBlocks = stat.availableBlocks.toLong()
        return Formatter.formatFileSize(context, blockSize * availableBlocks)
    }

    fun getRomSpace(context: Context): String {
        return try {
            val free = getRomAvailableSize(context)
            val total = getRomTotalSize(context)
            "$free/$total"
        } catch (e: Exception) {
            "-/-"
        }
    }

    /** @return 获得机身可用内存 */
    private fun getRomAvailableSize(context: Context): String {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSize.toLong()
        val availableBlocks = stat.availableBlocks.toLong()
        return Formatter.formatFileSize(context, blockSize * availableBlocks)
    }

    /** @return 获得机身内存总大小 */
    private fun getRomTotalSize(context: Context): String {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSize.toLong()
        val totalBlocks = stat.blockCount.toLong()
        return Formatter.formatFileSize(context, blockSize * totalBlocks)
    }

    /**  @return 手机总内存(兆) */
    fun getTotalMemory(context: Context?): Long {
        val str1 = "/proc/meminfo" // 系统内存信息文件
        val str2: String
        val arrayOfString: Array<String>
        var initial_memory: Long = 0
        try {
            val localFileReader = FileReader(str1)
            val localBufferedReader = BufferedReader(
                localFileReader, 8192
            )
            str2 = localBufferedReader.readLine() // 读取meminfo第一行，系统总内存大小
            if (!TextUtils.isEmpty(str2)) {
                arrayOfString = str2.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // 获得系统总内存，单位是KB，乘以1024转换为Byte
                initial_memory = (Integer.valueOf(arrayOfString[1]) / 1024).toLong()
            }
            localBufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return initial_memory // Byte转换为KB或者MB，内存大小规格化
    }

    /**  @return 手机当前可用内存(兆) */
    fun getAvailMemory(context: Context): Long { // 获取android当前可用内存大小
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(mi)
        return mi.availMem / 1024 / 1024
    }

    fun getWidthPixels(context: Context): Int {
        val metrics = DisplayMetrics()
        val windowManager = context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager ?: return 0
        windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    fun getRealHeightPixels(context: Context): Int {
        val windowManager = context.applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var height = 0
        var display: Display? = null
        if (windowManager != null) {
            display = windowManager.defaultDisplay
        }
        val dm = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            height = dm.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return height
    }

    /** @return 获取屏幕尺寸 */
    fun getScreenInch(context: Activity): Double {
        var inch = 0.0
        try {
            var realWidth = 0
            var realHeight = 0
            val display = context.windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            if (Build.VERSION.SDK_INT >= 17) {
                val size = Point()
                display.getRealSize(size)
                realWidth = size.x
                realHeight = size.y
            } else if (Build.VERSION.SDK_INT < 17
                && Build.VERSION.SDK_INT >= 14
            ) {
                val mGetRawH = Display::class.java.getMethod("getRawHeight")
                val mGetRawW = Display::class.java.getMethod("getRawWidth")
                realWidth = mGetRawW.invoke(display) as Int
                realHeight = mGetRawH.invoke(display) as Int
            } else {
                realWidth = metrics.widthPixels
                realHeight = metrics.heightPixels
            }
            inch = formatDouble(
                Math.sqrt(
                    (realWidth / metrics.xdpi * (realWidth / metrics.xdpi)
                            + realHeight / metrics.ydpi * (realHeight / metrics.ydpi)).toDouble()
                ), 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return inch
    }

    /**
     * @param d Double类型保留指定位数的小数，返回double类型（四舍五入）
     * @param newScale 为指定的位数
     */
    private fun formatDouble(d: Double, newScale: Int): Double {
        val bd = BigDecimal(d)
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    /**
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`
     * 需添加权限 `<uses-permission android:name="android.permission.INTERNET" />`
     * @return MAC 地址
     */
    fun getMacAddress(context: Context): String {
        var macAddress = getMacAddressByWifiInfo(context)
        if ("02:00:00:00:00:00" != macAddress) {
            return macAddress
        }
        macAddress = macAddressByNetworkInterface
        if ("02:00:00:00:00:00" != macAddress) {
            return macAddress
        }
        macAddress = macAddressByFile
        return if ("02:00:00:00:00:00" != macAddress) {
            macAddress
        } else "please open wifi"
    }

    /**
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`
     * @return MAC 地址
     */
    @SuppressLint("HardwareIds", "MissingPermission")
    private fun getMacAddressByWifiInfo(context: Context): String {
        try {
            @SuppressLint("WifiManagerLeak") val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifi != null) {
                val info = wifi.connectionInfo
                if (info != null) return info.macAddress
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "02:00:00:00:00:00"
    }

    /**
     * 需添加权限 `<uses-permission android:name="android.permission.INTERNET" />`
     * @return MAC 地址
     */
    private val macAddressByNetworkInterface: String
        get() {
            try {
                val nis: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (ni in nis) {
                    if (!ni.name.equals("wlan0", ignoreCase = true)) continue
                    val macBytes = ni.hardwareAddress
                    if (macBytes != null && macBytes.size > 0) {
                        val res1 = StringBuilder()
                        for (b in macBytes) {
                            res1.append(String.format("%02x:", b))
                        }
                        return res1.deleteCharAt(res1.length - 1).toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "02:00:00:00:00:00"
        }

    /** @return MAC 地址 */
    private val macAddressByFile: String
        private get() {
            var result = execCmd(arrayOf("getprop wifi.interface"), false, true)
            if (result.result == 0) {
                val name = result.successMsg
                if (name != null) {
                    result = execCmd(arrayOf("cat /sys/class/net/$name/address"), false, true)
                    if (result.result == 0) {
                        if (result.successMsg != null) {
                            return result.successMsg
                        }
                    }
                }
            }
            return "02:00:00:00:00:00"
        }

    /**
     * 是否是在 root 下执行命令
     * @param commands        命令数组
     * @param isRoot          是否需要 root 权限执行
     * @param isNeedResultMsg 是否需要结果消息
     * @return CommandResult
     */
    fun execCmd(
        commands: Array<String>?,
        isRoot: Boolean,
        isNeedResultMsg: Boolean
    ): CommandResult {
        var result = -1
        if (commands == null || commands.size == 0) {
            return CommandResult(result)
        }
        var process: Process? = null
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        var successMsg: StringBuilder? = null
        var errorMsg: StringBuilder? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec(if (isRoot) "su" else "sh")
            os = DataOutputStream(process.outputStream)
            for (command in commands) {
                if (command == null) continue
                os.write(command.toByteArray())
                os.writeBytes(LINE_SEP)
                os.flush()
            }
            os.writeBytes("exit" + LINE_SEP)
            os.flush()
            result = process.waitFor()
            if (isNeedResultMsg) {
                successMsg = StringBuilder()
                errorMsg = StringBuilder()
                successResult = BufferedReader(
                    InputStreamReader(
                        process.inputStream,
                        "UTF-8"
                    )
                )
                errorResult = BufferedReader(
                    InputStreamReader(
                        process.errorStream,
                        "UTF-8"
                    )
                )
                var line: String?
                if (successResult.readLine().also { line = it } != null) {
                    successMsg.append(line)
                    while (successResult.readLine().also { line = it } != null) {
                        successMsg.append(LINE_SEP).append(line)
                    }
                }
                if (errorResult.readLine().also { line = it } != null) {
                    errorMsg.append(line)
                    while (errorResult.readLine().also { line = it } != null) {
                        errorMsg.append(LINE_SEP).append(line)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeIO(os!!, successResult!!, errorResult!!)
            process?.destroy()
        }
        return CommandResult(
            result,
            successMsg?.toString() ?: "",
            errorMsg?.toString() ?: ""
        )
    }

    /**
     * 关闭 IO
     *
     * @param closeables closeables
     */
    private fun closeIO(vararg closeables: Closeable) {
        if (closeables == null) return
        for (closeable in closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}