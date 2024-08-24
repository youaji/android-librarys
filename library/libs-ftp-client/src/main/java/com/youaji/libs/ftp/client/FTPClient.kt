package com.youaji.libs.ftp.client

import android.content.Context
import com.youaji.libs.ftp.client.ftp.FTPClient
import com.youaji.libs.ftp.client.ftps.FTPSClient
import com.youaji.libs.ftp.client.ftps.MemorizingTrustManager
import com.youaji.libs.ftp.client.sftp.KeyFileManager
import com.youaji.libs.ftp.client.sftp.KeyVerifier
import com.youaji.libs.ftp.client.sftp.SFTPClient
import javax.net.ssl.X509TrustManager

class FTPClient {

    class Builder(private val context: Context) {
        fun setFTP(): FTPBuilder {
            return FTPBuilder(ClientProvider.FTP, context)
        }

        fun setFTPS(): FTPSBuilder {
            return FTPSBuilder(ClientProvider.FTPS, context)
        }

        fun setSFTP(): SFTPBuilder {
            return SFTPBuilder(ClientProvider.SFTP, context)
        }
    }
}

abstract class ClientProviderBuilder(
    protected val type: ClientProvider,
    protected val context: Context
) {
    protected var server = ""
    protected var port = 21
    protected var username = "anonymous"// 默认不需要用户名与密码，即匿名登录
    protected var password = ""
    protected var startDirectory = ""
    protected var safIntegration = false

    abstract fun build(): Client

    fun setServer(server: String): ClientProviderBuilder {
        this.server = server
        return this
    }

    fun setPort(port: Int): ClientProviderBuilder {
        this.port = port
        return this
    }

    fun setUsername(username: String): ClientProviderBuilder {
        this.username = username
        return this
    }

    fun setPassword(pwd: String): ClientProviderBuilder {
        this.password = pwd
        return this
    }

    fun setStartDirectory(dir: String): ClientProviderBuilder {
        this.startDirectory = dir
        return this
    }

    fun setSAFIntegration(saf: Boolean): ClientProviderBuilder {
        this.safIntegration = saf
        return this
    }
}

class FTPBuilder(type: ClientProvider, context: Context) : ClientProviderBuilder(type, context) {
    private var utf8 = false
    private var passive = false

    fun setUTF8(utf8: Boolean): FTPBuilder {
        this.utf8 = utf8
        return this
    }

    fun setPassive(passive: Boolean): FTPBuilder {
        this.passive = passive
        return this
    }

    override fun build(): Client {
        val client = FTPClient()
        client.utf8 = utf8
        client.connect(server, port)
        client.passive = passive
        client.login(username, password)
        return client
    }
}

class FTPSBuilder(type: ClientProvider, context: Context) : ClientProviderBuilder(type, context) {
    private var utf8 = false
    private var passive = false
    private var implicit = false
    private var privateData = false
    private var trustManager: X509TrustManager = MemorizingTrustManager(context)

    fun setUTF8(utf8: Boolean): FTPSBuilder {
        this.utf8 = utf8
        return this
    }

    fun setPassive(passive: Boolean): FTPSBuilder {
        this.passive = passive
        return this
    }

    fun setImplicit(implicit: Boolean): FTPSBuilder {
        this.implicit = implicit
        return this
    }

    fun setPrivateData(privateData: Boolean): FTPSBuilder {
        this.privateData = privateData
        return this
    }

    fun setTrustManager(trustManager: X509TrustManager): FTPSBuilder {
        this.trustManager = trustManager
        return this
    }

    override fun build(): Client {
        MemorizingTrustManager(context)
        val client = FTPSClient(context, trustManager)
        client.implicit = implicit
        client.utf8 = utf8
        client.connect(server, port)
        client.passive = passive
        client.login(username, password)
        client.privateData = privateData
        return client
    }
}

class SFTPBuilder(type: ClientProvider, context: Context) : ClientProviderBuilder(type, context) {
    private var privateKey = false
    private var keyVerifier = KeyVerifier(context)
    private var keyFileManager = KeyFileManager(context)
    fun setPrivateKey(privateKey: Boolean): SFTPBuilder {
        this.privateKey = privateKey
        return this
    }

    fun setKeyVerifier(keyVerifier: KeyVerifier): SFTPBuilder {
        this.keyVerifier = keyVerifier
        return this
    }

    fun setKeyFileManager(keyFileManager: KeyFileManager): SFTPBuilder {
        this.keyFileManager = keyFileManager
        return this
    }

    override fun build(): Client {
        val client = SFTPClient(context, keyVerifier)
        client.connect(server, port)
        if (privateKey) client.loginByPrivateKey(username, keyFileManager.file(0), password)
        else client.login(username, password)
        return client
    }
}