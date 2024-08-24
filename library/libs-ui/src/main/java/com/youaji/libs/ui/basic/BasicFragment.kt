@file:Suppress("unused")
package com.youaji.libs.ui.basic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment

import com.youaji.libs.ui.state.Decorative
import com.youaji.libs.ui.state.LoadingState
import com.youaji.libs.ui.state.LoadingStateDelegate
import com.youaji.libs.ui.state.OnReloadListener

/**
 * @author youaji
 * @since 2022/9/13
 */
abstract class BasicFragment(private val layoutRes: Int) :
    Fragment(),
    LoadingState by LoadingStateDelegate(),
    OnReloadListener,
    Decorative,
    FragmentVisibility {

    /** 如果对用户可见，则为True */
    private var isFragmentVisible = false

    /** 如果用户第一次看到，则为 True */
    private var isFragmentVisibleFirst = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(layoutRes, container, false)
        return root.decorate(this, this)
    }

    override fun onResume() {
        super.onResume()

        setFragmentVisible()
    }

    override fun onPause() {
        super.onPause()

        setFragmentInvisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            setFragmentInvisible()
        } else {
            setFragmentVisible()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            setFragmentVisible()
        } else {
            setFragmentInvisible()
        }
    }

    override fun isVisibleToUser(): Boolean =
        isFragmentVisible

    override fun setFragmentVisible() {
        val parent = parentFragment
        if (parent != null && parent is BasicFragment) {
            if (!parent.isVisibleToUser()) {
                // 父 Fragment 不可见，子 Fragment 必定不可见。
                return
            }
        }

        if (isResumed && !isHidden && userVisibleHint && !isFragmentVisible) {
            isFragmentVisible = true
            onVisible()
            if (isFragmentVisibleFirst) {
                isFragmentVisibleFirst = false
                onVisibleFirst()
            } else {
                onVisibleExceptFirst()
            }
            setChildFragmentVisible()
        }
    }

    override fun setFragmentInvisible() {
        if (isFragmentVisible) {
            isFragmentVisible = false
            onInvisible()
            setChildFragmentInvisible()
        }
    }

    private fun setChildFragmentVisible() {
        childFragmentManager.fragments.forEach {
            if (it is FragmentVisibility) {
                it.setFragmentVisible()
            }
        }
    }

    private fun setChildFragmentInvisible() {
        childFragmentManager.fragments.forEach {
            if (it is FragmentVisibility) {
                it.setFragmentInvisible()
            }
        }
    }
}