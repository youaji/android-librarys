package com.youaji.example.librarys.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.example.librarys.databinding.FragmentHomeBinding
import com.youaji.libs.ui.basic.BasicBindingFragment
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.widget.range.box.RangeBox

class HomeFragment : BasicBindingFragment<FragmentHomeBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hiiii.setTickText(arrayOf("5kHz","10kHz","15kHz","20kHz","25kHz"))
        binding.hiiii.setTickRange(0, 100)
        binding.hiiii.setChangeListener(object : RangeBox.OnChangeListener {
            override fun onChanged(view: RangeBox, start: Float, end: Float, completed: Boolean) {
                logDebug("$start $end $completed")
            }
        })
    }

}