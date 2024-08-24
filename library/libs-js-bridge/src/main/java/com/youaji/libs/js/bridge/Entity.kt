package com.youaji.libs.js.bridge

data class JSRequest(
    val data: String,
    var callbackID: String? = null,
    var handlerName: String? = null,
)

data class JSResponse(
    val responseID: String,
    val responseData: String,
)