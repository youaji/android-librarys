@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.nine.drag.helper

interface ItemTouchHelperAdapter {

    fun start(): Int

    fun end(): Int

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemDismiss(position: Int)
}