package com.youaji.libs.debug.file

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.youaji.libs.debug.file.databinding.LibsDebugFileActivityFileExplorerBinding
import com.youaji.libs.debug.file.fragment.FileRootFragment
import com.youaji.libs.debug.file.fragment.FileListFragment
import com.youaji.libs.debug.file.fragment.paramFileKey
import com.youaji.libs.util.topActivity
import java.io.File
import java.util.ArrayDeque

/**
 * 文件管理
 * @author youaji
 * @since 2024/01/05
 */
class FileExplorerActivity : AppCompatActivity() {

    companion object {
        fun start() {
            try {
                val intent = Intent()
                intent.setClass(topActivity, FileExplorerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                topActivity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private lateinit var binding: LibsDebugFileActivityFileExplorerBinding

    /** 一个双端队列实现，不过它内部使用的是数组来对元素进行操作，不允许存储null值，同时可以当做队列，双端队列，栈来进行使用。 */
    private val fragments = ArrayDeque<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LibsDebugFileActivityFileExplorerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initToolbar()
        browseFile(BrowseType.FileRoot)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (fragments.isNotEmpty()) {
            val fragment = fragments.first
            if (fragment is FileListFragment) {
                if (fragment.onBackPressed())
                    return
            }
            fragments.remove(fragment)
            supportFragmentManager.popBackStack()   // 回退 fragment 操作
            if (fragments.isNotEmpty()) {
                fragments.first?.onResume()
                return
            }
        }
        finish()
        super.onBackPressed()
    }

    private fun initToolbar() {
        window.statusBarColor = Color.BLACK
        binding.toolbar.setTitleTextColor(Color.WHITE)
        binding.toolbar.setBackgroundColor(Color.BLACK)
        binding.toolbar.setNavigationOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }
    }

    /** 添加fragment */
    private fun addFragment(target: Class<out Fragment>, bundle: Bundle? = null) {
        try {
            val fragment = target.newInstance()
            bundle?.let { fragment.arguments = bundle }
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(binding.layoutFrame.id, fragment)
            fragments.addFirst(fragment)
            fragmentTransaction.addToBackStack("")
            fragmentTransaction.commit()    // 将 fragment 提交到任务栈中
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun browseFile(type: BrowseType) {
        val bundle = Bundle()
        bundle.putSerializable(paramFileKey, type.file)
        addFragment(type.target, bundle)
        val toolbarType = when (type) {
            BrowseType.FileRoot -> ToolbarType.Root
            is BrowseType.FileList -> ToolbarType.File(type.file.name)
        }
        updateToolbar(toolbarType)
    }

    fun updateToolbar(type: ToolbarType) {
        binding.toolbar.title = type.title
        when (type) {
            is ToolbarType.File -> {
                binding.toolbar.setNavigationIcon(R.drawable.libs_debug_file_ic_arrow_back_24_white)
            }

            ToolbarType.Root -> {
                binding.toolbar.setNavigationIcon(R.drawable.libs_debug_file_ic_close_24_white)
            }
        }
    }

    sealed class BrowseType(open val file: File?, val target: Class<out Fragment>) {
        object FileRoot : BrowseType(null, FileRootFragment::class.java)
        data class FileList(override val file: File) : BrowseType(file, FileListFragment::class.java)
    }

    sealed class ToolbarType(open val title: String) {
        object Root : ToolbarType("应用沙盒")
        data class File(override val title: String) : ToolbarType(title)
    }

}