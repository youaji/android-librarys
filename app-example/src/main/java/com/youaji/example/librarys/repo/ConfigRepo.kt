package com.youaji.example.librarys.repo

import android.os.Parcelable
import com.tencent.mmkv.MMKV
import com.youaji.libs.util.mmkv.MMKVOwner
import com.youaji.libs.util.mmkv.mmkvParcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

object ConfigRepo : MMKVOwner {

    override val kv: MMKV = MMKV.mmkvWithID("DebugConfigRepo")

    var lastTcpClientConfig by mmkvParcelable(TCPLastClientConfig())

    @Parcelize
    data class TCPLastClientConfig(val ip: String = "", val port: String = "") : Parcelable, Serializable


}