package com.youaji.libs.opencv.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * 调整大小至目标大小
 * @param dstWidth 目标宽度
 * @param dstHeight 目标高度
 */
fun Bitmap?.scale(dstWidth: Int, dstHeight: Int): Bitmap? {
    if (this == null || this.isRecycled || dstWidth * dstHeight <= 0) {
        return null
    }
    val originalMat = Mat()
    Utils.bitmapToMat(this, originalMat)
    val resizedMat = Mat()
    Imgproc.resize(originalMat, resizedMat, Size(dstWidth.toDouble(), dstHeight.toDouble()))
    val dstBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(resizedMat, dstBitmap)
    originalMat.release()
    resizedMat.release()
    recycle()
    return dstBitmap
}

/**
 * 旋转
 * @param rotateCode 0 -> 将图像顺时针旋转90度；1 -> 将图像旋转180度；2 -> 将图像逆时针旋转90度；
 */
fun Bitmap?.rotate(rotateCode: Int): Bitmap? {
    if (this == null || this.isRecycled) {
        return null
    }
    val originalMat = Mat()
    Utils.bitmapToMat(this, originalMat)
    val rotatedMat = Mat()
    Core.rotate(originalMat, rotatedMat, rotateCode)
    val dstBitmap = Bitmap.createBitmap(rotatedMat.cols(), rotatedMat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(rotatedMat, dstBitmap)
    originalMat.release()
    rotatedMat.release()
    recycle()
    return dstBitmap
}

/**
 * 翻转
 * @param flipCode  0 -> 围绕 x 轴翻转；正数（如1） -> 围绕 y 轴翻转；负数（如-1） -> 同时围绕 x和 y 轴翻转；
 */
fun Bitmap?.flip(flipCode: Int): Bitmap? {
    if (this == null || this.isRecycled) {
        return null
    }
    val originalMat = Mat()
    Utils.bitmapToMat(this, originalMat)
    val flippedMat = Mat()
    Core.flip(originalMat, flippedMat, flipCode)
    val dstBitmap = Bitmap.createBitmap(flippedMat.cols(), flippedMat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(flippedMat, dstBitmap)
    originalMat.release()
    flippedMat.release()
    recycle()
    return dstBitmap
}