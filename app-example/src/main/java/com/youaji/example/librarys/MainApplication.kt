package com.youaji.example.librarys

import android.app.Application
import com.youaji.libs.debug.DebugService
//import com.youaji.module.basic.BasicApplication
//import com.youaji.module.basic.BasicConfig

class MainApplication : Application() {

    companion object {}

    override fun onCreate() {
        super.onCreate()
        DebugService.get.initCrash(this)
        DebugService.get.initPgyer(
            this,
            "",
            "",
        )
    }

//    override fun config(config: BasicConfig) {
//    }
}