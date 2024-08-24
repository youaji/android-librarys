@file:Suppress("unused")
package com.youaji.libs.ui.vm

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

/**
 * @author youaji
 * @since 2022/11/18
 */
object ViewModelUtil {

    @SuppressWarnings("unchecked")
    fun <VM : ViewModel> createViewModel(
        activity: ComponentActivity,
        factory: ViewModelProvider.Factory? = null,
        position: Int
    ): VM {
        val vbClass =
            (activity.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<*>>()
        val viewModel = vbClass[position] as Class<VM>
        return factory?.let {
            ViewModelProvider(
                activity,
                factory
            )[viewModel]
        } ?: let {
            ViewModelProvider(activity)[viewModel]
        }
    }

    @SuppressWarnings("unchecked")
    fun <VM : ViewModel> createViewModel(
        fragment: Fragment,
        factory: ViewModelProvider.Factory? = null,
        position: Int
    ): VM {
        val vbClass =
            (fragment.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<*>>()
        val viewModel = vbClass[position] as Class<VM>
        return factory?.let {
            ViewModelProvider(
                fragment,
                factory
            )[viewModel]
        } ?: let {
            ViewModelProvider(fragment)[viewModel]
        }
    }

    @SuppressWarnings("unchecked")
    fun <VM : ViewModel> createActivityViewModel(
        fragment: Fragment,
        factory: ViewModelProvider.Factory? = null,
        position: Int
    ): VM {
        val vbClass =
            (fragment.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<*>>()
        val viewModel = vbClass[position] as Class<VM>
        return factory?.let {
            ViewModelProvider(
                fragment.requireActivity(),
                factory
            )[viewModel]
        } ?: let {
            ViewModelProvider(fragment.requireActivity())[viewModel]
        }
    }


}