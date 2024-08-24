package com.youaji.libs.debug.crash.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicLong

/**
 * 内存相关工具类
 * 所有结果以KB为单位
 * @author youaji
 * @since 2024/01/05
 */
object MemoryUtils {
    /** 获取当前应用进程的pid */
    val currentPid: Int
        get() = Process.myPid()

    /**
     * 获取总体内存使用情况
     */
    fun getMemoryInfo(context: Context, onGetMemoryInfoCallback: OnGetMemoryInfoCallback) {
        Thread {
            val pkgName = context.packageName
            val pid = Process.myPid()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            //1. ram
            val ramMemoryInfo = RamMemoryInfo()
            ramMemoryInfo.availMem = mi.availMem
            ramMemoryInfo.isLowMemory = mi.lowMemory
            ramMemoryInfo.lowMemThreshold = mi.threshold
            ramMemoryInfo.totalMem = getRamTotalMemSync(context)
            //2. pss
            val pssInfo = getAppPssInfo(context, pid)
            //3. dalvik heap
            val dalvikHeapMem = appDalvikHeapMem
            Handler(Looper.getMainLooper()).post { onGetMemoryInfoCallback.onGetMemoryInfo(pkgName, pid, ramMemoryInfo, pssInfo, dalvikHeapMem) }
        }.start()
    }

    /**
     * 获取手机RAM的存储情况
     */
    fun getSystemRam(context: Context, onGetRamMemoryInfoCallback: OnGetRamMemoryInfoCallback) {
        getRamTotalMem(context, object : OnGetRamTotalMemCallback {
            override fun onGetRamTotalMem(totalMem: Long) {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val mi = ActivityManager.MemoryInfo()
                am.getMemoryInfo(mi)
                val ramMemoryInfo = RamMemoryInfo()
                ramMemoryInfo.availMem = mi.availMem
                ramMemoryInfo.isLowMemory = mi.lowMemory
                ramMemoryInfo.lowMemThreshold = mi.threshold
                ramMemoryInfo.totalMem = totalMem
                onGetRamMemoryInfoCallback.onGetRamMemoryInfo(ramMemoryInfo)
            }
        })
    }

    /**
     * 获取应用实际占用内存
     *
     * @return 应用pss信息KB
     */
    fun getAppPssInfo(context: Context, pid: Int): PssInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = am.getProcessMemoryInfo(intArrayOf(pid))[0]
        val pssInfo = PssInfo()
        //返回总的PSS内存使用量(以kB为单位)
        pssInfo.totalPss = memoryInfo.totalPss
        //dalvik堆的比例设置大小
        pssInfo.dalvikPss = memoryInfo.dalvikPss
        //本机堆的比例设置大小
        pssInfo.nativePss = memoryInfo.nativePss
        //比例设置大小为其他所有
        pssInfo.otherPss = memoryInfo.otherPss
        return pssInfo
    }

    /** @return dalvik堆内存KB */
    val appDalvikHeapMem: DalvikHeapMem
        get() {
            val runtime = Runtime.getRuntime()
            val dalvikHeapMem = DalvikHeapMem()
            // 空闲内存
            dalvikHeapMem.freeMem = runtime.freeMemory()
            // 最大内存
            dalvikHeapMem.maxMem = Runtime.getRuntime().maxMemory()
            // 已用内存
            dalvikHeapMem.allocated = Runtime.getRuntime().totalMemory() - runtime.freeMemory()
            return dalvikHeapMem
        }

    /**
     * 获取应用能够获取的max dalvik堆内存大小
     * 和Runtime.getRuntime().maxMemory()一样
     *
     * @return 单位M
     */
    fun getAppTotalDalvikHeapSize(context: Context): Long {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.memoryClass.toLong()
    }

    /**
     * 获取手机RAM容量/手机实际内存
     * 单位
     */
    private fun getRamTotalMem(context: Context, onGetRamTotalMemCallback: OnGetRamTotalMemCallback) {
        Thread {
            val totalRam = getRamTotalMemSync(context)
            Handler(Looper.getMainLooper()).post { onGetRamTotalMemCallback.onGetRamTotalMem(totalRam) }
        }.start()
    }

    /**
     * 同步获取系统的总ram大小
     */
    private fun getRamTotalMemSync(context: Context): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            mi.totalMem
        } else if (sTotalMem.get() > 0L) { //如果已经从文件获取过值，则不需要再次获取
            sTotalMem.get()
        } else {
            val tm = ramTotalMemByFile
            sTotalMem.set(tm)
            tm
        }
    }

    private val sTotalMem = AtomicLong(0L)
    private val ramTotalMemByFile: Long
        /**
         * 获取手机的RAM容量，其实和activityManager.getMemoryInfo(mi).totalMem效果一样，
         * 也就是说，在API16以上使用系统API获取，低版本采用这个文件读取方式
         *
         * @return 容量KB
         */
        private get() {
            val dir = "/proc/meminfo"
            try {
                val fr = FileReader(dir)
                val br = BufferedReader(fr, 2048)
                val memoryLine = br.readLine()
                val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
                br.close()
                return subMemoryLine.replace("\\D+".toRegex(), "").toInt().toLong()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return 0L
        }

    /**
     * 格式化单位
     * @param size
     * @return
     */
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "Byte"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(java.lang.Double.toString(megaByte))
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }

    /**
     * Dalvik堆内存，只要App用到的内存都算（包括共享内存）
     */
    class DalvikHeapMem {
        var freeMem: Long = 0
        var maxMem: Long = 0
        var allocated: Long = 0
    }

    /**
     * 应用实际占用内存（共享按比例分配）
     */
    class PssInfo {
        var totalPss = 0
        var dalvikPss = 0
        var nativePss = 0
        var otherPss = 0
    }

    /**
     * 手机RAM内存信息
     * 物理内存信息
     */
    class RamMemoryInfo {
        //可用RAM
        var availMem: Long = 0

        //手机总RAM
        var totalMem: Long = 0

        //内存占用满的阀值，超过即认为低内存运行状态，可能会Kill process
        var lowMemThreshold: Long = 0

        //是否低内存状态运行
        var isLowMemory = false
    }

    /**
     * 内存相关的所有数据
     */
    interface OnGetMemoryInfoCallback {
        fun onGetMemoryInfo(pkgName: String?, pid: Int, ramMemoryInfo: RamMemoryInfo?, pssInfo: PssInfo?, dalvikHeapMem: DalvikHeapMem?)
    }

    interface OnGetRamMemoryInfoCallback {
        fun onGetRamMemoryInfo(ramMemoryInfo: RamMemoryInfo?)
    }

    private interface OnGetRamTotalMemCallback {
        //手机总RAM容量/KB
        fun onGetRamTotalMem(totalMem: Long)
    }
}