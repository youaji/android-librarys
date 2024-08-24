package com.youaji.libs.debug.file.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.core.content.FileProvider
import com.youaji.libs.util.isExistOrCreateNewFile
import java.io.File


object OpenFileUtil {
    private val matchMap = mapOf(
        ".apk" to "application/vnd.android.package-archive",
        ".bin" to "application/octet-stream",
        ".exe" to "application/octet-stream",
        ".class" to "application/octet-stream",
        ".doc" to "application/msword",
        ".docx" to "application/msword",
        ".xls" to "application/msword",
        ".xlsx" to "application/msword",
        ".gtar" to "application/x-gtar",
        ".tar" to "application/x-tar",
        ".gz" to "application/x-gzip",
        ".zip" to "application/zip",
        ".z" to "application/x-compress",
        ".tgz" to "application/x-compressed",
        ".jar" to "application/java-archive",
        ".rar" to "application/x-rar-compressed",
        ".js" to "application/x-javascript",
        ".mpc" to "application/vnd.mpohun.certificate",
        ".msg" to "application/vnd.ms-outlook",
        ".pdf" to "application/pdf",
        ".pps" to "application/vnd.ms-powerpoint",
        ".ppt" to "application/vnd.ms-powerpoint",
        ".wps" to "application/vnd.ms-works",
        ".rtf" to "application/rtf",
        ".c" to "text/plain",
        ".h" to "text/plain",
        ".cpp" to "text/plain",
        ".conf" to "text/plain",
        ".htm" to "text/html",
        ".html" to "text/html",
        ".java" to "text/plain",
        ".prop" to "text/plain",
        ".sh" to "text/plain",
        ".rc" to "text/plain",
        ".log" to "text/plain",
        ".txt" to "text/plain",
        ".xml" to "text/plain",
        ".m3u" to "audio/x-mpegurl",
        ".m4a" to "audio/mp4a-latm",
        ".m4b" to "audio/mp4a-latm",
        ".m4p" to "audio/mp4a-latm",
        ".mp2" to "audio/x-mpeg",
        ".mp3" to "audio/x-mpeg",
        ".mpga" to "audio/mpeg",
        ".ogg" to "audio/ogg",
        ".rmvb" to "audio/x-pn-realaudio",
        ".wav" to "audio/x-wav",
        ".wma" to "audio/x-ms-wma",
        ".wmv" to "audio/x-ms-wmv",
        ".m4u" to "video/vnd.mpegurl",
        ".3gp" to "video/3gpp",
        ".m4v" to "video/x-m4v",
        ".mov" to "video/quicktime",
        ".asf" to "video/x-ms-asf",
        ".avi" to "video/x-msvideo",
        ".mpg4" to "video/mp4",
        ".mp4" to "video/mp4",
        ".mpe" to "video/mpeg",
        ".mpeg" to "video/mpeg",
        ".mpg" to "video/mpeg",
        ".bmp" to "image/bmp",
        ".gif" to "image/gif",
        ".jpeg" to "image/jpeg",
        ".jpg" to "image/jpeg",
        ".png" to "image/png",
    )
//        arrayOf("", "*/*")

    fun open(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //判断7.0以上
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, context.packageName + ".fileExplorerProvider", file)
        } else {
            Uri.fromFile(file)
        }
        intent.setDataAndType(uri, matchType(file.path))
        context.startActivity(intent)
    }

    private fun matchType(filePath: String): String {
        var type = "*/*"
        matchMap.keys.forEach {
            if (filePath.endsWith(it)) {
                matchMap[it]?.let { t -> type = t }
                return type
            }
        }
        return type
    }
    /**
     * 根据路径打开文件
     * @param context 上下文
     * @param path 文件路径
     */
//    fun openFileByPath(context: Context?, path: String?) {
//        if (context == null || path == null) return
//        val intent = Intent()
//        //设置intent的Action属性
//        intent.action = Intent.ACTION_VIEW
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.addCategory(Intent.CATEGORY_DEFAULT)
//
//        //文件的类型
//        var type = ""
//        for (i in MATCH_ARRAY.indices) {
//            //判断文件的格式
//            if (path.contains(MATCH_ARRAY[i][0])) {
//                type = MATCH_ARRAY[i][1]
//                break
//            }
//        }
//        try {
//            val out = File(path)
//            val fileURI: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                // 由于7.0以后文件访问权限，可以通过定义xml在androidmanifest中申请，也可以直接跳过权限
//                // 通过定义xml在androidmanifest中申请
//                //                fileURI = FileProvider.getUriForFile(context,
//                //                        "com.lonelypluto.zyw_test.provider",
//                //                        out);
//                // 直接跳过权限
//                val builder = VmPolicy.Builder()
//                StrictMode.setVmPolicy(builder.build())
//                Uri.fromFile(out)
//            } else {
//                Uri.fromFile(out)
//            }
//            //设置intent的data和Type属性
//            intent.setDataAndType(fileURI, type)
//            //跳转
//            if (context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
//                context.startActivity(intent)
//            } else {
//                Toast.makeText(context, "没有找到对应的程序", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) { //当系统没有携带文件打开软件，提示
//            Toast.makeText(context, "无法打开该格式文件", Toast.LENGTH_SHORT).show()
//            e.printStackTrace()
//        }
//    }

}