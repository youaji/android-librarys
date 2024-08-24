@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * AbsDelegationAdapter
 *
 * @author zwenkai@foxmail.com, Created on 2018-04-10 23:08:38
 *         Major Function：<b>The base DelegationAdapter</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
abstract class AbsDelegationAdapter(protected var delegatesManager: AdapterDelegatesManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Add an Adapter Delegate with tag, the role of tag is to distinguish Adapters with the
     * same data type.
     *
     * @param delegate
     * @param tag
     */
    @JvmOverloads
    fun addDelegate(delegate: AdapterDelegate<*, *>, tag: String = delegate.tag): AbsDelegationAdapter {
        delegate.tag = tag
        delegatesManager.addDelegate(delegate, tag)
        return this
    }

    /**
     * Get the viewType of the adapter delegate.
     *
     * @param delegate
     */
    fun getDelegateViewType(delegate: AdapterDelegate<*, *>): Int {
        return delegatesManager.getDelegateViewType(delegate)
    }

    fun setFallbackDelegate(delegate: AdapterDelegate<*, *>): AbsDelegationAdapter {
        @Suppress("UNCHECKED_CAST")
        delegatesManager.fallbackDelegate = delegate as AdapterDelegate<Any, RecyclerView.ViewHolder>?
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegatesManager.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegatesManager.onBindViewHolder(holder, position, getItem(position))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
        delegatesManager.onBindViewHolder(holder, position, payloads, getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return delegatesManager.getItemViewType(getItem(position), position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        delegatesManager.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return delegatesManager.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        delegatesManager.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        delegatesManager.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        delegatesManager.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        delegatesManager.onDetachedFromRecyclerView(recyclerView)
    }

    abstract fun getItem(position: Int): Any
}
