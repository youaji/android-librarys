package com.youaji.libs.debug.crash.util

import android.os.Environment
import com.youaji.libs.util.appVersionName
import com.youaji.libs.util.cacheDirPath
import com.youaji.libs.util.isExistOrCreateNewDir
import com.youaji.libs.util.isExistOrCreateNewFile
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.nio.channels.FileChannel

/**
 * 保存日志的工具类
 * @author youaji
 * @since 2024/01/05
 */
object FileUtil {

    /**
     * 应用缓存目录（优先外存）
     * 外存：SDCard/Android/data/<package>/cache
     * 内存：data/data/<package>/cache
     */
    private val cacheDir: String
        get() = cacheDirPath

    /** 崩溃文件存储目录 */
    val crashFileDir: String
        get() = "$cacheDir${File.separator}CrashLogs${File.separator}"

    /** 崩溃截图存储目录 */
    private val crashShotDir: String
        get() = "$cacheDir${File.separator}CrashShots${File.separator}"

    /** 崩溃截图文件 */
    val crashShotFile: String
        get() {
            File(crashShotDir).isExistOrCreateNewDir()
            return "${crashShotDir}crash_${System.currentTimeMillis()}.jpg"
        }

    val crashSharePath: String
        /**
         * 获取崩溃分享路径地址
         * @return                              路径
         */
        get() {
            val path = Environment.getExternalStorageDirectory().toString() + ""
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }

    fun deleteAllCrashFiles() {
        deleteAllFiles(File(crashFileDir))
    }

    fun getCrashFile(crashTime: String, crashName: String): File? {
        val fileName = "$appVersionName--$crashTime--$crashName.txt"
        val file = File(crashFileDir, fileName)
        return if (file.isExistOrCreateNewFile()) file else null
    }

    /** 获取所有已知崩溃文件 */
    fun getCrashFiles(): List<File> {
        val files = File(crashFileDir).listFiles()
        if (files.isNullOrEmpty()) return listOf()
        val fileList = mutableListOf<File>()
        files.forEach {
            if (it.isFile) fileList.add(it)
        }
        return fileList
    }

    /** @return 删除单个文件是否成功 */
    fun deleteFile(fileName: String): Boolean {
        val file = File(fileName)
        return if (file.exists() && file.isFile) file.delete()
        else false
    }

    /** 删除该目录所有文件 */
    private fun deleteAllFiles(dir: File) {
        dir.listFiles()?.forEach { f ->
            if (f.isDirectory) {
                deleteAllFiles(f)
                try {
                    f.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (f.exists()) {
                    deleteAllFiles(f)
                    try {
                        f.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /** 读取file文件，转化成字符串 */
    fun readFile(fileName: String?): String {
        var res = ""
        try {
            val inputStream = FileInputStream(fileName)
            var inputStreamReader: InputStreamReader? = null
            try {
                inputStreamReader = InputStreamReader(inputStream, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val reader = BufferedReader(inputStreamReader)
            val sb = StringBuilder("")
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                    sb.append("\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            res = sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return res
    }

    /** 重命名文件 */
    fun renameFile(oleFile: File?, newFile: File?) {
        if (oleFile == null || newFile == null) return
        oleFile.renameTo(newFile)
    }

    /**
     * 根据文件路径拷贝文件
     *
     * @param src                                   源文件
     * @param dest                                  目标文件
     * @return                                      boolean 成功true、失败false
     */
    fun copyFile(src: File?, dest: File?): Boolean {
        if (src == null || dest == null) return false
        if (dest.exists()) dest.delete()
        if (!dest.parentFile.isExistOrCreateNewDir()) return false
        try {
            dest.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var isSuccess = false
        var srcChannel: FileChannel? = null
        var dstChannel: FileChannel? = null
        try {
            srcChannel = FileInputStream(src).channel
            dstChannel = FileOutputStream(dest).channel
            srcChannel.transferTo(0, srcChannel.size(), dstChannel)
            isSuccess = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            srcChannel?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            dstChannel?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return isSuccess
    }
}