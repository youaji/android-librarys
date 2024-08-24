package com.youaji.libs.debug.file.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.youaji.libs.debug.file.FileExplorerActivity
import com.youaji.libs.debug.file.adapter.FileAdapter
import com.youaji.libs.debug.file.databinding.LibsDebugFileFragmentFileRootBinding
import com.youaji.libs.debug.file.util.FileExplorerUtils
import com.youaji.libs.ui.adapter.RecycleViewAdapter
import com.youaji.libs.util.dp
import com.youaji.libs.widget.recyclerView.decoration.LinearItemDecoration
import java.io.File

/**
 * 相关沙盒文件目录
 * @author youaji
 * @since 2024/01/05
 */
class FileRootFragment : BasicFileFragment() {
    private lateinit var binding: LibsDebugFileFragmentFileRootBinding
    private val fileAdapter = FileAdapter()

    /** 获取root文件 */
    private val rootFiles: List<File>
        get() {
            arguments?.let {
                @Suppress("DEPRECATION")
                val dir = it.getSerializable("dir_key") as File?
                if (dir != null && dir.exists()) {
                    val files = dir.listFiles() ?: return listOf()
                    return listOf(*files)
                }
            }
            return listOf()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibsDebugFileFragmentFileRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initData()
    }

    private fun initList() {
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.addItemDecoration(LinearItemDecoration(spaceVertical = 2.dp.toInt()))
        binding.list.adapter = fileAdapter
        fileAdapter.itemClickListener = object : RecycleViewAdapter.OnItemClickListener<File> {
            override fun onItemClick(position: Int, bean: File) {
                if (!bean.exists()) return
                rootActivity?.browseFile(FileExplorerActivity.BrowseType.FileList(bean))
            }
        }
    }

    private fun initData() {
        fileAdapter.dataList = getRootFile().toMutableList()
        updateToolbar()
    }

    override fun updateToolbar() {
        rootActivity?.updateToolbar(FileExplorerActivity.ToolbarType.Root)
    }

    /** 初始化 root file 文件 */
    private fun getRootFile(): List<File> {
        return if (rootFiles.isEmpty()) getDefaultRootFiles()
        else {
            val files = mutableListOf<File>()
            rootFiles.forEach { files.add(it) }
            files
        }
    }

    /**
     * 初始化默认文件。注意：加 External和不加(默认)的比较
     * 相同点:
     *  1.都可以做 app 缓存目录。
     *  2.app 卸载后，两个目录下的数据都会被清空。
     * 不同点:
     *  1.目录的路径不同。前者的目录存在外部 SD 卡上的。后者的目录存在 app 的内部存储上。
     *  2.前者的路径在手机里可以直接看到。后者的路径需要root以后，用Root Explorer 文件管理器才能看到。
     */
    private fun getDefaultRootFiles(): List<File> {
        val files = mutableListOf<File>()
        // 第一个是文件父路径
        filesDir?.let {
            files.add(it)
            FileExplorerUtils.logInfo(it.path)
        }

        // 第二个是缓存文件路径
        externalCacheDir?.let {
            files.add(it)
            FileExplorerUtils.logInfo(it.path)
        }

        // 第三个是外部file路径
        externalFilesDir?.let {
            files.add(it)
            FileExplorerUtils.logInfo(it.path)
        }

        return files
    }
}