package com.youaji.libs.debug.file.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.youaji.libs.debug.file.databinding.LibsDebugFileItemPathBinding
import com.youaji.libs.ui.adapter.RecycleViewAdapter

class PathAdapter : RecycleViewAdapter<String, LibsDebugFileItemPathBinding>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<LibsDebugFileItemPathBinding> {
        val inflate = LibsDebugFileItemPathBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(inflate)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder<LibsDebugFileItemPathBinding>,
        position: Int,
        binding: LibsDebugFileItemPathBinding,
        bean: String
    ) {
        binding.textDir.text = " $bean /"
        binding.root.setOnClickListener { itemClickListener?.onItemClick(position, bean) }
    }
}