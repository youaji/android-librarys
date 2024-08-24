package com.youaji.libs.js.bridge.impl

import android.util.Log
import com.youaji.libs.js.bridge.interfaces.BridgeHandler
import com.youaji.libs.js.bridge.interfaces.OnBridgeCallback

class DefaultHandler : BridgeHandler {
    override fun handler(data: String, callBackFunction: OnBridgeCallback) {
        Log.d("DefaultHandler", "data:$data")
        callBackFunction.onCallBack("DefaultHandler handler")
    }
}