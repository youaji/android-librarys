package com.youaji.libs.picture.viewer.interfaces

import com.youaji.libs.picture.viewer.PictureViewer

/**
 * call when [PictureViewer] View close
 */
fun interface OnDismissListener {
    fun onDismiss()
}
