@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate.extras.binding

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

import com.youaji.libs.ui.adapter.delegate.extras.span.SpanAdapterDelegate

/**
 * BindingSpanAdapterDelegate
 *
 * @author zwenkai@foxmail.com, Created on 2018-04-11 10:29:52
 *         Major Function：<b>Binding Delegation Adapter</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
abstract class BindingSpanAdapterDelegate<T> : SpanAdapterDelegate<T, BindingViewHolder> {

    /**
     * get layout resource
     *
     * @return
     */
    @get:LayoutRes
    abstract val layoutRes: Int

    constructor()

    constructor(tag: String) : super(tag)

    override fun onCreateViewHolder(parent: ViewGroup): BindingViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(parent.context),
                layoutRes,
                parent,
                false)
        val holder = BindingViewHolder(binding.root)
        holder.setBinding(binding)
        return holder
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int, item: T) {
        super.onBindViewHolder(holder, position, item)
        setVariable(holder.getBinding(), item, position)
        holder.getBinding<ViewDataBinding>().executePendingBindings()
    }

    /**
     * Set variable data
     *
     *
     * example：
     * binding.setVariable(BR.viewModel, mViewModel);
     *
     * @param binding
     * @param item
     * @param position
     */
    abstract fun setVariable(binding: ViewDataBinding, item: T, position: Int)
}
