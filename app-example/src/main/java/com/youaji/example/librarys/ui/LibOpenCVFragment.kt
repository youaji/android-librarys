package com.youaji.example.librarys.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.example.librarys.databinding.FragmentLibOpencvBinding
import com.youaji.libs.ui.basic.BasicBindingFragment
//import com.youaji.module.render.thread.ARGB32Frame
//import com.youaji.module.render.thread.RenderARGB32Thread
//import com.youaji.module.render.thread.RenderARGB32WeightedThread


class LibOpenCVFragment : BasicBindingFragment<FragmentLibOpencvBinding>() {
//
//    private var isUseVLC = false
//
//    /*
//     * -------------------------------------------------------------------------------------------------
//     * VLC
//     * -------------------------------------------------------------------------------------------------
//     */
//    private val useTextureView = false
//    private val enableSubtitles = true
//    private lateinit var libVLCStart: LibVLC
//    private lateinit var libVLCEnd: LibVLC
//    private lateinit var mediaPlayerStart: MediaPlayer
//    private lateinit var mediaPlayerEnd: MediaPlayer
//    private fun getOptions(): ArrayList<String> {
//        return arrayListOf(
//            "--sub-source=marq{marquee=\"%H:%M:%S\",position=${Position.TopRight},color=0xFF0000，size=1}",
//            "--width=${(screenWidth - 30.dp.toInt()) / 2}",
//            "-vvv",
//        )
//    }
//
//    private fun initVLC() {
//        if (!isUseVLC) return
//        libVLCStart = LibVLC(requireContext(), getOptions())
//        libVLCEnd = LibVLC(requireContext(), getOptions())
//        mediaPlayerStart = MediaPlayer(libVLCStart)
//        mediaPlayerEnd = MediaPlayer(libVLCEnd)
//        mediaPlayerStart.setEventListener { }
//    }
//
//    private fun playStartVLC(url: String) {
//        try {
//            val media = Media(libVLCStart, Uri.parse(url))
//            mediaPlayerStart.media = media
//            media.release()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        mediaPlayerStart.play()
//    }
//
//    private fun playEndVLC(url: String) {
//        try {
//            val media = Media(libVLCStart, Uri.parse(url))
//            mediaPlayerEnd.media = media
//            media.release()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        mediaPlayerEnd.play()
//    }
//
//    private fun startVLC() {
//        if (!isUseVLC) return
//        mediaPlayerStart.attachViews(binding.layoutVideoStart, null, enableSubtitles, useTextureView)
//        mediaPlayerEnd.attachViews(binding.layoutVideoEnd, null, enableSubtitles, useTextureView)
//    }
//
//    private fun destroyVLC() {
//        if (!isUseVLC) return
//        mediaPlayerStart.release()
//        mediaPlayerEnd.release()
//        libVLCStart.release()
//        libVLCEnd.release()
//    }
//
//    private fun stopVLC() {
//        if (!isUseVLC) return
//        mediaPlayerStart.vlcVout.detachViews()
//        mediaPlayerEnd.vlcVout.detachViews()
//    }
//
//    /*
//    * -------------------------------------------------------------------------------------------------
//    * FFmpeg
//    * -------------------------------------------------------------------------------------------------
//    */
//    private var videoStartWidth = 0
//    private var videoEndWidth = 0
//    private var videoStartHeight = 0
//    private var videoEndHeight = 0
//    private val textureViewStartSize = Size(0.0, 0.0)
//    private val textureViewEndSize = Size(0.0, 0.0)
//    private val textureViewResultSize = Size(0.0, 0.0)
//    private lateinit var nativeLibFFmpegStart: NativeLibFFmpeg
//    private lateinit var nativeLibFFmpegEnd: NativeLibFFmpeg
//    private lateinit var renderThreadStart: RenderARGB32Thread
//    private lateinit var renderThreadEnd: RenderARGB32Thread
//    private lateinit var renderWeightedThread: RenderARGB32WeightedThread
//    private val ffmpegStartCallback = object : OnFFmpegCallback {
//        override fun onFFmpegPlayerCode(code: Int) {
//            runOnUI { toast("ffmpegStart:$code") }
//        }
//
//        override fun onFFmpegPlayerFrame(argb32Buffer: ByteArray?, width: Int, height: Int) {
//            argb32FrameStart = argb32Buffer?.let { ARGB32Frame(it.toList(), width, height) }
//            renderThreadStart.pushFrame(argb32Buffer, width, height)
//            renderWeightedThread.pushARGB32Frame(argb32Buffer, width, height)
//            runOnUI {
//                resetTextureLayoutStart(width, height)
//                resetResultTextureLayout(width, height)
//            }
//        }
//    }
//
//    private val ffmpegEndCallback = object : OnFFmpegCallback {
//        override fun onFFmpegPlayerCode(code: Int) {
//            runOnUI { toast("ffmpegEnd：$code") }
//        }
//
//        override fun onFFmpegPlayerFrame(argb32Buffer: ByteArray?, width: Int, height: Int) {
//            argb32FrameEnd = argb32Buffer?.let { ARGB32Frame(it.toList(), width, height) }
//            renderThreadEnd.pushFrame(argb32Buffer, width, height)
//            renderWeightedThread.pushVYUYFrame(argb32Buffer, width, height)
//            runOnUI {
//                resetTextureLayoutEnd(width, height)
//            }
//        }
//    }
//
//    private fun initFFmpeg() {
//        if (isUseVLC) return
//        nativeLibFFmpegStart = NativeLibFFmpeg()
//        nativeLibFFmpegEnd = NativeLibFFmpeg()
//        renderThreadStart = RenderARGB32Thread.create(binding.viewTextureStart)
//        renderThreadEnd = RenderARGB32Thread.create(binding.viewTextureEnd)
//        renderWeightedThread = RenderARGB32WeightedThread.create(binding.viewTextureResult)
//    }
//
//    private fun playStartFFmpeg(url: String) {
//        if (isUseVLC) return
//        nativeLibFFmpegStart.play(url, ffmpegStartCallback)
//        renderThreadStart.startRender(binding.viewTextureStart)
//    }
//
//    private fun playEndFFmpeg(url: String) {
//        if (isUseVLC) return
//        nativeLibFFmpegEnd.play(url, ffmpegEndCallback)
//        renderThreadEnd.startRender(binding.viewTextureEnd)
//    }
//
//    private fun releaseStartFFmpeg() {
//        if (isUseVLC) return
//        nativeLibFFmpegStart.release()
//    }
//
//    private fun releaseEndFFmpeg() {
//        if (isUseVLC) return
//        nativeLibFFmpegEnd.release()
//    }
//
//    private fun resetTextureLayoutStart(w: Int, h: Int) {
//        if (isUseVLC) return
//        if (videoStartWidth == w && videoStartHeight == h) return
//        videoStartWidth = w
//        videoStartHeight = h
//        val ratioLayout = textureViewStartSize.width / textureViewStartSize.height
//        val ratioVideo = videoStartWidth.toDouble() / videoStartHeight.toDouble()
//        val (layoutWidth, layoutHeight) =
//            if (ratioVideo > ratioLayout) {
//                val lw = textureViewStartSize.width.toInt()
//                val lh = (textureViewStartSize.width / ratioVideo).toInt()
//                Pair(lw, lh)
//            } else {
//                val lw = (textureViewStartSize.height * ratioVideo).toInt()
//                val lh = textureViewStartSize.height.toInt()
//                Pair(lw, lh)
//            }
//        val layoutParams = binding.viewTextureStart.layoutParams
//        layoutParams.width = layoutWidth
//        layoutParams.height = layoutHeight
//        binding.viewTextureStart.layoutParams = layoutParams
//    }
//
//    private fun resetTextureLayoutEnd(w: Int, h: Int) {
//        if (isUseVLC) return
//        if (videoEndWidth == w && videoEndHeight == h) return
//        videoEndWidth = w
//        videoEndHeight = h
//        val ratioLayout = textureViewEndSize.width / textureViewEndSize.height
//        val ratioVideo = videoEndWidth.toDouble() / videoEndHeight.toDouble()
//        val (layoutWidth, layoutHeight) =
//            if (ratioVideo > ratioLayout) {
//                val lw = textureViewEndSize.width.toInt()
//                val lh = (textureViewEndSize.width / ratioVideo).toInt()
//                Pair(lw, lh)
//            } else {
//                val lw = (textureViewEndSize.height * ratioVideo).toInt()
//                val lh = textureViewEndSize.height.toInt()
//                Pair(lw, lh)
//            }
//        val layoutParams = binding.viewTextureEnd.layoutParams
//        layoutParams.width = layoutWidth
//        layoutParams.height = layoutHeight
//        binding.viewTextureEnd.layoutParams = layoutParams
//    }

