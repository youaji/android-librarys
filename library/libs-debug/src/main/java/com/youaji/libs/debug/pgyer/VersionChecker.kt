package com.youaji.libs.debug.pgyer

import com.youaji.libs.util.appVersionName
import com.youaji.libs.util.logger.logDebug
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.regex.Pattern

/**
 * 蒲公英版本检查
 *
 * [参考来源](https://github.com/PGYER/AppUpdateChecker/blob/main/Android/UpdateChecker.java)
 *
 * 参考来源 describe Update Checker
 *
 * 参考来源 date 2023-06-06
 *
 * @since 2024/6/20
 * @param apiKey 蒲公英 apiKey [获取地址](https://www.pgyer.com/account/api)
 */
class VersionChecker(private val apiKey: String = "8128a0e422039621c5c002ab478a83fe") {
    private val apiAppCheck = "https://www.pgyer.com/apiv2/app/check"

    /**
     * 检测App是否有更新
     *
     * @param appKey            appKey 获取方式：App管理页面中找到appKey，也可以在某个API返回的结果中获取
     * @param buildVersion      (选填)使用 App 本身的 Build 版本号，Android 对应字段为 versionName, iOS 对应字段为 version
     * @param buildBuildVersion (选填)使用蒲公英生成的自增 Build 版本号
     * @param channelKey        (选填)渠道KEY
     * @param callback          结果回调
     * @see [https://www.pgyer.com/doc/view/api.appUpdate](https://www.pgyer.com/doc/view/api.appUpdate)
     */
    fun check(
        appKey: String? = null,
        buildVersion: String? = appVersionName,
        buildBuildVersion: Int? = null,
        channelKey: String? = null,
        callback: Callback,
    ) {
        val data: MutableMap<String, String> = HashMap()
        data["_api_key"] = apiKey
        data["appKey"] = appKey ?: ""
        data["buildVersion"] = buildVersion ?: ""
        data["buildBuildVersion"] = buildBuildVersion.toString()
        data["channelKey"] = channelKey ?: ""
        logDebug("=== version checker request ===\n$data")

        Http.post(apiAppCheck, data, object : Http.Callback {
            override fun response(response: String): Boolean {
                val dataMap = parseResponse(response)
                if (dataMap == null) {
                    callback.error("response no data")
                    return false
                }
                if (dataMap.containsKey("message")) {
                    callback.error(dataMap["message"] ?: "empty message!")
                    return false
                }

                val updateInfo = UpdateInfo()
                updateInfo.buildBuildVersion = dataMap["buildBuildVersion"]?.toInt() ?: 0
                updateInfo.forceUpdateVersion = dataMap["forceUpdateVersion"] ?: ""
                updateInfo.forceUpdateVersionNo = dataMap["forceUpdateVersionNo"] ?: ""
                updateInfo.needForceUpdate = dataMap["needForceUpdate"] == "true"
                updateInfo.downloadURL = dataMap["downloadURL"] ?: ""
                updateInfo.buildHaveNewVersion = dataMap["buildHaveNewVersion"] == "true"
                updateInfo.buildVersionNo = dataMap["buildVersionNo"] ?: ""
                updateInfo.buildVersion = dataMap["buildVersion"] ?: ""
                updateInfo.buildShortcutUrl = if (dataMap["buildShortcutUrl"] == null) "" else dataMap["buildShortcutUrl"]!!
                updateInfo.buildUpdateDescription = dataMap["buildUpdateDescription"] ?: ""
                logDebug("=== version checker response ===\n$dataMap")
                callback.result(updateInfo)
                return true
            }

            override fun error(message: String?) {
                callback.error(message ?: "empty message!")
            }
        })
    }

    private fun parseResponse(response: String): Map<String, String?>? {
        val responseMap: MutableMap<String, String?> = HashMap()
        val responseRegexp = "^\\{\"code\":(.*),\"message\":\"(.*?)\".*\\}$"
        val responsePattern = Pattern.compile(responseRegexp)
        val responseMatcher = responsePattern.matcher(response)
        if (responseMatcher.find()) {
            responseMap["code"] = responseMatcher.group(1)
            responseMap["message"] = responseMatcher.group(2)
        } else {
            return null
        }
        val responseDataRegexp = "^\\{\"code\":.*,\"message\":\".*\",\"data\":(.*)\\}$"
        val responseDataPattern = Pattern.compile(responseDataRegexp)
        val responseDataMatcher = responseDataPattern.matcher(response)
        val data =
            if (responseDataMatcher.find()) {
                responseDataMatcher.group(1) ?: ""
            } else {
                return responseMap
            }
        val dataMap: MutableMap<String, String?> = HashMap()
        val dataRegexp = "\"(.*?)\":(\".*?\"|true|false)"
        val dataPattern = Pattern.compile(dataRegexp)
        val dataMatcher = dataPattern.matcher(data)
        while (dataMatcher.find()) {
            val key = dataMatcher.group(1) ?: ""
            val value = dataMatcher.group(2)
            if (value == "true" || value == "false") {
                dataMap[key] = value
            } else {
                dataMap[key] = value?.substring(1, value.length - 1)
            }
        }
        return dataMap
    }

