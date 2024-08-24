@file:Suppress("unused")
package com.youaji.libs.ui.state

import android.view.View

interface Decorative {
  val isDecorated: Boolean get() = true
  val contentView: View? get() = null
}