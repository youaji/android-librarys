package com.youaji.libs.debug.file.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.youaji.libs.debug.file.R
import com.youaji.libs.debug.file.databinding.LibsDebugFileItemFileBinding
import com.youaji.libs.debug.file.util.FileExplorerUtils
import com.youaji.libs.ui.adapter.RecycleViewAdapter
import java.io.File

class FileAdapter : RecycleViewAdapter<File, LibsDebugFileItemFileBinding>() {
    fun removeAt(position: Int) {
        if (dataList.isEmpty()) return
        if (position < 0) return
        if (position >= dataList.size) return
        dataList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<LibsDebugFileItemFileBinding> {
        val inflate = LibsDebugFileItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindViewHolder(
        holder: ViewHolder<LibsDebugFileItemFileBinding>,
        position: Int,
        binding: LibsDebugFileItemFileBinding,
        bean: File
    ) {
        val isDirectory = bean.isDirectory
        binding.textName.text = bean.name
        binding.textDate.text = FileExplorerUtils.getFileTime(bean)
        binding.textSize.text = if (isDirectory) {
            "${bean.listFiles()?.size ?: 0}项"
        } else {
            val directorySize = FileExplorerUtils.getDirectorySize(bean)
            val printSizeForSpannable = FileExplorerUtils.getPrintSizeForSpannable(directorySize)
            printSizeForSpannable
        }
        binding.iconMore.isVisible = isDirectory
        binding.icon.setImageResource(
            if (isDirectory) R.drawable.libs_debug_file_ic_file_folder_24_black
            else R.drawable.libs_debug_file_ic_file_24_black
//                when (FileExplorerUtils.getSuffix(bean)) {
//                "jpg" -> R.drawable.libs_debug_file_ic_file_jpg_24_black
//                "txt" -> R.drawable.libs_debug_file_ic_file_txt_24_black
//                "db" -> R.drawable.libs_debug_file_ic_file_jpg_24_black
//                else -> R.drawable.libs_debug_file_ic_file_jpg_24_black
//            }
        )
        binding.root.setOnClickListener { itemClickListener?.onItemClick(position, bean) }
        binding.root.setOnLongClickListener { itemLongClickListener?.onItemLongClick(position, bean) ?: false }
    }
}