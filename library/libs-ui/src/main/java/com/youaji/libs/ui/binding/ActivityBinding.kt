@file:Suppress("unused")
package com.youaji.libs.ui.binding

import android.app.Activity
import androidx.viewbinding.ViewBinding

/**
 * @author youaji
 * @since 2022/9/13
 */
interface ActivityBinding<VB : ViewBinding> {
    val binding: VB
    fun Activity.setContentViewWithBinding()
}

class ActivityBindingDelegate<VB : ViewBinding> : ActivityBinding<VB> {
    private lateinit var _binding: VB

    override val binding: VB get() = _binding

    override fun Activity.setContentViewWithBinding() {
        _binding = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        setContentView(binding.root)
    }
}