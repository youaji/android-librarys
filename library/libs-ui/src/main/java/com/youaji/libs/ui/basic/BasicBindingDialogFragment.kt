@file:Suppress("unused")
package com.youaji.libs.ui.basic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.viewbinding.ViewBinding

import com.youaji.libs.ui.binding.FragmentBinding
import com.youaji.libs.ui.binding.FragmentBindingDelegate
import com.youaji.libs.ui.state.Decorative
import com.youaji.libs.ui.state.LoadingState
import com.youaji.libs.ui.state.LoadingStateDelegate
import com.youaji.libs.ui.state.OnReloadListener

/**
 * @author youaji
 * @since 2022/1/3
 */
abstract class BasicBindingDialogFragment<VB : ViewBinding> : BasicDialogFragment(),
    LoadingState by LoadingStateDelegate(),
    OnReloadListener,
    Decorative,
    FragmentBinding<VB> by FragmentBindingDelegate() {

    override fun createView(context: Context?, inflater: LayoutInflater, container: ViewGroup?): View {
        return createViewWithBinding(inflater, container).decorate(this, this)
    }

}