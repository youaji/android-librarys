@file:Suppress("unused")
package com.youaji.libs.event.bus

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

object EventBusInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        EventBus.application = context.applicationContext as Application
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()
}