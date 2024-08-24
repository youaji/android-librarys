@file:Suppress("unused")
package com.youaji.libs.ui.state

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

class LoadingStateDelegate : LoadingState {
  private var loadingStateView: LoadingStateView? = null

  override fun Activity.decorateContentView(listener: OnReloadListener?, decorative: Decorative?) {
    findViewById<ViewGroup>(android.R.id.content).getChildAt(0).decorate(listener, decorative)
  }

  override fun View.decorate(listener: OnReloadListener?, decorative: Decorative?): View =
    when {
      decorative?.isDecorated == false -> this
      decorative?.contentView == null ->
        LoadingStateView(this, listener).also { loadingStateView = it }.decorView
      else -> {
        loadingStateView = LoadingStateView(decorative.contentView!!, listener)
        this
      }
    }

  override fun registerView(vararg viewDelegates: LoadingStateView.ViewDelegate) {
    loadingStateView?.register(*viewDelegates)
  }

  override fun Activity.setToolbar(@StringRes titleId: Int, navBtnType: NavBtnType, block: (ToolbarConfig.() -> Unit)?) {
    setToolbar(getString(titleId), navBtnType, block)
  }

  override fun Activity.setToolbar(title: String?, navBtnType: NavBtnType, block: (ToolbarConfig.() -> Unit)?) {
    loadingStateView?.setHeaders(ToolbarViewDelegate(title, navBtnType, block))
  }

  override fun Fragment.setToolbar(@StringRes titleId: Int, navBtnType: NavBtnType, block: (ToolbarConfig.() -> Unit)?) {
    setToolbar(getString(titleId), navBtnType, block)
  }

  override fun Fragment.setToolbar(title: String?, navBtnType: NavBtnType, block: (ToolbarConfig.() -> Unit)?) {
    loadingStateView?.setHeaders(ToolbarViewDelegate(title, navBtnType, block))
  }

  override fun Activity.setHeaders(vararg delegates: LoadingStateView.ViewDelegate) {
    loadingStateView?.setHeaders(*delegates)
  }

  override fun Fragment.setHeaders(vararg delegates: LoadingStateView.ViewDelegate) {
    loadingStateView?.addChildHeaders(*delegates)
  }

  override fun Activity.setDecorView(delegate: LoadingStateView.DecorViewDelegate) {
    loadingStateView?.setDecorView(delegate)
  }

  override fun Fragment.setDecorView(delegate: LoadingStateView.DecorViewDelegate) {
    loadingStateView?.addChildDecorView(delegate)
  }

  override fun showLoadingView() {
    loadingStateView?.showLoadingView()
  }

  override fun showContentView() {
    loadingStateView?.showContentView()
  }

  override fun showErrorView() {
    loadingStateView?.showErrorView()
  }

  override fun showEmptyView() {
    loadingStateView?.showEmptyView()
  }

  override fun showCustomView(viewType: Any) {
    loadingStateView?.showView(viewType)
  }

  override fun updateToolbar(block: ToolbarConfig.() -> Unit) {
    updateView<BasicToolbarViewDelegate>(ViewType.TITLE) { onBindToolbar(config.apply(block)) }
  }

  override fun <T : LoadingStateView.ViewDelegate> updateView(viewType: Any, block: T.() -> Unit) {
    loadingStateView?.updateViewDelegate(viewType, block)
  }

  override fun ToolbarViewDelegate(title: String?, navBtnType: NavBtnType, block: (ToolbarConfig.() -> Unit)?) =
    requireNotNull(loadingStateView?.getViewDelegate<BasicToolbarViewDelegate>(ViewType.TITLE)) {
      "ToolbarViewDelegate must be registered before."
    }.apply {
      config = ToolbarConfig(title, navBtnType).apply { block?.invoke(this) }
    }
}