@file:Suppress("unused")
package com.youaji.libs.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 *
 * RecycleView Differ Adapter
 * 针对单类型的 ViewType 的简单封装
 * 数据对比后加载
 *
 * @author youaji
 * @since 2024/4/6
 * @param  E: 数据实体
 * @param VB: ViewBinding
 */
abstract class RecycleViewDifferAdapter<E : Any, VB : ViewBinding> : RecyclerView.Adapter<RecycleViewDifferAdapter.ViewHolder<VB>>() {

    private val listDiffer: AsyncListDiffer<E> = AsyncListDiffer(this, object : DiffUtil.ItemCallback<E>() {
        override fun areItemsTheSame(oldItem: E, newItem: E): Boolean =
            onAreItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: E, newItem: E): Boolean =
            onAreContentsTheSame(oldItem, newItem)
    })

    private val dataList: List<E>
        get() = listDiffer.currentList

    open var itemClickListener: ((position: Int, bean: E) -> Unit)? = null
    open var itemLongClickListener: ((position: Int, bean: E) -> Boolean)? = null
    open class ViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    fun updateData(data: List<E>?) {
        listDiffer.submitList(data ?: listOf())
    }

    fun addData(data: E) {
        val tempList = mutableListOf<E>()
        tempList.addAll(dataList)
        tempList.add(data)
        updateData(tempList)
    }

    fun addData(data: MutableList<E>?) {
        data?.let {
            val tempList = mutableListOf<E>()
            tempList.addAll(dataList)
            tempList.addAll(it)
            updateData(tempList)
        }
    }

    fun removeItem(position: Int) {
        if (listDiffer.currentList.isEmpty()
            || position < 0
            || position >= listDiffer.currentList.size
        ) return

        val tempList = mutableListOf<E>()
        tempList.addAll(dataList)
        tempList.removeAt(position)
        listDiffer.submitList(tempList)
    }

    fun clearData() {
        updateData(listOf())
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder<VB>, position: Int) {
        onBindViewHolder(holder, holder.bindingAdapterPosition, holder.binding, dataList[holder.bindingAdapterPosition])
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<VB>
    abstract fun onBindViewHolder(holder: ViewHolder<VB>, position: Int, binding: VB, bean: E)
    abstract fun onAreItemsTheSame(oldItem: E, newItem: E): Boolean
    abstract fun onAreContentsTheSame(oldItem: E, newItem: E): Boolean
}




