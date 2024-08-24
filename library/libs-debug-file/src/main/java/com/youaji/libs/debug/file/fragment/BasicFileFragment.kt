package com.youaji.libs.debug.file.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.youaji.libs.debug.file.FileExplorerActivity
import java.io.File

const val paramFileKey = "param_file"

abstract class BasicFileFragment : Fragment() {

    /** /data/user/0/'package'/ */
    protected val filesDir: File? by lazy { context?.filesDir?.parentFile }

    /** /storage/emulated/0/Android/data/'package'/cache */
    protected val externalCacheDir: File? by lazy { context?.externalCacheDir }

    /** /storage/emulated/0/Android/data/'package'/files */
    protected val externalFilesDir: File? by lazy { context?.getExternalFilesDir(null as String?) }

    protected val rootActivity: FileExplorerActivity? by lazy { activity as FileExplorerActivity? }
    protected var currentFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { currentFile = it.getSerializable(paramFileKey) as File? }
    }

    override fun onResume() {
        updateToolbar()
        super.onResume()
    }

    abstract fun updateToolbar()

}