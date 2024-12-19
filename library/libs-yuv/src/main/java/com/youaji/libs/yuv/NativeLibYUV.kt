package com.youaji.libs.yuv

import android.media.Image
import java.nio.ByteBuffer

class NativeLibYUV {
    companion object {
        init {
            System.loadLibrary("_yuv")
        }
    }

    fun image2NV21(image: Image): ByteArray {
        val nv21Buffer = ByteArray(image.width * image.height * 3 / 2)
        yuv4208882nv21(image.width, image.height, image.planes[0].buffer, image.planes[2].buffer, nv21Buffer)
        return nv21Buffer
    }

    /**
     * uyvy to argb
     * @param uyvyBuffer
     * @param argb32Buffer
     * @param width
     * @param height
     */
    external fun uyvy2argb(uyvyBuffer: ByteArray, argb32Buffer: ByteArray, width: Int, height: Int): Boolean

    /**
     * yuv2 to argb
     * @param yuy2Buffer
     * @param argb32Buffer
     * @param width
     * @param height
     */
    external fun yuv22argb(yuy2Buffer: ByteArray, argb32Buffer: ByteArray, width: Int, height: Int): Boolean

    /**
     * argb to nv21
     * @param argb32Buffer
     * @param nv21Buffer
     * @param width
     * @param height
     */
    external fun argb2nv21(argb32Buffer: ByteArray, nv21Buffer: ByteArray, width: Int, height: Int): Boolean

    /**
     * argb to i420
     * @param argb32Buffer
     * @param i420Buffer
     * @param width
     * @param height
     */
    external fun argb2i420(argb32Buffer: ByteArray, i420Buffer: ByteArray, width: Int, height: Int): Boolean

    /**
     * yuv420_888 to nv21
     * @param width
     * @param height
     * @param yPlane
     * @param vPlane
     * @param nv21Buffer
     */
    external fun yuv4208882nv21(width: Int, height: Int, yPlane: ByteBuffer, vPlane: ByteBuffer, nv21Buffer: ByteArray)
}