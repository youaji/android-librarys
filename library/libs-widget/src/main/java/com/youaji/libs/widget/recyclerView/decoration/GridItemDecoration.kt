@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.decoration

import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 1、列数自动获取
 * 2、如设置了 spanSize，会自动适配。
 *    注意：只支持整行的，如：列数为 2，但是跨列 3，还剩余 1 列，此时就会出现错乱
 *
 * @author youaji
 * @since 2023/1/13
 */
class GridItemDecoration(
    @Deprecated("spanCount 已修改为自动获取")
    private val spanCount: Int,
    private val space: Int,// 间隔值，单位dx
    private val includeEdge: Boolean = false,//是否包含边缘（左右）
) : RecyclerView.ItemDecoration() {


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val layoutManager = parent.layoutManager

        if (layoutManager !is GridLayoutManager)
            throw IllegalArgumentException("GridItemDecoration not support $layoutManager")

        // 获取当前View的位置
        val position = parent.getChildAdapterPosition(view)
        // 多少列
        val spanCount = layoutManager.spanCount
        // 跨多少列
        val spanSize = layoutManager.spanSizeLookup.getSpanSize(position)
        // 列信息
        val column = getColumn(position, spanCount, spanSize)
        // 当前列下标
        val columnIndex = column.first
        // 当前行列数
        val columnCount = column.second

        if (includeEdge) {
            // 间隔 - 当前列 * 间隔 / 列数
            outRect.left = space - columnIndex * space / columnCount
            // （当前列 + 1）* 间隔 / 列数
            outRect.right = (columnIndex + 1) * space / columnCount
        } else {
            outRect.left = columnIndex * space / columnCount
            outRect.right = space - (columnIndex + 1) * space / columnCount
        }

        // 第一行
        if (position < columnCount) {
            outRect.top = space
        }

        outRect.bottom = space
    }

    private val rowCache = SparseArray<Pair<Int, Int>>()

    private fun putCache(position: Int, value: Pair<Int, Int>) {
        rowCache.put(position, value)
    }

    private fun getColumn(position: Int, spanCount: Int, spanSize: Int): Pair<Int, Int> {
        // 获取缓存
        rowCache.get(position, null)?.let { return it }

        // 第一个
        // 初始化
        if (position == 0) {
            val firstValue = Pair(0, if (spanSize == 1) spanCount else 1)
            putCache(position, firstValue)
            return firstValue
        }

        // 上一个的数据
        val previousValue = rowCache.get(
            position - 1,
            Pair(position - 1, if (spanSize == 1) spanCount else 1),// 没找到时的默认值
        )

        // 根据上一个的数据计算当前的数据
        val (index, count) =
            if (previousValue.first + 1 == previousValue.second) {
                Pair(0, if (spanSize == 1) spanCount else 1)
            } else {
                Pair(previousValue.first + 1, previousValue.second)
            }

        val newValue = Pair(index, count)
        putCache(position, newValue)
        return newValue
    }


}