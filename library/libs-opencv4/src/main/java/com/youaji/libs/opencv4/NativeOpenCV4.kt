package com.youaji.libs.opencv4

class NativeOpenCV4 {
    companion object {
        init {
            System.loadLibrary("_opencv4")
        }
    }

    /**
     * @param srcAddr
     * @param dstAddr
     * @param radius
     */
    private external fun multiScaleDetailBoosting(srcAddr: Long, dstAddr: Long, radius: Int)
}