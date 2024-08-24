@file:Suppress("unused")
package com.youaji.libs.widget.flow

import android.content.Context
import android.util.AttributeSet

/**
 * @author youaji
 * @since 2023/2/5
 */
open class FlowListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FlowLayout(context, attrs, defStyleAttr),
    FlowAdapter.OnDataChangedListener {

    protected var flowAdapter: FlowAdapter<*>? = null

    fun setAdapter(tagAdapter: FlowAdapter<*>?) {
        flowAdapter = tagAdapter
        flowAdapter!!.setOnDataChangedListener(this)
        updateView()
    }

    override fun onChanged() {
        updateView()
    }

    private fun updateView() {
        removeAllViews()
        flowAdapter?.let {
            val count = it.getCount()
            for (i in 0 until count) {
                val tagView = it.getView(this, it.getItem(i), i)
                tagView?.let { tag ->
                    tag.tag = it.getItem(i)
                    it.initView(tagView, it.getItem(i), i)
                    addView(tag)
                }
            }
        }
    }

}