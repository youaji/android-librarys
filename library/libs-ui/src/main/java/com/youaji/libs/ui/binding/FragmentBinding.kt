@file:Suppress("unused")
package com.youaji.libs.ui.binding

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

/**
 * @author youaji
 * @since 2022/9/13
 */
interface FragmentBinding<VB : ViewBinding> {
    val binding: VB
    fun Fragment.createViewWithBinding(inflater: LayoutInflater, container: ViewGroup?): View
}

class FragmentBindingDelegate<VB : ViewBinding> : FragmentBinding<VB> {
    private var _binding: VB? = null
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override val binding: VB
        get() = requireNotNull(_binding) { "绑定的属性已被破坏！" }

    override fun Fragment.createViewWithBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        if (_binding == null) {
            _binding = ViewBindingUtil.inflateWithGeneric(this, inflater, container, false)
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    handler.post { _binding = null }
                }
            })
        }
        return _binding!!.root
    }
}