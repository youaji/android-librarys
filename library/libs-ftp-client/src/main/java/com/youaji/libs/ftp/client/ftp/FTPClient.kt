package com.youaji.libs.ftp.client.ftp

import com.youaji.libs.ftp.client.Client
import com.youaji.libs.ftp.client.File
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPCmd
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream

class FTPClient : Client {

    private val client: FTPClient = FTPClient().apply {
        autodetectUTF8 = true
    }

    override fun connect(host: String, port: Int) {
        client.connect(host, port)
    }

    override var implicit: Boolean = false
    override var utf8: Boolean = false
        set(value) {
            if (value) client.controlEncoding = "UTF-8"
            field = value
        }
    override var passive: Boolean = false
        set(value) {
            if (value) client.enterLocalPassiveMode()
            else client.enterLocalActiveMode()
            field = value
        }

    private var supportsMlsCommands = false

    override fun login(user: String, password: String) {
        client.login(user, password)
        client.setFileType(FTP.BINARY_FILE_TYPE)
        supportsMlsCommands = client.hasFeature(FTPCmd.MLST)
    }

    override fun loginByPrivateKey(user: String, key: java.io.File, passphrase: String) {
        throw NotImplementedError("FTP does not support private keys")
    }

    override val isConnected: Boolean
        get() = client.isConnected

    override var privateData: Boolean = false

    override fun upload(name: String, stream: InputStream): Boolean {
        return client.storeFile(name, stream)
    }

    override fun download(name: String, stream: OutputStream): Boolean {
        return client.retrieveFile(name, stream)
    }

    override fun mkdir(path: String): Boolean {
        return client.makeDirectory(path)
    }

    override fun rm(path: String): Boolean {
        return client.deleteFile(path)
    }

    override fun rmDir(path: String): Boolean {
        return client.removeDirectory(path)
    }

    override fun rename(old: String, new: String): Boolean {
        return client.rename(old, new)
    }

    override fun list(): List<File> {
        return convertFiles(if (supportsMlsCommands) client.mlistDir() else client.listFiles())
    }

    override fun list(path: String?): List<File> {
        return convertFiles(if (supportsMlsCommands) client.mlistDir(path) else client.listFiles(path))
    }

    override fun file(path: String): File {
        if (!supportsMlsCommands) {
            // TODO improve this
            throw IllegalStateException("server does not support MLST command")
        }
        return FTPFile(client.mlistFile(path))
    }

    override fun exit(): Boolean {
        if (!client.logout()) {
            return false
        }
        client.disconnect()
        return true
    }

    companion object {
        internal fun convertFiles(files: Array<FTPFile>): List<File> {
            val result = ArrayList<File>()
            files.forEach {
                result.add(FTPFile(it))
            }
            return result
        }
    }
}
