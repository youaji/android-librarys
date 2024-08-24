package com.youaji.libs.tcp.client.sdk

import javax.net.ssl.KeyManager
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

/**
 * @author youaji
 * @since 2024/01/11
 */
class YouSocketSSLConfig private constructor() {
    /** 安全协议名称(缺省为 SSL) */
    var protocol: String? = null
        private set

    /** 信任证书管理器(缺省为 X509) */
    var trustManagers: Array<TrustManager>? = null
        private set

    /** 证书秘钥管理器(缺省为 null)  */
    var keyManagers: Array<KeyManager>? = null
        private set

    /** 自定义 SSLFactory(缺省为 null) */
    var customSSLFactory: SSLSocketFactory? = null
        private set

    class Builder {
        private val socketSSLConfig = YouSocketSSLConfig()

        fun setProtocol(protocol: String?): Builder {
            socketSSLConfig.protocol = protocol
            return this
        }

        fun setTrustManagers(trustManagers: Array<TrustManager>): Builder {
            socketSSLConfig.trustManagers = trustManagers
            return this
        }

        fun setKeyManagers(keyManagers: Array<KeyManager>): Builder {
            socketSSLConfig.keyManagers = keyManagers
            return this
        }

        fun setCustomSSLFactory(factory: SSLSocketFactory): Builder {
            socketSSLConfig.customSSLFactory = factory
            return this
        }

        fun build(): YouSocketSSLConfig {
            return socketSSLConfig
        }
    }
}