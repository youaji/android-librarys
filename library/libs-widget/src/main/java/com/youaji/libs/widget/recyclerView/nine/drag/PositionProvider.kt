@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.nine.drag

interface PositionProvider {
    fun start(): Int

    fun end(): Int

    fun canItemMove(fromPosition: Int, toPosition: Int): Boolean
}