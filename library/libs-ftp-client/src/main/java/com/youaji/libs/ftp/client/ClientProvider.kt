package com.youaji.libs.ftp.client

enum class ClientProvider {
    FTP,
    FTPS,
    SFTP;

//    fun get(context: Context): Client {
//        return when (this) {
//            FTP -> FTPClient()
//            FTPS -> FTPSClient(context)
//            SFTP -> SFTPClient(context)
//        }
//    }
}