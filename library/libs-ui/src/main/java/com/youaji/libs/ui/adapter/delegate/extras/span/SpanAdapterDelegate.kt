@file:Suppress("unused")
package com.youaji.libs.ui.adapter.delegate.extras.span

import androidx.recyclerview.widget.RecyclerView

import com.youaji.libs.ui.adapter.delegate.extras.ClickableAdapterDelegate

/**
 * SpanAdapterDelegate
 *
 * @author zwenkai@foxmail.com, Created on 2018-06-10 10:00:34
 *         Major Function：<b>AdapterDelegate with span<b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */

abstract class SpanAdapterDelegate<T, VH : RecyclerView.ViewHolder> : ClickableAdapterDelegate<T, VH> {

    open var spanSize: Int = DEFAULT_SPAN_SIZE

    constructor()

    constructor(tag: String) : super(tag)

    companion object {
        const val DEFAULT_SPAN_SIZE = 1
    }

}
