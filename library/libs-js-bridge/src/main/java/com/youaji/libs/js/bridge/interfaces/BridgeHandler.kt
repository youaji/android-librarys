package com.youaji.libs.js.bridge.interfaces

interface BridgeHandler {
    fun handler(data: String, callBackFunction: OnBridgeCallback)
}