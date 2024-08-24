@file:Suppress("unused")
package com.youaji.libs.util.mmkv

import android.content.Context
import androidx.startup.Initializer
import com.tencent.mmkv.MMKV

class MMKVInitializer : Initializer<Unit> {

  override fun create(context: Context) {
    if (MMKVOwner.default == null) {
      MMKV.initialize(context)
      MMKVOwner.default = MMKV.defaultMMKV()
    }
  }

  override fun dependencies() = emptyList<Class<Initializer<*>>>()
}