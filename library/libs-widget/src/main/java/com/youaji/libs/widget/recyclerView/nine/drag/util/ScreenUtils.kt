@file:Suppress("unused")
package com.youaji.libs.widget.recyclerView.nine.drag.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

object ScreenUtils {

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun getDrawingCacheWithColorBackground(contentView: View, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(
            contentView.width,
            contentView.height, Bitmap.Config.ARGB_8888
        )
        bitmap.eraseColor(color)
        val canvas = Canvas(bitmap)
        contentView.draw(canvas)
        return bitmap
    }
}