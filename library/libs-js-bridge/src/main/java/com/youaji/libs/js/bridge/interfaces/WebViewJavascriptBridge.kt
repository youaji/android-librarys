package com.youaji.libs.js.bridge.interfaces

interface WebViewJavascriptBridge {
    fun send2Web(data: String)
    fun send2Web(function: String, vararg values: Any)
    fun send2Web(data: String, responseCallback: OnBridgeCallback?)
}