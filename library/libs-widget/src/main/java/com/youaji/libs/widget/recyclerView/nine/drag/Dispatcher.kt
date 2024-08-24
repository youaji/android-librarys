@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.nine.drag

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.MainThread
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.youaji.libs.widget.recyclerView.nine.drag.helper.ItemTouchHelperAdapter
import com.youaji.libs.widget.recyclerView.nine.drag.helper.SimpleItemTouchHelperCallback

class Dispatcher<T> : ItemTouchHelperAdapter {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mItemTouchHelper: ItemTouchHelper
    private lateinit var mParentView: ParentView
    private lateinit var mContext: Context
    private lateinit var mTouchCallBack: SimpleItemTouchHelperCallback<T>
    private lateinit var mPositionProvider: PositionProvider
    private var inited: Boolean = false
    private var mRoot: ViewGroup? = null

    private var mSelectedPhotos: MutableList<T> = mutableListOf()

    //    /**
//     * 支持两种创建方式 1，外部传入ParentView 2 外部传入rootView 内部创建parentView添加到rootView里
//     */
    @MainThread
    fun onCreate(
        rootContainer: ViewGroup,
        context: Context,
        recyclerView: RecyclerView,
        list: List<T>,
        provider: PositionProvider,
    ) {
        if (inited) return
        inited = true
        mRoot = rootContainer
        mContext = context
        mTouchCallBack = SimpleItemTouchHelperCallback(this)
        mRecyclerView = recyclerView
        mItemTouchHelper = ItemTouchHelper(mTouchCallBack)
        mItemTouchHelper.attachToRecyclerView(recyclerView)
        mSelectedPhotos = list as MutableList<T>
        mPositionProvider = provider
    }

//    @MainThread
//    fun onCreate(
//        parentView: ParentView, context: Context, photoRecyclerView: RecyclerView,
//        list: List<T>, posProvider: PositionProvider,
//    ) {
//        checkIsInit()
//        inited = true
//        mParentView = parentView
//        mContext = context
//        mTouchCallBack = SimpleItemTouchHelperCallback(this)
//        photoView = photoRecyclerView
//        mItemTouchHelper = ItemTouchHelper(mTouchCallBack)
//        mItemTouchHelper.attachToRecyclerView(photoRecyclerView)
//        mSelectedPhotos = list as MutableList<T>
//        mPosProvider = posProvider
//    }

    fun onStartDrag(viewHolder: DragViewHolder) {
        // 开始拖拽时 找到 Activity 的 contentView
        mRoot?.let {
            mParentView = ParentView(mContext)
            val layoutParams =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            mParentView.layoutParams = layoutParams
            mRoot!!.addView(mParentView)
        }

        mTouchCallBack.setOnDrawListener(mParentView)
        mParentView.onStartDrag(viewHolder)
        mItemTouchHelper.startDrag(viewHolder)
    }

    fun innerItemMove(fromPosition: Int, toPosition: Int) {
        val photo = mSelectedPhotos.removeAt(fromPosition)
        photo?.let { mSelectedPhotos.add(toPosition, it) }
        mRecyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearView() {
        mParentView.clear()
        mRoot?.removeView(mParentView)
        mTouchCallBack.removeOnDrawListener()
        mRecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition == toPosition || mPositionProvider.canItemMove(fromPosition, toPosition)) {
            return false
        }
        innerItemMove(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
    }


    override fun start(): Int {
        return mPositionProvider.start()
    }

    override fun end(): Int {
        return mPositionProvider.end()
    }

    fun onDestroy() {

    }
}