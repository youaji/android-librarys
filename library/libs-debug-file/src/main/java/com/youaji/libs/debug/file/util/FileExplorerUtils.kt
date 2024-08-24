package com.youaji.libs.debug.file.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileExplorerUtils {
    const val DB = "db"
    const val SHARED_PREFS = "shared_prefs"
    const val XML = ".xml"
    const val JSON = ".json"
    const val IS_DEBUG = true

    /**
     * 日志
     * @param log                       日志
     */
    fun logInfo(log: String?) {
        if (IS_DEBUG) {
            Log.i("FileExplorer", log!!)
        }
    }

    /**
     * 日志
     * @param log                       日志
     */
    @JvmStatic
    fun logError(log: String?) {
        if (IS_DEBUG) {
            Log.e("FileExplorer", log!!)
        }
    }

    /**
     * 这个是获取文件的后缀名
     * @param file                      file文件
     * @return                          后缀名
     */
    fun getSuffix(file: File?): String {
        return if (file != null && file.exists()) file.name.substring(file.name.lastIndexOf(".") + 1)
            .lowercase(Locale.getDefault()) else ""
    }

    /**
     * 文件创建时间，方便测试查看缓存文件的最后修改时间
     *
     * @param file    文件
     */
    fun getFileTime(file: File?): String {
        if (file != null && file.exists()) {
            val lastModified = file.lastModified()
            return SimpleDateFormat("yyyy-MM-dd")
                .format(Date(lastModified))
        }
        return ""
    }

    /**
     * 系统分享文件
     * 需要使用Provider
     * @param context               上下文
     * @param file                  文件
     */
    @JvmStatic
    fun shareFile(context: Context, file: File?): Boolean {
        return try {
            if (null != file && file.exists()) {
                val share = Intent(Intent.ACTION_SEND)
                val uri: Uri
                //判断7.0以上
                uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(context, context.packageName + ".crashFileProvider", file)
                } else {
                    Uri.fromFile(file)
                }
                share.putExtra(Intent.EXTRA_STREAM, uri)
                //此处可发送多种文件
                share.type = getMimeType(file.absolutePath)
                share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(share, "分享文件"))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    private fun getMimeType(filePath: String?): String? {
        val mmr = MediaMetadataRetriever()
        var mime: String? = "*/*"
        if (filePath != null) {
            mime = try {
                mmr.setDataSource(filePath)
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            } catch (e: IllegalStateException) {
                return mime
            } catch (e: IllegalArgumentException) {
                return mime
            } catch (e: RuntimeException) {
                return mime
            }
        }
        return mime
    }

    /**
     * 复制内容到剪切板
     * @param context                   上下文
     * @param content                   内容
     */
    fun copyToClipBoard(context: Context, content: String?): Boolean {
        if (!TextUtils.isEmpty(content)) {
            //获取剪贴版
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            //创建ClipData对象
            //第一个参数只是一个标记，随便传入。
            //第二个参数是要复制到剪贴版的内容
            val clip = ClipData.newPlainText("", content)
            //传入clipdata对象.
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip)
                return true
            }
        }
        return false
    }

    /**
     * 是否是db文件
     * @param file                      文件
     * @return
     */
    fun isDB(file: File?): Boolean {
        return if (file == null) {
            false
        } else {
            val suffix = getSuffix(file)
            DB == suffix
        }
    }

    /**
     * 是否是sp文件
     * @param file                      文件
     * @return
     */
    fun isSp(file: File): Boolean {
        val parentFile = file.parentFile
        return parentFile != null && parentFile.name == SHARED_PREFS && file.name.contains(XML)
    }

    /**
     * 是否是图片文件
     *
     * @param file 文件
     * @return
     */
    fun isImage(file: File?): Boolean {
        if (file == null) {
            return false
        }
        val suffix = getSuffix(file)
        return "jpg" == suffix || "jpeg" == suffix || "png" == suffix || "bmp" == suffix
    }

    /**
     * 将文件大小转化为具体的kb单位
     * @param size                          大小，字节
     * @return
     */
    fun getPrintSizeForSpannable(size: Long): SpannableString {
        var size = size
        val spannableString: SpannableString
        val sizeSpan = RelativeSizeSpan(0.5f)
        if (size < 1024) {
            spannableString = SpannableString(size.toString() + "B")
            spannableString.setSpan(sizeSpan, spannableString.length - 1, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return spannableString
        } else {
            size = size / 1024
        }
        if (size < 1024) {
            spannableString = SpannableString(size.toString() + "KB")
            spannableString.setSpan(sizeSpan, spannableString.length - 2, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            return spannableString
        } else {
            size = size / 1024
        }
        return if (size < 1024) {
            size = size * 100
            val string = ((size / 100).toString() + "."
                    + (size % 100).toString() + "MB")
            spannableString = SpannableString(string)
            spannableString.setSpan(sizeSpan, spannableString.length - 2, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString
        } else {
            size = size * 100 / 1024
            val string = ((size / 100).toString() + "."
                    + (size % 100).toString() + "GB")
            spannableString = SpannableString(string)
            spannableString.setSpan(sizeSpan, spannableString.length - 2, spannableString.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            spannableString
        }
    }

    /**
     * 删除文件
     * @param file                      文件
     */
    fun deleteDirectory(file: File?): Boolean {
        if (file != null) {
            if (file.isDirectory) {
                val listFiles = file.listFiles()
                val length = listFiles.size
                for (i in 0 until length) {
                    val f = listFiles[i]
                    deleteDirectory(f)
                }
            }
            file.delete()
        }
        // 如果删除的文件路径所对应的文件存在，并且是一个文件，则表示删除失败
        return if (file != null && file.exists() && file.isFile) {
            false
        } else {
            //删除成功
            true
        }
    }

    /** @return 删除单个文件是否成功 */
    fun deleteFile(file: File): Boolean {
        return if (file.exists()) {
            if (file.isFile) file.delete()
            else deleteAllFiles(file)
        } else false
    }

    /** 删除该目录所有文件 */
    private fun deleteAllFiles(dir: File): Boolean {
        var result = false
        dir.listFiles()?.forEach { f ->
            if (f.isDirectory) {
                result = deleteAllFiles(f)
                try {
                    result = f.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                if (f.exists()) {
                    result = deleteAllFiles(f)
                    try {
                        result = f.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return result
    }

    /**
     * 获取文件大小
     * @param directory                 文件
     * @return
     */
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        val listFiles = directory.listFiles()
        if (listFiles != null) {
            val length = listFiles.size
            for (i in 0 until length) {
                val file = listFiles[i]
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        } else {
            //如果不是文件目录，则获取单个文件大小
            size = directory.length()
        }
        return size
    }

    @JvmStatic
    val fileSharePath: String
        /**
         * 获取分享路径地址
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

    /**
     * 根据文件路径拷贝文件
     *
     * @param src                                   源文件
     * @param dest                                  目标文件
     * @return                                      boolean 成功true、失败false
     */
    @JvmStatic
    fun copyFile(src: File?, dest: File?): Boolean {
        var result = false
        if (src == null || dest == null) {
            return false
        }
        if (dest.exists()) {
            // delete file
            dest.delete()
        }
        if (!createOrExistsDir(dest.parentFile)) {
            return false
        }
        try {
            dest.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var srcChannel: FileChannel? = null
        var dstChannel: FileChannel? = null
        try {
            srcChannel = FileInputStream(src).channel
            dstChannel = FileOutputStream(dest).channel
            srcChannel.transferTo(0, srcChannel.size(), dstChannel)
            result = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return result
        }
        try {
            srcChannel.close()
            dstChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 判断文件是否创建，如果没有创建，则新建
     * @param file                                  file文件
     * @return
     */
    fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }
}