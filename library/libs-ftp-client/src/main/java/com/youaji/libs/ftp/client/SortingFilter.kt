package com.youaji.libs.ftp.client

class SortingFilter(
    private var method: Method = Method.NAME,
    private var descending: Boolean = false,
    private var showHidden: Boolean = true
) {
    enum class Method {
        NAME,
        TIMESTAMP,
        SIZE,
        SERVER,
    }

    private fun sort(files: List<File>): List<File> {
        return if (descending) {
            when (method) {
                Method.NAME -> files.sortedByDescending { it.name }
                Method.TIMESTAMP -> files.sortedByDescending { it.timestamp }
                Method.SIZE -> files.sortedByDescending { it.size }
                Method.SERVER -> files.reversed()
            }
        } else {
            when (method) {
                Method.NAME -> files.sortedBy { it.name }
                Method.TIMESTAMP -> files.sortedBy { it.timestamp }
                Method.SIZE -> files.sortedBy { it.size }
                Method.SERVER -> files
            }
        }
    }

    private fun filter(files: List<File>): List<File> {
        return files.filter { showHidden || !it.name.startsWith(".") }
    }

    fun sortFilter(files: List<File>): List<File> {
        return sort(filter(files))
    }
}