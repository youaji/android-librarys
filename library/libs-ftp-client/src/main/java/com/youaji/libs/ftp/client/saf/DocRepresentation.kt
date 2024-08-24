package com.youaji.libs.ftp.client.saf

import com.youaji.libs.ftp.client.Connection
import com.youaji.libs.ftp.client.File

internal data class DocRepresentation(val connId: Int, val name: String) {
    override fun toString(): String {
        return "$connId?$name"
    }

    fun child(ch: String): DocRepresentation {
        return DocRepresentation(connId, File.joinPaths(name, ch))
    }

    companion object {
        fun parse(r: String): DocRepresentation {
            val p = r.split("?", limit = 2)
            return DocRepresentation(p[0].toInt(), if (p.size == 2) p[1] else "")
        }

        fun fromConn(c: Connection): DocRepresentation {
            return DocRepresentation(c.id, "")
        }
    }
}
