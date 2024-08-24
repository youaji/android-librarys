package com.youaji.libs.js.bridge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Message(
    var handlerName: String = "",
    var data: String = "",
    var callbackID: String = "",
    var responseID: String = "",
    var responseData: String = "",
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}

fun String.toMessageList(): List<Message> {
    return Gson().fromJson(this, object : TypeToken<List<Message>>() {}.type)
}