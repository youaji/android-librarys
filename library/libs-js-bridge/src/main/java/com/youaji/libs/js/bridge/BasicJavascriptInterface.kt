package com.youaji.libs.js.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import com.youaji.libs.js.bridge.interfaces.OnBridgeCallback

abstract class BasicJavascriptInterface(private val callbacks: MutableMap<String, OnBridgeCallback>) {
    @JavascriptInterface
    fun send(data: String, callbackId: String): String {
        Log.d(
            "BasicJavascriptInterface",
            "--- send ---\n" +
                    "currentThread:${Thread.currentThread().name}\n" +
                    "callbackId:$callbackId\n" +
                    "data:$data"
        )
        return send(data)
    }

    @JavascriptInterface
    fun response(data: String, responseId: String) {
        Log.d(
            "BasicJavascriptInterface",
            "--- response ---\n" +
                    "currentThread:${Thread.currentThread().name}\n" +
                    "responseId:$responseId\n" +
                    "data:$data"
        )
        if (responseId.isNotEmpty()) {
            callbacks.remove(responseId)?.onCallBack(data)
        }
    }

    abstract fun send(data: String): String
}