package com.youaji.libs.debug.crash

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.youaji.libs.debug.crash.databinding.LibsDebugCrashActivityCrashDetailBinding
import com.youaji.libs.debug.crash.util.CompressUtils
import com.youaji.libs.debug.crash.util.ScreenShotsUtils
import com.youaji.libs.debug.crash.util.FileUtil
import com.youaji.libs.debug.crash.util.CrashLibUtils
import com.youaji.libs.util.activityList
import com.youaji.libs.util.intent
import com.youaji.libs.util.startActivity
import com.youaji.libs.util.toast
import java.io.File

/**
 * 崩溃详情页面展示
 * @author youaji
 * @since 2024/01/05
 */
class CrashDetailActivity : AppCompatActivity() {

    private lateinit var binding: LibsDebugCrashActivityCrashDetailBinding
    private val filePath: String by intent("filePath") { "" }

    /** 崩溃日志的内容 */
    private var crashContent: String = ""

    /** 具体的异常类型 */
    private var matchErrorInfo: String = ""

    companion object {
        fun start(filePath: String) = startActivity<CrashDetailActivity>("filePath" to filePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LibsDebugCrashActivityCrashDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initData()
        binding.screenShot.setOnClickListener { it.isVisible = false }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initToolbar() {
        window.statusBarColor = Color.BLACK
        binding.toolbar.title = "日志详情"
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setBackgroundColor(Color.BLACK)
        binding.toolbar.setNavigationIcon(R.drawable.libs_debug_crash_ic_arrow_back_24_white)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.overflowIcon = AppCompatResources.getDrawable(this, R.drawable.libs_debug_crash_ic_more_vert_24_white)
        binding.toolbar.inflateMenu(R.menu.libs_debug_crash_menu_crash_detail)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.share -> shareLogFile()
                R.id.copy -> copyContent()
                R.id.shot -> saveShot()
            }
            true
        }
    }

    private fun initData() {
        Thread {
            // 获取文件夹名字匹配异常信息高亮显示
            val file = File(filePath)
            val splitNames = file.name.replace(".txt", "").split("--".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (splitNames.size == 3) {
                val errorName = splitNames[2]
                if (errorName.isNotEmpty()) {
                    matchErrorInfo = errorName
                }
            }
            // 获取内容
            crashContent = FileUtil.readFile(filePath)

            // 富文本显示
            var spannable = Spannable.Factory.getInstance().newSpannable(crashContent)
            // 匹配错误信息
            if (matchErrorInfo.isNotEmpty()) {
                spannable = CrashLibUtils.addNewSpan(this@CrashDetailActivity, spannable, crashContent, matchErrorInfo, Color.parseColor("#FF0006"), 18)
            }
            // 匹配包名
            spannable = CrashLibUtils.addNewSpan(this@CrashDetailActivity, spannable, crashContent, packageName, Color.parseColor("#0070BB"), 0)

            // 匹配Activity
            activityList.forEach {
                spannable = CrashLibUtils.addNewSpan(this@CrashDetailActivity, spannable, crashContent, it.javaClass.simpleName, Color.parseColor("#55BB63"), 16)
            }

            runOnUiThread {
                binding.content.text = spannable ?: crashContent
            }
        }.start()
    }

    private fun shareLogFile() {
        // 先把文件转移到外部存储文件
        val srcFile = File(filePath)
        val destFilePath: String = FileUtil.crashSharePath + "/CrashShare.txt"
        val destFile = File(destFilePath)
        val copy: Boolean = FileUtil.copyFile(srcFile, destFile)
        if (copy) {
            CrashLibUtils.shareFile(this@CrashDetailActivity, destFile)
        } else {
            toast("文件保存失败")
        }
    }

    private fun copyContent() {
        val clipboardManager = this@CrashDetailActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建ClipData对象
        val clipData: ClipData = ClipData.newPlainText("CrashLog", crashContent)
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData)
        toast("复制内容成功")
    }

    private fun saveShot() {
        val bitmap = ScreenShotsUtils.measureSize(this, binding.scroll)
        Thread { savePicture(bitmap) }.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun savePicture(bitmap: Bitmap?) {
        if (bitmap != null) {
            val crashPicPath: String = FileUtil.crashShotFile
            val saveBitmap  = CrashLibUtils.saveBitmap(this@CrashDetailActivity, bitmap, crashPicPath)
            if (saveBitmap) {
                val bitmapCompress: Bitmap = CompressUtils.getBitmap(File(crashPicPath), 200, 200)
                runOnUiThread {
                    toast("保存截图成功，请到相册查看\n路径：$crashPicPath")
                    binding.screenShot.setImageBitmap(bitmapCompress)
                    binding.screenShot.isVisible = true
                }
            } else {
                toast("保存截图失败")
            }
        } else {
            toast("保存截图失败")
        }
    }


}