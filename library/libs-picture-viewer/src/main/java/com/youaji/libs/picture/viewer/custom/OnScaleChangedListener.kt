package com.youaji.libs.picture.viewer.custom

import androidx.annotation.RestrictTo

/**
 * Interface definition for callback to be invoked when attached ImageView scale changes
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface OnScaleChangedListener {
    /**
     * Callback for when the scale changes
     *
     * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
     * @param focusX      focal point X position
     * @param focusY      focal point Y position
     */
    fun onScaleChange(scaleFactor: Float, focusX: Float, focusY: Float)
}
