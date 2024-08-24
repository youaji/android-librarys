package com.youaji.libs.debug.crash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.youaji.libs.debug.crash.databinding.LibsDebugCrashActivityCrashBinding
import com.youaji.libs.debug.crash.databinding.LibsDebugCrashItemCrashBinding
import com.youaji.libs.debug.crash.util.FileUtil
import com.youaji.libs.debug.crash.util.CrashLibUtils
import com.youaji.libs.ui.adapter.RecycleViewAdapter
import com.youaji.libs.util.design.alertDialog
import com.youaji.libs.util.design.cancelButton
import com.youaji.libs.util.design.okButton
import com.youaji.libs.util.topActivity
import java.io.File

/**
 * 崩溃展示页面
 * @author youaji
 * @since 2024/01/05
 */
class CrashActivity : AppCompatActivity() {

    companion object {
        fun start() {
            val intent = Intent(topActivity.applicationContext, CrashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            topActivity.applicationContext.startActivity(intent)
        }
    }

    private lateinit var binding: LibsDebugCrashActivityCrashBinding
    private val crashList = mutableListOf<File>()
    private val crashAdapter = CrashAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LibsDebugCrashActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        initList()
        initAdapter()
        initRefresh()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initToolbar() {
        window.statusBarColor = Color.BLACK
        binding.toolbar.title = "崩溃日志"
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setBackgroundColor(Color.BLACK)
        binding.toolbar.setNavigationIcon(R.drawable.libs_debug_crash_ic_close_24_white)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.overflowIcon = AppCompatResources.getDrawable(this, R.drawable.libs_debug_crash_ic_more_vert_24_white)
        binding.toolbar.inflateMenu(R.menu.libs_debug_crash_menu_crash_list)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.refresh_list -> getCrashList()
                R.id.delete_all -> deleteAll()
            }
            true
        }
    }

    private fun initList() {
        binding.list.itemAnimator = DefaultItemAnimator()
        binding.list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initRefresh() {
        binding.refresh.setColorSchemeColors(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE)
        binding.refresh.setOnRefreshListener { initCrashFileList() }
        binding.refresh.post {
            binding.refresh.isRefreshing = true
            initCrashFileList()
        }
    }

    private fun initCrashFileList() {
        Thread { getCrashList() }.start()
    }

    private fun getCrashList() {
        crashList.clear()
        crashList.addAll(FileUtil.getCrashFiles())
        crashList.sortWith { o1, o2 ->
            try {
                // 根据修改时间排序
                val lastModified01 = o1.lastModified()
                val lastModified02 = o2.lastModified()
                if (lastModified01 > lastModified02) -1
                else 1
            } catch (e: Exception) {
                1
            }
        }
        runOnUiThread {
            hideLoading()
            crashAdapter.dataList = crashList
            binding.refresh.isRefreshing = false
        }
    }

    private fun initAdapter() {
        binding.list.adapter = crashAdapter
        crashAdapter.itemClickListener = object : RecycleViewAdapter.OnItemClickListener<File> {
            override fun onItemClick(position: Int, bean: File) {
                CrashDetailActivity.start(bean.absolutePath)
            }
        }
        crashAdapter.itemLongClickListener = object : RecycleViewAdapter.OnItemLongClickListener<File> {
            override fun onItemLongClick(position: Int, bean: File): Boolean {
                if (crashList.size > position && position >= 0) {
                    alertDialog {
                        message = "是否删除当前日志？"
                        cancelButton()
                        okButton {
                            showLoading()
                            crashAdapter.removeAt(position)
                            FileUtil.deleteFile(bean.path)
                        }
                    }.show()
                }
                return true
            }
        }
    }

    private fun showLoading() {
        binding.loading.isVisible = true
    }

    private fun hideLoading() {
        binding.loading.isVisible = false
    }

    private fun deleteAll() {
        alertDialog {
            message = "是否删除全部日志？"
            cancelButton()
            okButton {
                showLoading()
                Thread {
                    FileUtil.deleteAllCrashFiles()
                    getCrashList()
                }.start()
            }
        }.show()
    }

    class CrashAdapter : RecycleViewAdapter<File, LibsDebugCrashItemCrashBinding>() {
        fun removeAt(position: Int) {
            if (dataList.isEmpty()) return
            if (position < 0) return
            if (position >= dataList.size) return
            dataList.removeAt(position)
            notifyItemRemoved(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<LibsDebugCrashItemCrashBinding> {
            val inflate = LibsDebugCrashItemCrashBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(inflate)
        }

        override fun onBindViewHolder(
            holder: ViewHolder<LibsDebugCrashItemCrashBinding>,
            position: Int,
            binding: LibsDebugCrashItemCrashBinding,
            bean: File
        ) {

            val fileName = bean.name.replace(".txt", "")
            val splitNames = fileName.split("--".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var spannable: Spannable? = null
            if (splitNames.size == 3) {
                val errorName = splitNames[2]
                if (errorName.isNotEmpty()) {
                    val name = fileName.replace("--", "  ")
                    spannable = Spannable.Factory.getInstance().newSpannable(name)
                    spannable = CrashLibUtils.addNewSpan(
                        binding.root.context, spannable, name, errorName,
                        Color.parseColor("#FF0006"),
                        18,
                    )
                }
            }

            var path = bean.absolutePath.replace("\\s", " ")
            path = path.replace("\\n", " ")
            binding.path.text = path
            binding.title.text = spannable ?: fileName
            binding.root.setOnClickListener { itemClickListener?.onItemClick(position, bean) }
            binding.root.setOnLongClickListener { itemLongClickListener?.onItemLongClick(position, bean) ?: false }
        }
    }
}