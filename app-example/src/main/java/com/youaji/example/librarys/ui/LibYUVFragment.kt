package com.youaji.example.librarys.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.example.librarys.databinding.FragmentLibYuvBinding
import com.youaji.libs.ui.basic.BasicBindingFragment
//import com.youaji.module.render.thread.RenderARGB32Thread

class LibYUVFragment : BasicBindingFragment<FragmentLibYuvBinding>() {

//    private lateinit var renderThread: RenderARGB32Thread

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        super.onCreateView(inflater, container, savedInstanceState)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        renderThread = RenderARGB32Thread.create(binding.viewTexture)
//        openFile("/sdcard/yuyv_25_256x192.yuv")
//        renderThread.startRender(binding.viewTexture)
    }

    override fun onDestroy() {
        super.onDestroy()
//        renderThread.stopRender()
    }

    private fun openFile(path: String) {
//        renderThermalThread.pushFrame(argb32Buffer, width, height)
//        val input = requireContext().openFileInput(fileName)
    }


}