    class UpdateInfo {
        /**
         * 蒲公英生成的用于区分历史版本的build号
         */
        var buildBuildVersion = 0

        /**
         * 强制更新版本号（未设置强置更新默认为空）
         */
        var forceUpdateVersion: String = ""

        /**
         * 强制更新的版本编号
         */
        var forceUpdateVersionNo: String = ""

        /**
         * 是否强制更新
         */
        var needForceUpdate = false

        /**
         * 应用安装地址
         */
        var downloadURL: String = ""

        /**
         * 是否有新版本
         */
        var buildHaveNewVersion = false

        /**
         * 上传包的版本编号，默认为1 (即编译的版本号，一般来说，编译一次会变动一次这个版本号, 在 Android 上叫 Version Code。对于 iOS 来说，是字符串类型；对于 Android 来说是一个整数。例如：1001，28等。)
         */
        var buildVersionNo: String = ""

        /**
         * 版本号, 默认为1.0 (是应用向用户宣传时候用到的标识，例如：1.1、8.2.1等。)
         */
        var buildVersion: String = ""

        /**
         * 应用短链接
         */
        var buildShortcutUrl = ""

        /**
         * 应用更新说明
         */
        var buildUpdateDescription: String = ""
    }

    interface Callback {
        fun result(updateInfo: UpdateInfo)
        fun error(message: String)
    }

    private object Http {
        private const val TIMEOUT = 3000
        private val BOUNDARY = UUID.randomUUID().toString()
        operator fun get(url: String, query: Map<String, String>?, callback: Callback) {
            request(url, "GET", query, null, callback)
        }

        fun post(url: String, data: Map<String, String>?, callback: Callback) {
            request(url, "POST", null, data, callback)
        }

        fun request(url: String, method: String, query: Map<String, String>?, data: Map<String, String>?, callback: Callback) {
            Thread {
                try {
                    var queryString = ""
                    if (method == "GET" && query != null) {
                        queryString = makeQueryString(query)
                    }
                    val connection = URL("$url?$queryString").openConnection() as HttpURLConnection
                    connection.setRequestMethod(method)
                    connection.setUseCaches(false)
                    connection.setConnectTimeout(TIMEOUT)
                    connection.setReadTimeout(TIMEOUT)
                    if (method == "POST" && data != null) {
                        connection.setDoInput(true)
                        connection.setDoOutput(true)
                        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
                        connection.setRequestProperty("Accept", "application/json")
                        val outputStream = connection.outputStream
                        outputStream.write(makeFormData(data).toByteArray())
                        outputStream.close()
                    }
                    connection.connect()
                    val responseCode = connection.getResponseCode()
                    if (responseCode == 200) {
                        var response = readResponse(connection)
                        response = unicodeDecode(response)
                        callback.response(response)
                    } else {
                        callback.error("Status Code $responseCode")
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    callback.error(e.message)
                }
            }.start()
        }

        private fun makeQueryString(query: Map<String, String>): String {
            val list = mutableListOf<String?>()
            for (key in query.keys) {
                list.add(key + "=" + query[key])
            }
            return java.lang.String.join("&", list)
        }

        private fun makeFormData(data: Map<String, String>): String {
            val formData = StringBuilder()
            for (key in data.keys) {
                formData.append(String.format("--%s\r\n", BOUNDARY))
                formData.append(String.format("Content-Disposition: form-data; name=\"%s\"\r\n", key))
                formData.append("\r\n")
                formData.append(String.format("%s\r\n", data[key]))
            }
            formData.append(String.format("--%s--\r\n", BOUNDARY))
            return formData.toString()
        }

        private fun readResponse(connection: HttpURLConnection): String {
            try {
                val inputStream = connection.inputStream
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(256)
                var readLength: Int
                while (inputStream.read(buffer).also { readLength = it } > 0) {
                    outputStream.write(buffer, 0, readLength)
                }
                val response = String(outputStream.toByteArray(), StandardCharsets.UTF_8)
                inputStream.close()
                outputStream.close()
                return response
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        private fun unicodeDecode(str: String): String {
            var string = str
            val pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))")
            val matcher = pattern.matcher(string)
            var ch: Char
            while (matcher.find()) {
                ch = (matcher.group(2) ?: "").toInt(16).toChar()
                string = string.replace(matcher.group(1) ?: "", ch.toString() + "")
            }
            return string
        }

        interface Callback {
            fun response(response: String): Boolean
            fun error(message: String?)
        }
    }
}