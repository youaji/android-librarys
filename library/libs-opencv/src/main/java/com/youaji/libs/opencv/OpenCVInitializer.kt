@file:Suppress("unused")
package com.youaji.libs.opencv

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

internal class OpenCVInitializer : Initializer<Unit> {

    private val loaderCallbackInterface = object : LoaderCallbackInterface {
        override fun onManagerConnected(status: Int) {
            Log.d("libs-opencv", "onManagerConnected: opencv initializer $status")
        }

        override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface) {
            Log.d("libs-opencv", "onPackageInstall: opencv initializer $operation")
        }
    }

    override fun create(context: Context) {
        if (OpenCVLoader.initDebug()) {
            loaderCallbackInterface.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context.applicationContext, loaderCallbackInterface)
        }
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}