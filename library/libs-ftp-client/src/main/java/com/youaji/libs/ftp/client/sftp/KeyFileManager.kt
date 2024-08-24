package com.youaji.libs.ftp.client.sftp

import android.content.Context
import android.net.Uri
import java.io.*

class KeyFileManager(private val context: Context) {

    private val keyNames = "sftp_ssh_keys"
    private val tempDir = File(context.cacheDir, keyNames)

    init {
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
    }

    fun file(id: Int): File {
        return File(context.filesDir, filename(id))
    }

    fun delete(id: Int) {
        context.deleteFile(filename(id))
    }

    fun copy(id: Int, newId: Int) {
        val fos = context.openFileOutput(filename(newId), Context.MODE_PRIVATE)
        val fis = context.openFileInput(filename(id))
        copyStream(fis, fos)
    }

    fun storeTemp(uri: Uri): File {
        val input = context.contentResolver.openInputStream(uri)!!
        val file = File(tempDir, uri.hashCode().toString())
        copyStream(input, FileOutputStream(file))
        return file
    }

    fun tempToFinal(tmp: File, id: Int) {
        val fis = FileInputStream(tmp)
        val fos = context.openFileOutput(filename(id), Context.MODE_PRIVATE)
        copyStream(fis, fos)
        tmp.delete()
    }

    fun finalToTemp(id: Int): File {
        val file = File(tempDir, id.hashCode().toString())
        val fis = context.openFileInput(filename(id))
        copyStream(fis, FileOutputStream(file))
        return file
    }

    private fun filename(id: Int): String {
        return "${keyNames}_$id"
    }

    private fun copyStream(input: InputStream, out: OutputStream) {
        input.copyTo(out)
        out.close()
        input.close()
    }
}