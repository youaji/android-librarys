package com.youaji.libs.js.bridge

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.youaji.libs.js.bridge.interfaces.OnBridgeCallback
import com.youaji.libs.js.bridge.interfaces.WebViewJavascriptBridge
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
class BridgeWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr), WebViewJavascriptBridge, BridgeWebViewClient.OnLoadJavaScript {

    private val tag = BridgeWebView::class.java.simpleName
    private var uniqueId = 0L
    private val urlMaxCharacterNum = 2097152
    private val bridgeWebViewClient: BridgeWebViewClient
    private val gson by lazy { Gson() }

    /** 启动JavaScript时的消息 */
    private var startupMessageList = mutableListOf<Any>()

    /** 是否已加载JavaScript */
    private var isLoadedJavaScript = false

    val callbacks = mutableMapOf<String, OnBridgeCallback>()

    init {
        clearCache(true)
        settings.useWideViewPort = true
        //		webView.getSettings().setLoadWithOverviewMode(true);
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.javaScriptEnabled = true
        //        mContent.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        settings.javaScriptCanOpenWindowsAutomatically = true
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
//            WebView.setWebContentsDebuggingEnabled(true)
//        }
        bridgeWebViewClient = BridgeWebViewClient(this)
        super.setWebViewClient(bridgeWebViewClient)
    }

    override fun setWebViewClient(client: WebViewClient) {
        bridgeWebViewClient.setWebViewClient(client)
    }

    override fun destroy() {
        callbacks.clear()
        super.destroy()
    }

    override fun onLoadJavaScriptStart() {
        isLoadedJavaScript = false
        Log.d(tag, "start load JavaScript file!")
    }

    override fun onLoadJavaScriptFinish() {
        isLoadedJavaScript = true
        startupMessageList.forEach { dispatchMessage(it) }
        startupMessageList.clear()
        Log.d(tag, "load JavaScript file finish!")
    }

    override fun send2Web(data: String) {
        send2Web(data, null)
    }

    override fun send2Web(function: String, vararg values: Any) {
        /* 必须主线程才可成功传递数据 */
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            var jsCommand = String.format(function, *values)
            jsCommand = String.format(BridgeUtil.formatJavaScriptStr, jsCommand)
            loadUrl(jsCommand)
        }
    }

    override fun send2Web(data: String, responseCallback: OnBridgeCallback?) {
        doSend(null, data, responseCallback)
    }

    /**
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    OnBridgeCallback
     */
    fun callHandler(handlerName: String?, data: String, callBack: OnBridgeCallback?) {
        doSend(handlerName, data, callBack)
    }

    fun sendResponse(data: String, callbackId: String) {
        if (callbackId.isNotEmpty()) {
            val response = JSResponse(callbackId, data)
            if (Thread.currentThread() === Looper.getMainLooper().thread) {
                dispatchMessage(response)
            } else {
                post { dispatchMessage(response) }
            }
        }
    }

    /**
     * 保存 message 到消息队列
     *
     * @param handlerName      handlerName
     * @param data             data
     * @param responseCallback OnBridgeCallback
     */
    private fun doSend(handlerName: String?, data: String, responseCallback: OnBridgeCallback?) {
        val request = JSRequest(data)
        responseCallback?.let {
            val callbackId = String.format(
                BridgeUtil.formatCallbackID,
                "${++uniqueId}${BridgeUtil.strUnderline}${SystemClock.currentThreadTimeMillis()}"
            )
            callbacks[callbackId] = it
            request.callbackID = callbackId
        }

        if (handlerName?.isNotEmpty() == true) {
            request.handlerName = handlerName
        }
        queueMessage(request)
    }

    private fun queueMessage(message: Any) {
        if (isLoadedJavaScript) {
            dispatchMessage(message)
        } else {
            startupMessageList.add(message)
        }
    }

    /**
     * 分发 message 必须在主线程方可分发成功
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun dispatchMessage(message: Any) {
        var messageJson = gson.toJson(message)
        // escape special characters for json string  为 json 字符串转义特殊字符
        // 系统原生 API 做 Json转义，没必要自己正则替换，而且替换不一定完整
        messageJson = JSONObject.quote(messageJson)
        val javascriptCommand = String.format(BridgeUtil.javaScriptHandleMessageFromJava, messageJson)
        // 必须主线程才可成功传递数据
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && javascriptCommand.length >= urlMaxCharacterNum) {
                this.evaluateJavascript(javascriptCommand, null)
            } else {
                this.loadUrl(javascriptCommand)
            }
        }
    }

}