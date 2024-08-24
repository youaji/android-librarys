package com.youaji.libs.picture.viewer.util.notch

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.youaji.libs.picture.viewer.util.notch.OSUtils.getProp
import com.youaji.libs.picture.viewer.util.notch.OSUtils.isEMUI
import com.youaji.libs.picture.viewer.util.notch.OSUtils.isMIUI
import com.youaji.libs.picture.viewer.util.notch.OSUtils.isOPPO
import com.youaji.libs.picture.viewer.util.notch.OSUtils.isVIVO
import java.lang.reflect.InvocationTargetException

/**
 * 全屏刘海屏适配
 *
 *  * [小米适配文档](https://dev.mi.com/console/doc/detail?pId=1293)
 *  * [VIVO适配文档](https://dev.vivo.com.cn/documentCenter/doc/103)
 *  * [OPPO适配文档](https://open.oppomobile.com/wiki/doc#id=10159)
 *  * [华为适配文档](https://developer.huawei.com/consumer/cn/devservice/doc/50114)
 *
 *
 * @author Created by 汪高皖 on 2019/3/12 0012 09:39
 */
object NotchAdapterUtils {
    fun adapter(window: Window?, @CutOutMode cutOutMode: Int) {
        if (window == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // OPPO 通过windowInsets.getDisplayCutout() 拿不到 DisplayCutout，始终返回null
            // 因此只要 androidP以上就适配，不管是否是异形屏
            adapterP(window, cutOutMode)
            return
        }
        if (!isNotch(window)) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adapterO(window, cutOutMode)
        }
    }

    /**
     * 适配android P及以上系统
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun adapterP(window: Window?, @CutOutMode cutOutMode: Int) {
        if (window == null) {
            return
        }
        val lp = window.attributes
        when (cutOutMode) {
            CutOutMode.DEFAULT -> lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            CutOutMode.SHORT_EDGES, CutOutMode.ALWAYS -> lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            else -> lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }
        window.setAttributes(lp)
    }

    /**
     * 适配 android O系统
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun adapterO(window: Window?, @CutOutMode cutOutMode: Int) {
        if (window == null) {
            return
        }
        if (isMIUI) {
            adapterOWithMIUI(window, cutOutMode)
        } else if (isEMUI) {
            adapterOWithEMUI(window, cutOutMode)
        }
    }

    /**
     * 适配 MIUI android O系统
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun adapterOWithMIUI(window: Window?, @CutOutMode cutOutMode: Int) {
        if (window == null) {
            return
        }

        /*
            0x00000100 开启配置
            0x00000200 竖屏配置
            0x00000400 横屏配置
            
            0x00000100 | 0x00000200 竖屏绘制到耳朵区
            0x00000100 | 0x00000400 横屏绘制到耳朵区
            0x00000100 | 0x00000200 | 0x00000400 横竖屏都绘制到耳朵区
         */
        val flag =
            if (cutOutMode == CutOutMode.ALWAYS) {
                0x00000100 or 0x00000200 or 0x00000400
            } else {
                0x00000100 or 0x00000200
            }
        val methodName: String =
            if (cutOutMode == CutOutMode.NEVER) {
                "clearExtraFlags"
            } else if (cutOutMode == CutOutMode.DEFAULT) {
                val attributes = window.attributes
                if (attributes.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS > 0) {
                    "addExtraFlags"
                } else {
                    "clearExtraFlags"
                }
            } else {
                "addExtraFlags"
            }

        try {
            val method = Window::class.java.getMethod(methodName, Int::class.javaPrimitiveType)
            method.invoke(window, flag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 适配 EMUI android O系统
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun adapterOWithEMUI(window: Window?, @CutOutMode cutOutMode: Int) {
        if (window == null) {
            return
        }
        val FLAG_NOTCH_SUPPORT = 0x00010000
        val methodName =
            if (cutOutMode == CutOutMode.NEVER) {
                "clearHwFlags"
            } else if (cutOutMode == CutOutMode.DEFAULT) {
                val attributes = window.attributes
                if (attributes.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS > 0) {
                    "addHwFlags"
                } else {
                    "clearHwFlags"
                }
            } else {
                "addHwFlags"
            }
        val layoutParams = window.attributes
        try {
            val layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx")
            val con = layoutParamsExCls.getConstructor(WindowManager.LayoutParams::class.java)
            val layoutParamsExObj = con.newInstance(layoutParams)
            val method = layoutParamsExCls.getMethod(methodName, Int::class.javaPrimitiveType)
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 是否是异形屏
     */
    fun isNotch(window: Window): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var isNotchScreen = false
            val windowInsets = window.decorView.getRootWindowInsets()
            if (windowInsets != null) {
                val displayCutout = windowInsets.displayCutout
                if (displayCutout != null) {
                    isNotchScreen = true
                }
            }
            isNotchScreen
        } else if (isMIUI) {
            isNotchOnMIUI
        } else if (isEMUI) {
            isNotchOnEMUI(window.context)
        } else if (isVIVO) {
            isNotchOnVIVO(window.context)
        } else if (isOPPO) {
            isNotchOnOPPO(window.context)
        } else {
            false
        }
    }

    val isNotchOnMIUI: Boolean
        get() = "1" == getProp("ro.miui.notch")

    fun isNotchOnEMUI(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        var isNotch = false
        try {
            val cl = context.classLoader
            val HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
            val get = HwNotchSizeUtil.getMethod("hasNotchOnHuawei")
            isNotch = get.invoke(HwNotchSizeUtil) as Boolean
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isNotch
    }

    fun isNotchOnVIVO(context: Context?): Boolean {
        if (context == null) {
            return false
        }

        // 是否有刘海
        val VIVO_NOTCH = 0x00000020
        // 是否有圆角
        // int VIVO_FILLET = 0x00000008;
        var isNotch = false
        try {
            val classLoader = context.classLoader
            @SuppressLint("PrivateApi") val FtFeature = classLoader.loadClass("android.util.FtFeature")
            val method = FtFeature.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
            isNotch = method.invoke(FtFeature, VIVO_NOTCH) as Boolean
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isNotch
    }

    fun isNotchOnOPPO(context: Context?): Boolean {
        return context?.packageManager?.hasSystemFeature("com.oppo.feature.screen.heteromorphism") ?: false
    }
}
