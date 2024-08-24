package com.youaji.libs.ftp.client

data class Connection(
    val type: ClientProvider,
    val title: String,
    val server: String,
    val port: Int,
    val username: String,
    val password: String,
    val startDirectory: String,
    val safIntegration: Boolean,

    // SFTP
    val privateKey: Boolean,
    // FTP FTPS
    val utf8: Boolean,
    // FTP FTPS
    val passive: Boolean,
    // FTPS
    val implicit: Boolean,
    // FTPS
    val privateData: Boolean,
    val id: Int = 0

) {
//    fun client(context: Context): Client {
//        val client = type.get(context)
//
//        client.implicit = implicit
//        client.utf8 = utf8
//        client.connect(server, port)
//        client.passive = passive
//        if (privateKey) {
//            client.loginByPrivateKey(username, KeyFileManager.fromContext(context).file(id), password)
//        } else {
//            client.login(username, password) // connect to server and login with login credentials
//        }
//        client.privateData = privateData
//
//        return client
//    }
}