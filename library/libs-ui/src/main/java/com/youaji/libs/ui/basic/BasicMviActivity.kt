@file:Suppress("unused")
package com.youaji.libs.ui.basic

import android.os.Bundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

import com.youaji.libs.ui.vm.ViewModelUtil

/**
 * @author youaji
 * @since 2022/11/18
 */
abstract class BasicMviActivity<VM : ViewModel, VB : ViewBinding>(
    private val viewModelFactory: ViewModelProvider.Factory? = null
) : BasicBindingActivity<VB>() {

    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelUtil.createViewModel(this, viewModelFactory, 0)
        initView()
        initViewStates()
        initViewEvents()
    }

    protected abstract fun initView()
    protected abstract fun initViewStates()
    protected abstract fun initViewEvents()
}