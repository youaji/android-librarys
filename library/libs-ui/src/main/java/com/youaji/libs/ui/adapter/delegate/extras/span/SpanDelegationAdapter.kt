@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate.extras.span

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import com.youaji.libs.ui.adapter.delegate.DelegationAdapter

/**
 * SpanDelegationAdapter
 *
 * @author zwenkai@foxmail.com, Created on 2018-06-10 11:08:37
 *         Major Function：<b>DelegationAdapter with span</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */

open class SpanDelegationAdapter @JvmOverloads constructor(hasConsistItemType: Boolean = false) : DelegationAdapter(hasConsistItemType) {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val delegate = delegatesManager.getDelegate(getItemViewType(position))
                    return if (null != delegate && delegate is SpanAdapterDelegate) {
                        delegate.spanSize
                    } else {
                        layoutManager.spanCount
                    }
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)

        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            val delegate = delegatesManager.getDelegate(holder.itemViewType)
            if (null != delegate && delegate is SpanAdapterDelegate) {
                layoutParams.isFullSpan = delegate.spanSize != SpanAdapterDelegate.DEFAULT_SPAN_SIZE
            } else {
                layoutParams.isFullSpan = true
            }
        }
    }
}
