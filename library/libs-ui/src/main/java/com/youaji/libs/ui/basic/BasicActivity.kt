@file:Suppress("unused")

package com.youaji.libs.ui.basic

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import com.gyf.immersionbar.ktx.immersionBar

import com.youaji.libs.ui.state.Decorative
import com.youaji.libs.ui.state.LoadingState
import com.youaji.libs.ui.state.LoadingStateDelegate
import com.youaji.libs.ui.state.OnReloadListener

/**
 * @author youaji
 * @since 2022/9/13
 */
abstract class BasicActivity(private val layoutRes: Int) :
    AppCompatActivity(),
    LoadingState by LoadingStateDelegate(),
    OnReloadListener,
    Decorative {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        decorateContentView(this, this)
        immersionBar()
    }
}