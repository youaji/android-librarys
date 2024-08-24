@file:Suppress("unused")
package com.youaji.libs.widget.flow

import android.view.View
import android.view.ViewGroup

/**
 * @author youaji
 * @since 2023/2/5
 */
abstract class FlowAdapter<T> {
    private var onDataChangedListener: OnDataChangedListener? = null

    private var data: MutableList<T> = mutableListOf()

    /**
     * 子 View 创建
     *
     * @param parent
     * @param item
     * @param position
     * @return
     */
    abstract fun <T> getView(parent: ViewGroup, item: T, position: Int): View?

    /**
     * 初始化View
     *
     * @param view
     * @param item
     * @param position
     * @return
     */
    abstract fun <T> initView(view: View?, item: T, position: Int)


    /**
     * 折叠View 默认不设置
     *
     * @return
     */
    fun foldView(): View? = null


    /**
     * 数据的数量
     *
     * @return
     */
    fun getCount(): Int = if (data.isEmpty()) 0 else data.size

    /**
     * 获取数据
     *
     * @return
     */
    fun getData(): List<T> = data


    /**
     * 设置新数据
     *
     * @param data
     */
    fun setNewData(data: List<T>) {
        if (data.isEmpty()) {
            this.data.clear()
        } else {
            this.data = data as MutableList<T>
        }
        notifyDataChanged()
    }

    /**
     * 添加数据
     *
     * @param data
     */
    fun addData(data: List<T>) {
        this.data.addAll(data)
        notifyDataChanged()
    }

    /**
     * 添加数据
     *
     * @param index
     * @param data
     */
    fun addData(index: Int, data: List<T>) {
        this.data.addAll(index, data)
        notifyDataChanged()
    }

    /**
     * 添加数据
     *
     * @param data
     */
    fun addData(data: T) {
        this.data.add(data)
        notifyDataChanged()
    }

    /**
     * @param position
     * @return 获取指定位置的数据
     */
    fun getItem(position: Int): T = data[position]

    /**
     * 刷新数据
     */
    fun notifyDataChanged() {
        if (onDataChangedListener != null) {
            onDataChangedListener!!.onChanged()
        }
    }

    fun setOnDataChangedListener(listener: OnDataChangedListener?) {
        onDataChangedListener = listener
    }

    interface OnDataChangedListener {
        fun onChanged()
    }
}