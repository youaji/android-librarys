@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.nine.drag.helper

import android.graphics.Canvas
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.youaji.libs.widget.recyclerView.nine.drag.Dispatcher
import com.youaji.libs.widget.recyclerView.nine.drag.util.ScreenUtils
import kotlin.math.abs

class SimpleItemTouchHelperCallback<T>(
    private val mDispatcher: Dispatcher<T>,
) : ItemTouchHelper.Callback() {

    private var mDrawListener: OnDrawListener? = null

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
        if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags = (ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            val swipeFlags = 0
            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            makeMovementFlags(dragFlags, swipeFlags)
        }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val start = mDispatcher.start()
        val end = mDispatcher.end()
        if (source.itemViewType != target.itemViewType) {
            return false
        }

        if (source.bindingAdapterPosition in start..end
            && target.bindingAdapterPosition >= start
            && target.bindingAdapterPosition <= end
        ) {
            mDispatcher.onItemMove(source.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }
        return false
    }


    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val alpha = alphaFull - abs(dX) / viewHolder.itemView.width.toFloat()
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX
        } else {
            mDrawListener?.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun getBoundingBoxMargin(): Int =
        ScreenUtils.getScreenHeight()

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) =
        mDispatcher.onItemDismiss(viewHolder.bindingAdapterPosition)

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = alphaFull
        mDispatcher.clearView()
    }

    fun setOnDrawListener(onDrawListener: OnDrawListener) {
        mDrawListener = onDrawListener
    }

    fun removeOnDrawListener() {
        mDrawListener = null
    }

    interface OnDrawListener {
        fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
        )
    }

    companion object {
        private const val alphaFull = 1.0f
    }
}
