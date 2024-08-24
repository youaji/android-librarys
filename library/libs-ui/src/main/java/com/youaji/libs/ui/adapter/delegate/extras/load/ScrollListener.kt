@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate.extras.load

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * ScrollListener
 *
 * @author zwenkai@foxmail.com, Created on 2019-03-09 00:08:35
 *         Major Function：<b>LoadScrollListener</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
internal abstract class ScrollListener : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        if (newState == RecyclerView.SCROLL_STATE_IDLE && isLastItemVisible(recyclerView)) {
            loadMore()
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE && lessThanOneScreen(recyclerView)) {
            loadMore()
        }
    }

    /**
     * Whether the last item is visible.
     *
     * @param recyclerView
     * @return
     */
    private fun isLastItemVisible(recyclerView: RecyclerView): Boolean {
        val lastVisiblePosition = getLastVisiblePosition(recyclerView)
        return lastVisiblePosition == recyclerView.adapter!!.itemCount - 1
    }

    /**
     * Returns the last visible position.
     *
     * @param recyclerView
     * @return
     */
    private fun getLastVisiblePosition(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager
        return if (layoutManager is LinearLayoutManager) {
            layoutManager.findLastVisibleItemPosition()
        } else {
            val lastVisibleChild = recyclerView.getChildAt(recyclerView.childCount - 1)
            if (lastVisibleChild != null) recyclerView.getChildAdapterPosition(lastVisibleChild) else RecyclerView.NO_POSITION
        }
    }

    /**
     * Returns whether the data is full screen.
     *
     * @param recyclerView
     * @return
     */
    private fun lessThanOneScreen(recyclerView: RecyclerView): Boolean {
        val lastVisiblePosition = getLastVisiblePosition(recyclerView)
        return if (isLastItemVisible(recyclerView)
                && lastVisiblePosition >= (if (hasStateView()) 1 else 0)) { // Don't load more without any data.
            // The bottom of the last item is less than the bottom of the RecyclerView, i.e., less than one screen.
            recyclerView.getChildAt(recyclerView.childCount - 1).bottom < recyclerView.bottom
        } else {
            false
        }
    }

    abstract fun loadMore()

    abstract fun hasStateView(): Boolean
}