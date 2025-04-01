package com.youaji.example.librarys.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import com.youaji.example.librarys.databinding.FragmentLibOpencvBinding
import com.youaji.libs.ui.basic.BasicBindingFragment
import com.youaji.libs.util.design.alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size

class LibOpenCVFragment : BasicBindingFragment<FragmentLibOpencvBinding>() {

    private var bitmap1: Bitmap? = null
    private var bitmap2: Bitmap? = null
    private val launcherGallery1 =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                bitmap1 = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))
                binding.image1.setImageBitmap(bitmap1)
            }
        }
    private val launcherGallery2 =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                bitmap2 = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))
                binding.image2.setImageBitmap(bitmap2)
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWeightedParams()
        binding.image1.setOnClickListener { launcherGallery1.launch("image/*") }
        binding.image2.setOnClickListener { launcherGallery2.launch("image/*") }
        binding.btnAddWeighted.setOnClickListener { addWeighted() }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun initWeightedParams() {
        binding.seekAlpha.max = 100
        binding.seekAlpha.progress = 50
        binding.seekBeta.max = 100
        binding.seekBeta.progress = 50
        binding.seekGamma.max = 100
        binding.seekGamma.progress = 0
        binding.seekPipRatio.max = 7
        binding.seekGamma.progress = 0
        binding.textAlpha.text = "alpha[${binding.seekAlpha.progress / 100.0}]"
        binding.textBeta.text = "beta[${binding.seekBeta.progress / 100.0}]"
        binding.textGamma.text = "gamma[${binding.seekGamma.progress / 100.0}]"
        binding.textPipRatio.text = "pip ratio[${(binding.seekPipRatio.progress + 3) / 10.0}]"
        binding.seekAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textAlpha.text = "alpha[${progress / 100.0}]"
//                addWeighted()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekBeta.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textBeta.text = "beta[${progress / 100.0}]"
//                addWeighted()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekGamma.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textGamma.text = "gamma[${progress / 100.0}]"
//                addWeighted()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekPipRatio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textPipRatio.text = "pip ratio[${(binding.seekPipRatio.progress + 3) / 10.0}]"
//                addWeighted()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.switchPip.isChecked = true
        binding.switchPip.setOnCheckedChangeListener { buttonView, _ ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            addWeighted()
        }
    }

    private var pipSize = Size(0.0, 0.0)

    private fun getPipSize(ratio: Double): Size {
//        val pipWidth = (bitmap2?.width ?: 0) * ratio
//        val pipHeight = (bitmap2?.height ?: 0) * ratio
        val pipWidth = binding.imageResult.measuredWidth * ratio
        val pipHeight = binding.imageResult.measuredHeight * ratio
        if (pipSize.width != pipWidth) {
            pipSize.width = pipWidth
        }
        if (pipSize.height != pipHeight) {
            pipSize.height = pipHeight
        }
        return pipSize
    }

    private fun pipAndWeighted(matARGB32: Mat, matYUV: Mat, matResult: Mat, pipSize: Size, alpha: Double, beta: Double, gamma: Double, pipEnable: Boolean = false) {
        if (!pipEnable) {
            Core.addWeighted(matARGB32, alpha, matYUV, beta, gamma, matResult)
            return
        }
        // 中心点
        val centerPoint = Point((matARGB32.width() / 2).toDouble(), (matARGB32.height() / 2).toDouble())
        // （画中画内容）区域大小
        val rectYUV = Rect(
            (centerPoint.x - (pipSize.width / 2)).toInt(),
            (centerPoint.y - (pipSize.height / 2)).toInt(),
            pipSize.width.toInt(),
            pipSize.height.toInt(),
        )
        // （画中画内容）区域图像
        val matRoiYUV = Mat(matYUV, rectYUV)

        // （画中画对应的背景）区域大小
        val rectARGB32 = Rect(
            (centerPoint.x - (matRoiYUV.width() / 2)).toInt(),
            (centerPoint.y - (matRoiYUV.height() / 2)).toInt(),
            matRoiYUV.width(),
            matRoiYUV.height(),
        )
        // （画中画对应的背景）区域图像
        val marRoiArgb32 = Mat(matARGB32, rectARGB32)

        // 画中画区域内容融合处理
        Core.addWeighted(marRoiArgb32, alpha, matRoiYUV, beta, gamma, matResult)

        // 画中画背景区域提取出来
        val matARGB32Submat = matARGB32.submat(rectARGB32)
        // 提取出来的位置替换内容
        matResult.copyTo(matARGB32Submat)
        // 再赋值给result
        matARGB32.copyTo(matResult)

        // 释放
        matRoiYUV.release()
        marRoiArgb32.release()
        matARGB32Submat.release()
    }

    private fun addWeighted() {
        showLoadingView()
        if (bitmap1 == null || bitmap2 == null) {
            showContentView()
            return
        }
        val bitmap1Width = bitmap1?.width ?: 0
        val bitmap1Height = bitmap1?.height ?: 0
        val bitmap1Ratios = bitmap1Width.toDouble() / bitmap1Height.toDouble()
        val bitmap2Width = bitmap2?.width ?: 0
        val bitmap2Height = bitmap2?.height ?: 0
        val bitmap2Ratios = bitmap2Width.toDouble() / bitmap2Height.toDouble()
        if (bitmap1Ratios != bitmap2Ratios) {
            alert("尺寸不一致")
            showContentView()
            return
        }
        val pipEnable = binding.switchPip.isChecked
        CoroutineScope(Dispatchers.IO).launch {
            val mat1 = Mat()
            val mat2 = Mat()
            Utils.bitmapToMat(bitmap1, mat1)
            Utils.bitmapToMat(bitmap2, mat2)

            val alpha = binding.seekAlpha.progress / 100.0
            val beta = binding.seekBeta.progress / 100.0
            val gamma = binding.seekGamma.progress / 100.0
            val pipRatio = (binding.seekPipRatio.progress + 3) / 10.0

            val matResult = Mat()

            pipAndWeighted(mat1, mat2, matResult, getPipSize(pipRatio), alpha, beta, gamma, pipEnable)

//            val mat = Mat()
//            pipAndWeighted(mat1, mat2, mat, getPipSize(pipRatio), alpha, beta, gamma, binding.switchPip.isChecked)
//            val openCV4 = NativeOpenCV4()
//            openCV4.multiScaleDetailBoosting(mat, matResult)

            val bitmapResult = Bitmap.createBitmap(matResult.width(), matResult.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(matResult, bitmapResult)
            activity?.runOnUiThread {
                binding.imageResult.setImageBitmap(bitmapResult)
                showContentView()
            }
        }
    }

}