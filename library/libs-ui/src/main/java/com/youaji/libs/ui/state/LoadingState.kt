@file:Suppress("unused")
package com.youaji.libs.ui.state

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

interface LoadingState {

  fun Activity.decorateContentView(listener: OnReloadListener? = null, decorative: Decorative? = null)

  fun View.decorate(listener: OnReloadListener? = null, decorative: Decorative? = null): View

  fun registerView(vararg viewDelegates: LoadingStateView.ViewDelegate)

  fun Activity.setToolbar(@StringRes titleId: Int, navBtnType: NavBtnType = NavBtnType.ICON, block: (ToolbarConfig.() -> Unit)? = null)

  fun Activity.setToolbar(title: String? = null, navBtnType: NavBtnType = NavBtnType.ICON, block: (ToolbarConfig.() -> Unit)? = null)

  fun Fragment.setToolbar(@StringRes titleId: Int, navBtnType: NavBtnType = NavBtnType.ICON, block: (ToolbarConfig.() -> Unit)? = null)

  fun Fragment.setToolbar(title: String? = null, navBtnType: NavBtnType = NavBtnType.ICON, block: (ToolbarConfig.() -> Unit)? = null)

  fun Activity.setHeaders(vararg delegates: LoadingStateView.ViewDelegate)

  fun Fragment.setHeaders(vararg delegates: LoadingStateView.ViewDelegate)

  fun Activity.setDecorView(delegate: LoadingStateView.DecorViewDelegate)

  fun Fragment.setDecorView(delegate: LoadingStateView.DecorViewDelegate)

  fun showLoadingView()

  fun showContentView()

  fun showErrorView()

  fun showEmptyView()

  fun showCustomView(viewType: Any)

  fun updateToolbar(block: ToolbarConfig.() -> Unit)

  fun <T : LoadingStateView.ViewDelegate> updateView(viewType: Any, block: T.() -> Unit)

  @Suppress("FunctionName")
  fun ToolbarViewDelegate(
    title: String? = null, navBtnType: NavBtnType = NavBtnType.ICON, block: (ToolbarConfig.() -> Unit)? = null
  ): BasicToolbarViewDelegate
}