package com.youaji.libs.ftp.client.ftps

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Base64
import com.youaji.libs.ftp.client.R
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.withLock

@SuppressLint("CustomX509TrustManager")
class MemorizingTrustManager(
    private var context: Context
) : X509TrustManager {

    private val defaultTrustManager: X509TrustManager? = getTrustManager(null)
    private var keyStoreStorage: SharedPreferences = context.getSharedPreferences(KEYSTORE_NAME, Context.MODE_PRIVATE)
    private var appKeyStore: KeyStore = loadAppKeyStore()
    private var appTrustManager: X509TrustManager? = getTrustManager(appKeyStore)

    /**
     * Get a list of all certificate aliases stored in MTM.
     *
     * @return an [Enumeration] of all certificates
     */
    val certificates: Enumeration<String>
        get() = try {
            appKeyStore.aliases()
        } catch (e: KeyStoreException) {
            // this should never happen, however...
            throw RuntimeException(e)
        }

    /**
     * Removes the given certificate from MTMs key store.
     *
     *
     *
     * **WARNING**: this does not immediately invalidate the certificate. It is
     * well possible that (a) data is transmitted over still existing connections or
     * (b) new connections are created using TLS renegotiation, without a new cert
     * check.
     *
     *
     * @param alias the certificate's alias as returned by [.getCertificates].
     * @throws KeyStoreException if the certificate could not be deleted.
     */
    fun deleteCertificate(alias: String?) {
        appKeyStore.deleteEntry(alias)
        keyStoreUpdated()
    }

    private fun getTrustManager(ks: KeyStore?): X509TrustManager? {
        try {
            val tmf = TrustManagerFactory.getInstance("X509")
            tmf.init(ks)
            for (t in tmf.trustManagers) {
                if (t is X509TrustManager) {
                    return t
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun loadAppKeyStore(): KeyStore {
        val keyStore: KeyStore = try {
            KeyStore.getInstance(KeyStore.getDefaultType())
        } catch (e: KeyStoreException) {
            throw RuntimeException(e)
        }
        try {
            keyStore.load(null, null)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val keystore = keyStoreStorage.getString(KEYSTORE_KEY, null)
        if (keystore != null) {
            val inputStream = ByteArrayInputStream(Base64.decode(keystore, Base64.DEFAULT))
            try {
                keyStore.load(inputStream, "MTM".toCharArray())
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return keyStore
    }

    private fun storeCert(alias: String, cert: Certificate) {
        try {
            appKeyStore.setCertificateEntry(alias, cert)
        } catch (e: KeyStoreException) {
            e.printStackTrace()
            return
        }
        keyStoreUpdated()
    }

    private fun storeCert(cert: X509Certificate) {
        storeCert(cert.subjectDN.toString(), cert)
    }

    private fun keyStoreUpdated() {
        // reload appTrustManager
        appTrustManager = getTrustManager(appKeyStore)

        // store KeyStore to shared preferences
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            appKeyStore.store(byteArrayOutputStream, "MTM".toCharArray())
            byteArrayOutputStream.flush()
            byteArrayOutputStream.close()
            keyStoreStorage.edit()
                .putString(KEYSTORE_KEY, Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // if the certificate is stored in the app key store, it is considered "known"
    private fun isCertKnown(cert: X509Certificate): Boolean {
        return try {
            appKeyStore.getCertificateAlias(cert) != null
        } catch (e: KeyStoreException) {
            false
        }
    }

    private fun checkCertTrusted(chain: Array<X509Certificate>, authType: String, isServer: Boolean) {
        try {
            if (isServer) {
                appTrustManager!!.checkServerTrusted(chain, authType)
            } else {
                appTrustManager!!.checkClientTrusted(chain, authType)
            }
        } catch (ae: CertificateException) {
            // if the cert is stored in our appTrustManager, we ignore expiredness
            // TODO rethink this
            if (isExpiredException(ae) || isCertKnown(chain[0])) {
                return
            }
            try {
                if (defaultTrustManager == null) {
                    throw ae
                }
                if (isServer) {
                    defaultTrustManager.checkServerTrusted(chain, authType)
                } else {
                    defaultTrustManager.checkClientTrusted(chain, authType)
                }
            } catch (e: CertificateException) {
                interactCert(chain, e)
            }
        }
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        checkCertTrusted(chain, authType, false)
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        checkCertTrusted(chain, authType, true)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return defaultTrustManager!!.acceptedIssuers
    }

    private fun certDetails(c: X509Certificate): String {
        val validityDateFormatter = SimpleDateFormat.getInstance()
        return context.getString(
            R.string.mtm_cert_details_properties,
            c.subjectDN.toString(),
            validityDateFormatter.format(c.notBefore),
            validityDateFormatter.format(c.notAfter),
            certHash(c, "SHA-1"),
            certHash(c, "SHA-256"),
            c.issuerDN.toString(),
        )
    }

    private fun certChainMessage(chain: Array<X509Certificate>, cause: CertificateException): String {
        var e: Throwable = cause
        val stringBuilder = StringBuilder()
        for (c in chain) {
            stringBuilder.append(certDetails(c))
        }
        return if (isPathException(e)) {
            context.getString(
                R.string.mtm_trust_anchor,
                context.getString(R.string.mtm_cert_details, stringBuilder.toString())
            )
        } else if (isExpiredException(e)) {
            context.getString(
                R.string.mtm_cert_expired,
                context.getString(R.string.mtm_cert_details, stringBuilder.toString())
            )
        } else {
            // get to the cause
            while (e.cause != null) {
                e = e.cause!!
            }
            context.getString(R.string.mtm_unknown_err, e.localizedMessage, stringBuilder.toString())
        }
    }

    private fun interact(message: String): Boolean {
        var choice: Boolean? = null
        val lock = ReentrantLock()
        val condition = lock.newCondition()

        (context as Activity).runOnUiThread {
            AlertDialog.Builder(context)
                .setTitle(R.string.mtm_accept_cert)
                .setMessage(message)
                .setPositiveButton(R.string.mtm_decision_always) { _: DialogInterface?, _: Int ->
                    lock.withLock {
                        choice = true
                        condition.signal()
                    }
                }
                .setNeutralButton(R.string.mtm_decision_abort) { _: DialogInterface?, _: Int ->
                    lock.withLock {
                        choice = false
                        condition.signal()
                    }
                }
                .setOnCancelListener { _: DialogInterface? ->
                    lock.withLock {
                        choice = false
                        condition.signal()
                    }
                }
                .create().show()
        }
        try {
            lock.withLock {
                condition.await()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return choice!!
    }

    private fun interactCert(chain: Array<X509Certificate>, cause: CertificateException) {
        if (interact(certChainMessage(chain, cause))) {
            storeCert(chain[0]) // only store the server cert, not the whole chain
        } else {
            throw cause
        }
    }

    companion object {
        private const val KEYSTORE_NAME = "ftps_keystore"
        private const val KEYSTORE_KEY = "ftps_keystore"

        private fun isExpiredException(e: Throwable?): Boolean {
            var err = e
            do {
                if (err is CertificateExpiredException) {
                    return true
                }
                err = err!!.cause
            } while (err != null)
            return false
        }

        private fun isPathException(e: Throwable?): Boolean {
            var err = e
            do {
                if (err is CertPathValidatorException) {
                    return true
                }
                err = err!!.cause
            } while (err != null)
            return false
        }

        private fun hexString(data: ByteArray): String {
            val si = StringBuilder()
            for (i in data.indices) {
                si.append(String.format("%02x", data[i]))
                if (i < data.size - 1) {
                    si.append(":")
                }
            }
            return si.toString()
        }

        private fun certHash(cert: X509Certificate, digest: String): String? {
            return try {
                val md = MessageDigest.getInstance(digest)
                md.update(cert.encoded)
                hexString(md.digest())
            } catch (e: CertificateEncodingException) {
                e.message
            } catch (e: NoSuchAlgorithmException) {
                e.message
            }
        }
    }
}