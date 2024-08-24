package com.youaji.libs.picture.viewer.util

import android.graphics.Matrix
import kotlin.math.pow
import kotlin.math.sqrt

object MatrixUtils {
    private val mMatrixValues = FloatArray(9)
    fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    fun getScale(matrix: Matrix): Float {
        return sqrt((getValue(matrix, Matrix.MSCALE_X).pow(2f) + getValue(matrix, Matrix.MSKEW_Y).pow(2f)).toDouble()).toFloat()
    }
}
