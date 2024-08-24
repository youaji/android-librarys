package com.youaji.example.librarys.extensions

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible

fun Spinner.setMediaUrlChoose(selected: (url: String) -> Unit) {
    ArrayAdapter(context, android.R.layout.simple_spinner_item, MediaUrls.map { "[${it.name}]\n${it.url}" }).also { a ->
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter = a
    }
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            (view as TextView).apply {
                freezesText = true
                marqueeRepeatLimit = -1
                setHorizontallyScrolling(true)
                isSelected = true
            }
            selected.invoke(MediaUrls[position].url)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}

fun Spinner.setSocketUrlChoose(selected: (ip: String, port: Int) -> Unit) {
    val historyInfo = SocketHistory.getHistory().toList()
    this.isVisible = historyInfo.isNotEmpty()
    if (isGone) return

    ArrayAdapter(context, android.R.layout.simple_spinner_item, historyInfo).also { a ->
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter = a
    }
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            (view as TextView).apply {
                freezesText = true
                marqueeRepeatLimit = -1
                setHorizontallyScrolling(true)
                isSelected = true
            }
            val info = historyInfo[position].split(":")
            if (info.size >= 2) {
                selected.invoke(info[0], info[1].toInt())
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}