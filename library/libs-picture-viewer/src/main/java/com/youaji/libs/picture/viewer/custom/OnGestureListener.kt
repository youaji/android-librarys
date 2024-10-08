package com.youaji.libs.picture.viewer.custom

internal interface OnGestureListener {
    fun onDrag(dx: Float, dy: Float)
    fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float)
    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
}