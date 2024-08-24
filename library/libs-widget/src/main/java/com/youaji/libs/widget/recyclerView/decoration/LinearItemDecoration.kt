@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @param space             间距
 * @param spaceVertical     垂直间距
 * @param spaceHorizontal   水平间距
 * @param firstSpace        第一个间距
 * @author youaji
 * @since 2023/1/18
 */
class LinearItemDecoration(
    private val space: Int = 0,
    private val spaceVertical: Int = 0,
    private val spaceHorizontal: Int = 0,
    private val firstSpace: Int = 0,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val firstIndex = parent.getChildAdapterPosition(view) == 0
        val lastIndex = parent.getChildAdapterPosition(view) == state.itemCount - 1
        val layoutManager = parent.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == RecyclerView.HORIZONTAL) {
                if (spaceVertical > 0) {
                    outRect.top = spaceVertical
                    outRect.bottom = spaceVertical
                }
                if (firstIndex) {
                    if (space > 0) {
                        outRect.left = space
                        outRect.top = space
                        outRect.right = space
                        outRect.bottom = space
                    }
                    if (spaceHorizontal > 0) {
                        outRect.left = spaceHorizontal
                        outRect.right = spaceHorizontal
                    }
                    if (firstSpace > 0) {
                        outRect.left = firstSpace
                    }
                } else {
                    if (space > 0) {
                        outRect.top = space
                        outRect.right = space
                        outRect.bottom = space
                    }
                    if (spaceHorizontal > 0) {
                        outRect.right = spaceHorizontal
                    }
                }
            } else if (orientation == RecyclerView.VERTICAL) {
                if (spaceHorizontal > 0) {
                    outRect.left = spaceHorizontal
                    outRect.right = spaceHorizontal
                }
                if (firstIndex) {
                    if (space > 0) {
                        outRect.left = space
                        outRect.top = space
                        outRect.right = space
                        outRect.bottom = space
                    }
                    if (spaceVertical > 0) {
                        outRect.top = spaceVertical
                        outRect.bottom = spaceVertical
                    }
                    if (firstSpace > 0) {
                        outRect.top = firstSpace
                    }
                } else {
                    if (space > 0) {
                        outRect.left = firstSpace
                        outRect.right = space
                        outRect.bottom = space
                    }
                    if (spaceVertical > 0) {
                        outRect.bottom = spaceVertical
                    }
                }
            }
        }
    }
}