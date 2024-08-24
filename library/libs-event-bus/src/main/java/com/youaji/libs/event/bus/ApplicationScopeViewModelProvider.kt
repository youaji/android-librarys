package com.youaji.libs.event.bus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

object ApplicationScopeViewModelProvider : ViewModelStoreOwner {

    private val eventViewModelStore: ViewModelStore = ViewModelStore()
    private val applicationProvider: ViewModelProvider by lazy {
        ViewModelProvider(
            ApplicationScopeViewModelProvider,
            ViewModelProvider.AndroidViewModelFactory.getInstance(EventBus.application)
        )
    }

    override fun getViewModelStore(): ViewModelStore =
        eventViewModelStore

    fun <T : ViewModel> getApplicationScopeViewModel(modelClass: Class<T>): T =
        applicationProvider[modelClass]
}