    /*
    * -------------------------------------------------------------------------------------------------
    * Fragment
    * -------------------------------------------------------------------------------------------------
    */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        initVLC()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.layoutVideoStart.isVisible = isUseVLC
//        binding.layoutVideoEnd.isVisible = isUseVLC
//        binding.viewTextureStart.isVisible = !isUseVLC
//        binding.viewTextureEnd.isVisible = !isUseVLC
//
//        binding.viewTextureStart.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                textureViewStartSize.width = binding.viewTextureStart.measuredWidth.toDouble()
//                textureViewStartSize.height = binding.viewTextureStart.measuredHeight.toDouble()
//                binding.viewTextureStart.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
//        binding.viewTextureEnd.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                textureViewEndSize.width = binding.viewTextureEnd.measuredWidth.toDouble()
//                textureViewEndSize.height = binding.viewTextureEnd.measuredHeight.toDouble()
//                binding.viewTextureEnd.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
//        binding.viewTextureResult.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                textureViewResultSize.width = binding.viewTextureResult.measuredWidth.toDouble()
//                textureViewResultSize.height = binding.viewTextureResult.measuredHeight.toDouble()
//                binding.viewTextureResult.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
//
//        binding.spinnerStart.setMediaUrlChoose { playUrlByStart(it) }
//        binding.spinnerEnd.setMediaUrlChoose { playUrlByEnd(it) }
//        binding.btnAddWeighted.setOnClickListener {
////            renderWeightedThread.startRender()
//            renderWeightedThread.stopRender()
//            startWeighted()
//        }
//
//        initFFmpeg()
//        initWeightedParams()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        startVLC()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        stopVLC()
//    }
//
//    override fun onDestroy() {
//        destroyVLC()
//        releaseStartFFmpeg()
//        releaseEndFFmpeg()
//        super.onDestroy()
//    }
//
//    private fun playUrlByStart(url: String) {
//        if (isUseVLC) {
//            playStartVLC(url)
//        } else {
//            playStartFFmpeg(url)
//        }
//    }
//
//    private fun playUrlByEnd(url: String) {
//        if (isUseVLC) {
//            playEndVLC(url)
//        } else {
//            playEndFFmpeg(url)
//        }
//    }
//
//    private fun startWeighted() {
//        Thread {
//            while (true) {
//                addWeighted()
//            }
//        }.start()
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun initWeightedParams() {
//        binding.seekAlpha.max = 100
//        binding.seekAlpha.progress = 50
//        binding.seekBeta.max = 100
//        binding.seekBeta.progress = 50
//        binding.seekGamma.max = 100
//        binding.seekGamma.progress = 0
//        binding.seekPipRatio.max = 5
//        binding.seekGamma.progress = 0
//        binding.textAlpha.text = "alpha[${binding.seekAlpha.progress / 100.0}]"
//        binding.textBeta.text = "beta[${binding.seekBeta.progress / 100.0}]"
//        binding.textGamma.text = "gamma[${binding.seekGamma.progress / 100.0}]"
//        binding.textPipRatio.text = "pip ratio[${(binding.seekPipRatio.progress + 3) / 10.0}]"
//        binding.seekAlpha.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            @SuppressLint("SetTextI18n")
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                binding.textAlpha.text = "alpha[${progress / 100.0}]"
//                renderWeightedThread.setWeightedParam(alpha = progress / 100.0)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//        binding.seekBeta.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            @SuppressLint("SetTextI18n")
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                binding.textBeta.text = "beta[${progress / 100.0}]"
//                renderWeightedThread.setWeightedParam(beta = progress / 100.0)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//        binding.seekGamma.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            @SuppressLint("SetTextI18n")
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                binding.textGamma.text = "gamma[${progress / 100.0}]"
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//        binding.seekPipRatio.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                binding.textPipRatio.text = "pip ratio[${(binding.seekPipRatio.progress + 3) / 10.0}]"
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//    }
//
//    private var argb32FrameStart: ARGB32Frame? = null
//    private var argb32FrameEnd: ARGB32Frame? = null
//    private var bitmapStart: Bitmap? = null
//    private var bitmapEnd: Bitmap? = null
//    private var pipSize = Size(0.0, 0.0)
//
//    private fun getPipSize(ratio: Double): Size {
//        val pipWidth = binding.viewTextureResult.measuredWidth * ratio
//        val pipHeight = binding.viewTextureResult.measuredHeight * ratio
//        if (pipSize.width != pipWidth) {
//            pipSize.width = pipWidth
//        }
//        if (pipSize.height != pipHeight) {
//            pipSize.height = pipHeight
//        }
//        return pipSize
//    }
//
//    private fun pipAndWeighted(matARGB32: Mat, matYUV: Mat, matResult: Mat, pipSize: Size, alpha: Double, beta: Double, gamma: Double) {
//        // 中心点
//        val centerPoint = Point((matARGB32.width() / 2).toDouble(), (matARGB32.height() / 2).toDouble())
//        // （画中画内容）区域大小
//        val rectYUV = Rect(
//            (centerPoint.x - (pipSize.width / 2)).toInt(),
//            (centerPoint.y - (pipSize.height / 2)).toInt(),
//            pipSize.width.toInt(),
//            pipSize.height.toInt(),
//        )
//        // （画中画内容）区域图像
//        val matRoiYUV = Mat(matYUV, rectYUV)
//
//        // （画中画对应的背景）区域大小
//        val rectARGB32 = Rect(
//            (centerPoint.x - (matRoiYUV.width() / 2)).toInt(),
//            (centerPoint.y - (matRoiYUV.height() / 2)).toInt(),
//            matRoiYUV.width(),
//            matRoiYUV.height(),
//        )
//        // （画中画对应的背景）区域图像
//        val marRoiArgb32 = Mat(matARGB32, rectARGB32)
//
//        // 画中画区域内容融合处理
//        Core.addWeighted(marRoiArgb32, alpha, matRoiYUV, beta, gamma, matResult)
//
//        // 画中画背景区域提取出来
//        val matARGB32Submat = matARGB32.submat(rectARGB32)
//        // 提取出来的位置替换内容
//        matResult.copyTo(matARGB32Submat)
//        // 再赋值给result
//        matARGB32.copyTo(matResult)
//
//        // 释放
//        matRoiYUV.release()
//        marRoiArgb32.release()
//        matARGB32Submat.release()
//    }
//
//    private fun addWeighted() {
//        val matStart = Mat()
//        val matEnd = Mat()
//        val matResult = Mat()
//
//        argb32FrameStart?.let { frameStart ->
//            if (frameStart.width * frameStart.height <= 0) return
//            if (bitmapStart == null || bitmapStart?.width != frameStart.width || bitmapStart?.height != frameStart.height) {
//                bitmapStart = Bitmap.createBitmap(frameStart.width, frameStart.height, Bitmap.Config.ARGB_8888)
//            }
//            bitmapStart?.copyPixelsFromBuffer(ByteBuffer.wrap(frameStart.buffer.toByteArray()))
//            Utils.bitmapToMat(bitmapStart, matStart)
//
//            if (binding.radioWeightedImage.isChecked) {
//                // image
//                if (bitmapEnd == null) {
//                    bitmapEnd = BitmapFactory.decodeResource(requireContext().resources, R.mipmap.test_6000x4000)
//                }
//            } else if (binding.radioWeightedVideo.isChecked) {
//                // video
//                argb32FrameEnd?.let { frameEnd ->
//                    if (frameEnd.width * frameEnd.height <= 0) return
//                    if (bitmapEnd == null || bitmapEnd?.width != frameEnd.width || bitmapEnd?.height != frameEnd.height) {
//                        bitmapEnd = Bitmap.createBitmap(frameEnd.width, frameEnd.height, Bitmap.Config.ARGB_8888)
//                    }
//                    bitmapEnd?.copyPixelsFromBuffer(ByteBuffer.wrap(frameEnd.buffer.toByteArray()))
//
//                    val matEndTmp = Mat()
//                    Utils.bitmapToMat(bitmapEnd, matEndTmp)
//                    Imgproc.resize(matEndTmp, matEnd, matStart.size())
//
//                    argb32FrameEnd = null
//                } ?: return
//            }
//
//            val matStartTmp = Mat()
//            Utils.bitmapToMat(bitmapStart, matStartTmp)
//            Imgproc.resize(matStartTmp, matStart, Size(binding.viewTextureResult.measuredWidth.toDouble(), binding.viewTextureResult.measuredHeight.toDouble()))
//            matStartTmp.release()
//
//            val matEndTmp = Mat()
//            Utils.bitmapToMat(bitmapEnd, matEndTmp)
//            Imgproc.resize(matEndTmp, matEnd, matStart.size())
//            matEndTmp.release()
//
//            val alpha = binding.seekAlpha.progress / 100.0
//            val beta = binding.seekBeta.progress / 100.0
//            val gamma = binding.seekGamma.progress / 100.0
//            val pipRatio = (binding.seekPipRatio.progress + 3) / 10.0
//            pipAndWeighted(matStart, matEnd, matResult, getPipSize(pipRatio), alpha, beta, gamma)
//
//            val bitmapResult = Bitmap.createBitmap(matResult.width(), matResult.height(), Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(matResult, bitmapResult)
//            if (binding.viewTextureResult.isAvailable) {
//                binding.viewTextureResult.lockCanvas()?.let { canvas ->
//                    bitmapResult?.let { bitmap ->
//                        val dst = android.graphics.Rect(0, 0, bitmapResult.width, bitmapResult.height)
//                        canvas.drawBitmap(bitmap, null, dst, null)
//                    }
//                    binding.viewTextureResult.unlockCanvasAndPost(canvas)
//                }
//            }
//
//            matStart.release()
//            matEnd.release()
//            matResult.release()
//            argb32FrameStart = null
//            bitmapResult.recycle()
//        }
//    }
//
//    private var videoResultWidth = 0
//    private var videoResultHeight = 0
//    private fun resetResultTextureLayout(w: Int, h: Int) {
//        if (videoResultWidth == w && videoResultHeight == h) return
//        videoResultWidth = w
//        videoResultHeight = h
//        val ratioLayout = textureViewResultSize.width / textureViewResultSize.height
//        val ratioVideo = videoResultWidth.toDouble() / videoResultHeight.toDouble()
//        val (layoutWidth, layoutHeight) =
//            if (ratioVideo > ratioLayout) {
//                val lw = textureViewResultSize.width.toInt()
//                val lh = (textureViewResultSize.width / ratioVideo).toInt()
//                Pair(lw, lh)
//            } else {
//                val lw = (textureViewResultSize.height * ratioVideo).toInt()
//                val lh = textureViewResultSize.height.toInt()
//                Pair(lw, lh)
//            }
//        val layoutParams = binding.viewTextureResult.layoutParams
//        layoutParams.width = layoutWidth
//        layoutParams.height = layoutHeight
//        binding.viewTextureResult.layoutParams = layoutParams
//    }
//
//    private fun runOnUI(action: Runnable) {
//        requireActivity().runOnUiThread(action)
//    }

}