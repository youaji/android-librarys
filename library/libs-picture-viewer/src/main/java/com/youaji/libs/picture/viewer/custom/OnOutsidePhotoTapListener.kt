package com.youaji.libs.picture.viewer.custom

import android.widget.ImageView
import androidx.annotation.RestrictTo

/**
 * Callback when the user tapped outside of the photo
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface OnOutsidePhotoTapListener {
    /**
     * The outside of the photo has been tapped
     */
    fun onOutsidePhotoTap(imageView: ImageView?)
}
