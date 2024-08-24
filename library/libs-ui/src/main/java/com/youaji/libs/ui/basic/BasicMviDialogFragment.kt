@file:Suppress("unused")
package com.youaji.libs.ui.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

import com.youaji.libs.ui.vm.ViewModelUtil

/**
 * @author youaji
 * @since 2023/1/30
 */
abstract class BasicMviDialogFragment<VM : ViewModel, VB : ViewBinding>(
    private val shareViewModel: Boolean = false,
    private val viewModelFactory: ViewModelProvider.Factory? = null
) : BasicBindingDialogFragment<VB>() {

    protected lateinit var viewModel: VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = if (shareViewModel) {
            ViewModelUtil.createActivityViewModel(this, viewModelFactory, 0)
        } else {
            ViewModelUtil.createViewModel(this, viewModelFactory, 0)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewStates()
        initViewEvents()
    }

    protected abstract fun initView()
    protected abstract fun initViewStates()
    protected abstract fun initViewEvents()
}