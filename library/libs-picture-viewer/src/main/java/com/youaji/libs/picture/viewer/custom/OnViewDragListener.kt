package com.youaji.libs.picture.viewer.custom

import androidx.annotation.RestrictTo

/**
 * Interface definition for a callback to be invoked when the photo is experiencing a drag event
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface OnViewDragListener {
    /**
     * Callback for when the photo is experiencing a drag event. This cannot be invoked when the
     * user is scaling.
     *
     * @param dx The change of the coordinates in the x-direction
     * @param dy The change of the coordinates in the y-direction
     * @return 返回值表示是否消费此次事件
     */
    fun onDrag(dx: Float, dy: Float): Boolean
}
