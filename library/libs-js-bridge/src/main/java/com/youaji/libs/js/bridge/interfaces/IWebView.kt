package com.youaji.libs.js.bridge.interfaces

import android.content.Context

interface IWebView {
    fun context(): Context
    fun loadWebUrl(url: String)
}