package com.youaji.libs.debug.file.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.youaji.libs.debug.file.FileExplorerActivity
import com.youaji.libs.debug.file.adapter.FileAdapter
import com.youaji.libs.debug.file.adapter.PathAdapter
import com.youaji.libs.debug.file.databinding.LibsDebugFileFragmentFileListBinding
import com.youaji.libs.debug.file.util.FileExplorerUtils
import com.youaji.libs.debug.file.util.OpenFileUtil
import com.youaji.libs.ui.adapter.RecycleViewAdapter
import com.youaji.libs.util.design.alertDialog
import com.youaji.libs.util.design.cancelButton
import com.youaji.libs.util.design.okButton
import com.youaji.libs.util.design.selector
import com.youaji.libs.util.dp
import com.youaji.libs.util.toast
import com.youaji.libs.widget.recyclerView.decoration.LinearItemDecoration
import java.io.File


/**
 * 文件列表
 * @author youaji
 * @since 2024/01/05
 */
class FileListFragment : BasicFileFragment() {
    private lateinit var binding: LibsDebugFileFragmentFileListBinding

    private val fileAdapter = FileAdapter()
    private val pathAdapter = PathAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LibsDebugFileFragmentFileListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = true
            refreshList()
            binding.refresh.isRefreshing = false
        }
        initFileList()
        initPathList()
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        updateToolbar()
    }

    fun onBackPressed(): Boolean {
        val isHandler =
            if (isRootFile(currentFile) || currentFile == null)
                false
            else {
                val parentFile = currentFile?.parentFile
                currentFile = parentFile
                refreshList()
                if (isRootFile(parentFile)) {
                    currentFile = null
                }
                true
            }
        return isHandler
    }

    private fun initFileList() {
        binding.listFile.layoutManager = LinearLayoutManager(context)
        binding.listFile.addItemDecoration(LinearItemDecoration(spaceVertical = 2.dp.toInt()))
        binding.listFile.adapter = fileAdapter
        fileAdapter.itemClickListener = object : RecycleViewAdapter.OnItemClickListener<File> {
            override fun onItemClick(position: Int, bean: File) {
                if (!bean.exists()) return
                if (bean.isFile) {
                    OpenFileUtil.open(requireContext(), bean)
                } else {
                    currentFile = bean
                    refreshList()
                }
            }
        }
        fileAdapter.itemLongClickListener = object : RecycleViewAdapter.OnItemLongClickListener<File> {
            override fun onItemLongClick(position: Int, bean: File): Boolean {
                selector(listOf("删除", "分享")) { _, i ->
                    when (i) {
                        0 -> deleteFile(bean, position)
                        1 -> shareFile(bean)
                    }
                }
                return true
            }
        }
    }

    private fun initPathList() {
        binding.listPath.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.listPath.adapter = pathAdapter
        pathAdapter.itemClickListener = object : RecycleViewAdapter.OnItemClickListener<String> {
            override fun onItemClick(position: Int, bean: String) {}
        }
    }

    private fun refreshList() {
        currentFile?.let { file ->
            val files = getFiles(file)
            fileAdapter.dataList = files as MutableList<File>
            pathAdapter.dataList = file.absolutePath.split("/") as MutableList<String>
            binding.listPath.scrollToPosition(pathAdapter.dataList.size - 1)
            updateToolbar()
        }
    }

    override fun updateToolbar() {
        rootActivity?.updateToolbar(FileExplorerActivity.ToolbarType.File(currentFile?.name ?: ""))
    }

    /** 获取某个 file 对应的子 file 列表 */
    private fun getFiles(dir: File?): List<File> {
        val files = mutableListOf<File>()
        dir?.listFiles()?.let { files.addAll(it) }
        return files
    }

    /** 判断是否是 root file文件 */
    private fun isRootFile(file: File?): Boolean =
        file == filesDir || file == externalCacheDir || file == externalFilesDir

    private fun deleteFile(file: File, position: Int) {
        context?.alertDialog {
            message = "是否删除当前文件${if (file.isDirectory) "夹" else ""}?"
            cancelButton()
            okButton {
                Thread {
                    if (FileExplorerUtils.deleteFile(file)) {
                        activity?.runOnUiThread {
                            toast("删除成功")
                            fileAdapter.removeAt(position)
                        }
                    } else {
                        activity?.runOnUiThread { toast("删除失败") }
                    }
                }.start()
            }
        }?.show()
    }

    private fun shareFile(file: File) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".fileExplorerProvider", file)
        } else {
            Uri.fromFile(file)
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_STREAM, uri) //传输图片或者文件 采用流的方式

        intent.type = "*/*" //分享文件
        context?.startActivity(Intent.createChooser(intent, file.name))
    }
}