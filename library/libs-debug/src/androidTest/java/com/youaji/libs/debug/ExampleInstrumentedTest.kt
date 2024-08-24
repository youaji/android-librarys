package com.youaji.libs.debug

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.youaji.libs.debug.pgyer.Version
import com.youaji.libs.util.logger.logDebug

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.youaji.library.debug.test", appContext.packageName)
    }

    @Test
    fun testCompareVersion() {
//        println("testCompareVersion start")
//        println("result:" + Version.compareVersion("0.0.1.1", "1.0.1"))
//        println("testCompareVersion end")
    }
}