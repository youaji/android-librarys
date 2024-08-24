package com.youaji.libs.js.bridge.impl

import android.os.Looper
import android.os.SystemClock
import android.webkit.WebView
import com.youaji.libs.js.bridge.BridgeUtil
import com.youaji.libs.js.bridge.Message
import com.youaji.libs.js.bridge.interfaces.BridgeHandler
import com.youaji.libs.js.bridge.interfaces.OnBridgeCallback
import com.youaji.libs.js.bridge.interfaces.WebViewJavascriptBridge
import com.youaji.libs.js.bridge.toMessageList
import java.net.URLEncoder

class BridgeHelper(private val webView: WebView) : WebViewJavascriptBridge {

    private var uniqueId: Long = 0
    private val responseCallbacks = mutableMapOf<String, OnBridgeCallback>()
    private val messageHandlers = mutableMapOf<String, BridgeHandler>()
    private var defaultHandler: BridgeHandler = DefaultHandler()
    private val startupMessageList = mutableListOf<Message>()
    private var isLoadedJavaScript = false

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
        doSend(data, responseCallback)
    }

    fun setDefaultHandler(handler: BridgeHandler) {
        defaultHandler = handler
    }

    fun registerHandler(handlerName: String, handler: BridgeHandler?) {
        handler?.let { messageHandlers[handlerName] = handler }
    }

    fun unregisterHandler(handlerName: String?) {
        handlerName?.let { messageHandlers.remove(handlerName) }
    }

    fun callHandler(handlerName: String, data: String, callBack: OnBridgeCallback?) {
        doSend(data, callBack, handlerName)
    }

    fun onPageFinished() {
        isLoadedJavaScript = false
        BridgeUtil.loadJavascript2WebView(webView)
        isLoadedJavaScript = true
        startupMessageList.forEach { dispatchMessage(it) }
        startupMessageList.clear()
    }

    fun shouldOverrideUrlLoading(url: String): Boolean {
        val replacedUrl = url.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25").replace("\\+".toRegex(), "%2B")
        return BridgeUtil.interceptUrl(replacedUrl) { u, isReturnData, isSchema ->
            if (isReturnData) {
                handlerReturnData(u)
            } else if (isSchema) {
                flushMessageQueue()
            }
        }
    }

    private fun handlerReturnData(url: String) {
        val functionName = BridgeUtil.getFunctionFromReturnUrl(url)
        val function = responseCallbacks[functionName]
        val data = BridgeUtil.getDataFromReturnUrl(url)
        function?.let {
            function.onCallBack(data)
            responseCallbacks.remove(functionName)
        }
    }

    private fun doSend(data: String, responseCallback: OnBridgeCallback?, handlerName: String = "") {
        val message = Message()
        if (data.isNotEmpty()) {
            message.data = data
        }
        responseCallback?.let {
            val callbackId = String.format(
                BridgeUtil.formatCallbackID,
                "${++uniqueId}${BridgeUtil.strUnderline}${SystemClock.currentThreadTimeMillis()}"
            )
            responseCallbacks[callbackId] = responseCallback
            message.callbackID = callbackId
        }
        if (handlerName.isNotEmpty()) {
            message.handlerName = handlerName
        }
        queueMessage(message)
    }

    private fun queueMessage(message: Message) {
        if (isLoadedJavaScript) {
            dispatchMessage(message)
        } else {
            startupMessageList.add(message)
        }
    }

    private fun dispatchMessage(message: Message) {
        var messageJson = message.toJson()
        //escape special characters for json string  为json字符串转义特殊字符
        messageJson = messageJson.replace("(\\\\)([^utrn])".toRegex(), "\\\\\\\\$1$2")
        messageJson = messageJson.replace("(?<=[^\\\\])(\")".toRegex(), "\\\\\"")
        messageJson = messageJson.replace("(?<=[^\\\\])(\')".toRegex(), "\\\\\'")
        messageJson = messageJson.replace("%7B".toRegex(), URLEncoder.encode("%7B"))
        messageJson = messageJson.replace("%7D".toRegex(), URLEncoder.encode("%7D"))
        messageJson = messageJson.replace("%22".toRegex(), URLEncoder.encode("%22"))
        val javascriptCommand = String.format(BridgeUtil.javaScriptHandleMessageFromJava, messageJson)
        // 必须主线程才可成功传递数据
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            loadUrl(javascriptCommand)
        }
    }

    private fun flushMessageQueue() {
        if (Thread.currentThread() != Looper.getMainLooper().thread)
            return
        loadUrl(BridgeUtil.javaScriptFetchQueueFromJava, object : OnBridgeCallback {
            override fun onCallBack(data: String) {
                // deserializeMessage 反序列化消息
                val messageList: List<Message>? =
                    try {
                        data.toMessageList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return
                    }
                if (messageList.isNullOrEmpty())
                    return
                messageList.forEach { message ->
                    val responseId = message.responseID
                    // 是否是 response  CallBackFunction
                    if (responseId.isNotEmpty()) {
                        val function = responseCallbacks[responseId]
                        val responseData = message.responseData
                        function?.onCallBack(responseData)
                        responseCallbacks.remove(responseId)
                    } else {
                        // if had callbackId 如果有回调Id
                        val callbackId = message.callbackID
                        val responseFunction =
                            if (callbackId.isNotEmpty()) {
                                object : OnBridgeCallback {
                                    override fun onCallBack(data: String) {
                                        val responseMsg = Message()
                                        responseMsg.responseID = callbackId
                                        responseMsg.responseData = data
                                        queueMessage(responseMsg)
                                    }
                                }
                            } else {
                                object : OnBridgeCallback {
                                    override fun onCallBack(data: String) {
                                        // do nothing
                                    }
                                }
                            }
                        // BridgeHandler执行
                        val handler =
                            if (message.handlerName.isNotEmpty()) {
                                messageHandlers[message.handlerName]
                            } else {
                                defaultHandler
                            }
                        handler?.handler(message.data, responseFunction)
                    }
                }
            }
        })
    }

    private fun loadUrl(jsUrl: String, returnCallback: OnBridgeCallback? = null) {
        webView.loadUrl(jsUrl)
        returnCallback?.let {
            // 添加至 Map<String, CallBackFunction>
            responseCallbacks[BridgeUtil.parseFunctionName(jsUrl)] = it
        }
    }
}