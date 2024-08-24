package com.youaji.libs.js.bridge

import android.content.Context
import android.webkit.WebView
import com.youaji.libs.js.bridge.interfaces.IWebView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object BridgeUtil {
    const val schema = "yy://"
    private const val returnData = schema + "return/" //格式为   yy://return/{function}/returnContent
    const val fetchQueue = returnData + "_fetchQueue/"
    const val strEmpty = ""
    const val strUnderline = "_"
    const val strSplitMark = "/"
    const val formatCallbackID = "JAVA_CB_%s"
    const val javaScriptHandleMessageFromJava = "javascript:WebViewJavascriptBridge._handleMessageFromNative(%s);"
    const val javaScriptFetchQueueFromJava = "javascript:WebViewJavascriptBridge._fetchQueue();"
    private const val javaScriptFile = "WebViewJavascriptBridge.js"
    const val formatJavaScriptStr = "javascript:%s"

    /**
     * js 文件将注入为第一个script引用
     */
    fun loadJavascript2WebView(view: WebView, url: String) {
        var js = "var newscript = document.createElement(\"script\");"
        js += "newscript.src=\"$url\";"
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);"
        view.loadUrl("javascript:$js")
    }

    /**
     * 这里只是加载 assets 中的 WebViewJavascriptBridge.js
     */
    @JvmStatic
    fun loadJavascript2WebView(view: WebView) {
        val jsContent = assetFile2Str(view.context, javaScriptFile)
        view.loadUrl("javascript:$jsContent")
    }

    /**
     * 解析 assets 文件夹里面的代码,去除注释,取可执行的代码
     */
    private fun assetFile2Str(context: Context, fileName: String): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            val sb = StringBuilder()
            do {
                line = bufferedReader.readLine()
                if (line != null && !line.matches("^\\s*\\/\\/.*".toRegex())) { // 去除注释
                    sb.append(line)
                }
            } while (line != null)
            bufferedReader.close()
            inputStream.close()
            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getFunctionFromReturnUrl(url: String?): String {
        return ""
    }

    fun getDataFromReturnUrl(url: String?): String {
        return ""
    }

    fun parseFunctionName(jsUrl: String?): String {
        return ""
    }

    fun interceptUrl(url: String, callback: ((url: String, isReturnData: Boolean, isSchema: Boolean) -> Unit)? = null): Boolean {
        var currUrl = url
        try {
            currUrl = URLDecoder.decode(currUrl, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        if (currUrl.startsWith(returnData)) { // 如果是返回数据
            callback?.invoke(currUrl, true, false)
            return true
        } else if (currUrl.startsWith(schema)) { //
            callback?.invoke(currUrl, false, true)
            return true
        }
        return false
    }
}