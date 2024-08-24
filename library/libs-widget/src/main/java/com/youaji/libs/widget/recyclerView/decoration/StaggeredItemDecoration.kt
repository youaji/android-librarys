@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * @author youaji
 * @since 2023/1/13
 */
class StaggeredItemDecoration(
    private val spanCount: Int = 2,
    private val space: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

        val spanIndex = layoutParams.spanIndex

        outRect.top = space

        // 如果是跨列(整行)，则 spanIndex 为 0
        if (layoutParams.isFullSpan){
            outRect.left = space
            outRect.right = space
            return
        }

        // 最后一列
        if (spanIndex == spanCount - 1) {
            outRect.right = space
        } else {
            outRect.left = space
            outRect.right = space
        }

    }
}