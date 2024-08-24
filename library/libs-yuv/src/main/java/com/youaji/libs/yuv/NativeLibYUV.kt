package com.youaji.libs.yuv

class NativeLibYUV {
    companion object {
        init {
            System.loadLibrary("_yuv")
        }
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
}