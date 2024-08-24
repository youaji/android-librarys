@file:Suppress("unused")
package com.youaji.libs.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 *
 * RecycleView Adapter
 * 针对单类型的 ViewType 的简单封装
 *
 * @author youaji
 * @since 2022/9/14
 * @param  E: 数据实体
 * @param VB: ViewBinding
 */
abstract class RecycleViewAdapter<E : Any, VB : ViewBinding> : RecyclerView.Adapter<RecycleViewAdapter.ViewHolder<VB>>() {

    open var dataList: MutableList<E> = mutableListOf()
        set(value) {
            val fieldSize = field.size
            if (fieldSize > 0) {
                field.clear()
                notifyItemRangeRemoved(0, fieldSize)
            }
            field.addAll(value)
            notifyItemRangeChanged(0, value.size)
        }

    open var itemClickListener: OnItemClickListener<E>? = null
    open var itemLongClickListener: OnItemLongClickListener<E>? = null

    fun addData(data: MutableList<E>, notify: Boolean = true) {
        val positionStart = dataList.size
        dataList.addAll(data)
        if (notify) notifyItemRangeInserted(positionStart + 1, dataList.size)
    }

    fun removeData(position: Int, notify: Boolean = true) {
        dataList.removeAt(position)
        if (notify) notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeData(){
        dataList = mutableListOf()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeDataRange(data: MutableList<E>, notify: Boolean = true) {
        dataList.removeAll(data)
        if (notify) notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder<VB>, position: Int) {
        onBindViewHolder(holder, holder.bindingAdapterPosition, holder.binding, dataList[holder.bindingAdapterPosition])
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<VB>

    abstract fun onBindViewHolder(holder: ViewHolder<VB>, position: Int, binding: VB, bean: E)

    open class ViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    fun interface OnItemClickListener<T> {
        fun onItemClick(position: Int, bean: T)
    }

    fun interface OnItemLongClickListener<T> {
        fun onItemLongClick(position: Int, bean: T): Boolean
    }
}




