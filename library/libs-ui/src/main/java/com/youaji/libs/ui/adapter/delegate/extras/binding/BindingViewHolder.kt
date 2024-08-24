@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate.extras.binding

import android.view.View

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * BindingViewHolder
 *
 * @author zwenkai@foxmail.com, Created on 2018-04-03 17:51:49
 *         Major Function：<b>Binding ViewHolder</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class BindingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var binding: ViewDataBinding? = null

    fun <T : ViewDataBinding> getBinding(): T {
        @Suppress("UNCHECKED_CAST")
        return binding as T
    }

    fun setBinding(binding: ViewDataBinding) {
        this.binding = binding
    }
}
