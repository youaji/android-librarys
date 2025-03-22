@file:Suppress("unused")
package com.youaji.libs.opencv4

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import org.opencv.android.OpenCVLoader

internal class OpenCVInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Log.d("libs-opencv4", "initialize Local OpenCV Start")
        val result = OpenCVLoader.initLocal()
        if (result)
            Log.d("libs-opencv4", "initialize Local OpenCV SUCCESS!\nby OpenCV ${OpenCVLoader.OPENCV_VERSION}")
        else
            Log.w("libs-opencv4", "initialize Local OpenCV Failed!\nby OpenCV ${OpenCVLoader.OPENCV_VERSION}")
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}