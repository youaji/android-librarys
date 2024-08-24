package com.youaji.libs.js.bridge

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.youaji.libs.js.bridge.BridgeUtil.loadJavascript2WebView

/**
 * 如果要自定义WebViewClient必须要集成此类
 * Created by bruce on 10/28/15.
 */
class BridgeWebViewClient(private val loadJavaScript: OnLoadJavaScript) : WebViewClient() {

    interface OnLoadJavaScript {
        fun onLoadJavaScriptStart()
        fun onLoadJavaScriptFinish()
    }

    private var webViewClient: WebViewClient? = null
    fun setWebViewClient(client: WebViewClient?) {
        webViewClient = client
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return webViewClient?.shouldOverrideUrlLoading(view, url)
            ?: if (BridgeUtil.interceptUrl(url)) true else super.shouldOverrideUrlLoading(view, url)
    }

    //    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            webViewClient?.shouldOverrideUrlLoading(view, request)
//                ?: if (interceptUrl(request.url)) true else super.shouldOverrideUrlLoading(view, request)
//        } else {
//            super.shouldOverrideUrlLoading(view, request)
//        }
//    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        webViewClient?.onPageStarted(view, url, favicon) ?: super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        webViewClient?.onPageFinished(view, url) ?: super.onPageFinished(view, url)
        loadJavaScript.onLoadJavaScriptStart()
        loadJavascript2WebView(view)
        loadJavaScript.onLoadJavaScriptFinish()
    }

    override fun onLoadResource(view: WebView, url: String) {
        webViewClient?.onLoadResource(view, url) ?: super.onLoadResource(view, url)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onPageCommitVisible(view: WebView, url: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webViewClient?.onPageCommitVisible(view, url) ?: super.onPageCommitVisible(view, url)
        } else {
            super.onPageCommitVisible(view, url)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return webViewClient?.shouldInterceptRequest(view, url) ?: super.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return webViewClient?.shouldInterceptRequest(view, request) ?: super.shouldInterceptRequest(view, request)
    }

    @Deprecated("Deprecated in Java")
    override fun onTooManyRedirects(view: WebView, cancelMsg: Message, continueMsg: Message) {
        webViewClient?.onTooManyRedirects(view, cancelMsg, continueMsg) ?: super.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        webViewClient?.onReceivedError(view, errorCode, description, failingUrl) ?: super.onReceivedError(view, errorCode, description, failingUrl)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webViewClient?.onReceivedError(view, request, error) ?: super.onReceivedError(view, request, error)
        } else {
            super.onReceivedError(view, request, error)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webViewClient?.onReceivedHttpError(view, request, errorResponse) ?: super.onReceivedHttpError(view, request, errorResponse)
        } else {
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }

    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
        webViewClient?.onFormResubmission(view, dontResend, resend) ?: super.onFormResubmission(view, dontResend, resend)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        webViewClient?.doUpdateVisitedHistory(view, url, isReload) ?: super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        webViewClient?.onReceivedSslError(view, handler, error) ?: super.onReceivedSslError(view, handler, error)
    }

    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        webViewClient?.onReceivedClientCertRequest(view, request) ?: super.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
        webViewClient?.onReceivedHttpAuthRequest(view, handler, host, realm) ?: super.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
        return webViewClient?.shouldOverrideKeyEvent(view, event) ?: super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
        webViewClient?.onUnhandledKeyEvent(view, event) ?: super.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
        webViewClient?.onScaleChanged(view, oldScale, newScale) ?: super.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(view: WebView, realm: String, account: String?, args: String) {
        webViewClient?.onReceivedLoginRequest(view, realm, account, args) ?: super.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            webViewClient?.onRenderProcessGone(view, detail) ?: super.onRenderProcessGone(view, detail)
        else
            super.onRenderProcessGone(view, detail)
    }

    override fun onSafeBrowsingHit(view: WebView, request: WebResourceRequest, threatType: Int, callback: SafeBrowsingResponse) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            webViewClient?.onSafeBrowsingHit(view, request, threatType, callback) ?: super.onSafeBrowsingHit(view, request, threatType, callback)
        } else {
            super.onSafeBrowsingHit(view, request, threatType, callback)
        }
    }
}