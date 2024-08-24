package com.youaji.example.librarys.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.youaji.example.librarys.databinding.FragmentSocketBinding
import com.youaji.example.librarys.ui.socket.SocketOkFragment
import com.youaji.libs.ui.basic.BasicBindingFragment

class SocketFragment : BasicBindingFragment<FragmentSocketBinding>() {

    private val pages = arrayOf(Pair("OK", SocketOkFragment()), Pair("LIB", LibSocketFragment()))
    private var tabLayoutMediator: TabLayoutMediator? = null
    private val pageChangeCallback: ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val tabCount: Int = binding.layoutTab.tabCount
            for (i in 0 until tabCount) {
                val tab: TabLayout.Tab? = binding.layoutTab.getTabAt(i)
                val tabView: TextView = tab?.customView as TextView
                tabView.gravity = Gravity.CENTER
                if (tab.position == position) {
                    tabView.setTextColor(Color.BLACK)
                    tabView.typeface = Typeface.DEFAULT_BOLD
                    tabView.textSize = 20f
                    tabView.maxLines = 1
                } else {
                    tabView.setTextColor(Color.GRAY)
                    tabView.typeface = Typeface.DEFAULT
                    tabView.textSize = 16f
                    tabView.maxLines = 1
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPager()
    }

    override fun onDestroy() {
        tabLayoutMediator?.detach()
        binding.pagerView.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroy()
    }

    private fun initPager() {
        binding.pagerView.registerOnPageChangeCallback(pageChangeCallback)
        binding.pagerView.offscreenPageLimit = pages.size
        binding.pagerView.isNestedScrollingEnabled = false
        binding.pagerView.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun getItemCount(): Int = pages.size
            override fun createFragment(position: Int): Fragment = pages[position].second
        }

        tabLayoutMediator = TabLayoutMediator(binding.layoutTab, binding.pagerView) { tab, position ->
            val textView = TextView(requireContext())
            textView.text = pages[position].first
            tab.customView = textView
        }
        tabLayoutMediator?.attach()
    }

}