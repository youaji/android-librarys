@file:Suppress("unused")
package com.youaji.libs.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * json格式化字符串（打印用）
 */
fun String.formatJson(): String {
    if (isAppDebug) {
        var formatStr = ""
        try {
            if (this.startsWith("{")) {
                val jsonObject = JSONObject(this)
                formatStr = jsonObject.toString(2)
            } else if (this.startsWith("[")) {
                val jsonArray = JSONArray(this)
                formatStr = jsonArray.toString(2)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formatStr
    } else {
        return this
    }
}

/**
 * 实体类格式化字符串（打印用）
 * entity(
 *     key1=value1
 *     key2=value2
 * )
 */
fun String.formatEntity(t0: String = "", isFirstLineTab: Boolean = true): String {
    val t____1 = "\t$t0"
    val firstLineTab = if (isFirstLineTab) t0 else ""
    return if (isAppDebug) {
        val str = StringBuilder()
        // 左圆括号下标
        val openParenthesisIndex = this.indexOfFirst { c -> c == '(' }
        if (openParenthesisIndex >= 0 && this.last() == ')') {
            // 类名
            val className = this.subSequence(0, openParenthesisIndex)
            // 圆括号内容
            val parenthesesStr = this.subSequence(openParenthesisIndex + 1, this.lastIndex)
            // 逗号分隔的内容
            val commaStrList = parenthesesStr.split(", ")

            str.append("$firstLineTab$className(\n")
            if (commaStrList.isNotEmpty()) {
                commaStrList.forEach { commaStr ->
//                    if (commaStr.last() == ']') {
//                        // 左边方括号下标
//                        val openSquareBracketsIndex = commaStr.indexOfFirst { c -> c == '[' }
//                        // 方括号类名
//                        val squareBracketsClassName = this.subSequence(0, openSquareBracketsIndex)
//                        // 方括号内容
//                        val squareBracketsStr = commaStr.subSequence(openSquareBracketsIndex + 1, this.lastIndex)
//                        if (squareBracketsStr.isEmpty()) {
//                            str.append("$t____1${squareBracketsClassName}=[\n")
//                            str.append("${squareBracketsStr.toString().formatEntity(t____1)}\n")
//                            str.append("$t____1]\n")
//                        } else {
//                            str.append("$t____1${commaStr}\n")
//                        }
//                    } else {
                    str.append("$t____1${commaStr}\n")
//                    }
                }
            } else {
                str.append("$t____1$commaStrList\n")
            }
            str.append("$t0)")
        } else {
            str.append("$firstLineTab$this")
        }
        str.toString()
    } else {
        this
    }
}

/**
 * map 格式化字符串（打印用）
 * {
 *     key1=value1
 *     key2=value2
 *     keyBean=
 *         entity(
 *             key1=value1
 *             key2=value2
 *         )
 *     keyMap={
 *         key1=value1
 *         key2=value2
 *     }
 * }
 */
fun Map<*, *>.format(t0: String = ""): String {
    @Suppress("UNDERSCORES")
    val t____1 = "\t$t0"
    val t________2 = "\t\t$t0"
    return if (isAppDebug) {
        val str = StringBuilder()
        str.append("{")
        this.forEach {
            when (it.value) {
                is Byte, Short, Int, Long, Float, Double, String -> {
                    str.append("\n$t____1$it")
                }
                is List<*> -> {
                    val value = it.value as List<*>
                    str.append("\n$t____1${it.key}=[\n")
                    value.forEach { info ->
                        str.append("${info.toString().formatEntity(t________2)}\n")
                    }
                    str.append("$t____1]")
                }
                is Map<*, *> -> {
                    val value = it.value as Map<*, *>
                    str.append("\n$t____1${it.key}=")
                    str.append(value.format(t____1))
                }
                else -> {
                    val value = it.value.toString()
                    if (value.isEmpty()) {
                        str.append("\n$t____1$it")
                    } else if (value.last() == ')') {
                        // bean
                        str.append("\n$t____1${it.key}=\n")
                        str.append(value.formatEntity(t________2))
                    } else {
                        str.append("\n$t____1$it")
                    }
                }
            }
        }
        str.append("\n$t0}")
        str.toString()
    } else {
        this.toString()
    }
}