package com.youaji.libs.opencv4

import org.opencv.core.Mat

class NativeOpenCV4 {
    companion object {
        init {
            System.loadLibrary("_opencv4")
        }
    }

    @Deprecated(message = "废弃，仅优化图像效果，没必要增加额外开销，且效果不明显")
    fun multiScaleDetailBoosting(src: Mat, dst: Mat, radius: Int = 3) {
        nativeMultiScaleDetailBoosting(src.nativeObjAddr, dst.nativeObjAddr, radius)
    }

    /**
     * 算法工程师张光华提供的针对融合后的图像优化
     * @param srcAddr
     * @param dstAddr
     * @param radius
     */
    private external fun nativeMultiScaleDetailBoosting(srcAddr: Long, dstAddr: Long, radius: Int)
}