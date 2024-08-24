@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate.extras

import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.RecyclerView

import com.youaji.libs.ui.R
import com.youaji.libs.ui.adapter.delegate.AdapterDelegate

/**
 * ClickableAdapterDelegate
 *
 * @author zwenkai@foxmail.com, Created on 2018-04-06 23:28:37
 *         Major Function：<b>Clickable ViewHolder Delegate</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
abstract class ClickableAdapterDelegate<T, VH : RecyclerView.ViewHolder> : AdapterDelegate<T, VH>, View.OnClickListener, View.OnLongClickListener {

    constructor()

    constructor(tag: String) : super(tag)

    override fun onBindViewHolder(holder: VH, position: Int, item: T) {
        if (clickable(position) || longClickable(position)) {

            holder.itemView.setTag(R.id.libs_ui_tag_clickable_adapter_delegate_holder, holder)
            holder.itemView.setTag(R.id.libs_ui_tag_clickable_adapter_delegate_data, item)

            if (clickable(position)) {
                holder.itemView.setOnClickListener(this)
            }

            if (clickable(position)) {
                holder.itemView.setOnLongClickListener(this)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onClick(view: View) {
        val holder = view.getTag(R.id.libs_ui_tag_clickable_adapter_delegate_holder) as VH
        val item = view.getTag(R.id.libs_ui_tag_clickable_adapter_delegate_data) as T
        val position = getPosition(holder)
        if (position == RecyclerView.NO_POSITION) {
            // ignore
            return
        }

        if (clickable(position)) {
            val lastClickTime = (view.getTag(R.id.libs_ui_tag_clickable_adapter_delegate_click_time) ?: 0L) as Long
            if (lastClickTime < SystemClock.uptimeMillis() - quickClickInterval()) {
                onItemClick(view, item, position)
                view.setTag(R.id.libs_ui_tag_clickable_adapter_delegate_click_time, SystemClock.uptimeMillis())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onLongClick(view: View): Boolean {
        val holder = view.getTag(R.id.libs_ui_tag_clickable_adapter_delegate_holder) as VH
        val item = view.getTag(R.id.libs_ui_tag_clickable_adapter_delegate_data) as T
        val position = getPosition(holder)
        if (position == RecyclerView.NO_POSITION) {
            // ignore
            return false
        }

        return if (longClickable(position)) {
            onItemLongClick(view, item, position)
        } else false
    }

    /**
     * Whether the adapter item can click
     *
     * @param position
     * @return
     */
    open fun clickable(position: Int) = true

    /**
     * Minimum time interval between two clicks
     */
    open fun quickClickInterval() = 500L

    /**
     * Whether the adapter item can long click
     *
     * @param position
     * @return
     */
    open fun longClickable(position: Int) = true

    /**
     * Called when a item view has been clicked.
     *
     * @param view
     * @param item
     * @param position
     */
    open fun onItemClick(view: View, item: T, position: Int) {
        // do nothing
    }

    /**
     * Called when a item view has been clicked and held.
     *
     * @param view
     * @param item
     * @param position
     * @return
     */
    open fun onItemLongClick(view: View, item: T, position: Int) = false

    /**
     * Get the position of ViewHolder
     *
     * @param holder
     * @return
     */
    private fun getPosition(holder: VH) = holder.adapterPosition
}
