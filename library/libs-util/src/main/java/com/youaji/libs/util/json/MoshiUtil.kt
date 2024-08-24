//@file:Suppress("unused")
//package com.youaji.libs.util.json
//
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//import java.lang.reflect.ParameterizedType
//import java.lang.reflect.Type
//
///**
// * 基于 moshi 的 json 转换封装
// */
//
//object MoshiUtil {
//
//    abstract class MoshiTypeReference<T> // 自定义的类，用来包装泛型
//
//    val moshi = Moshi.Builder()
//        .addLast(KotlinJsonAdapterFactory())
//        .build()!!
//
//    inline fun <reified T> toJson(src: T, indent: String = ""): String {
//        try {
//            val jsonAdapter = moshi.adapter<T>(getGenericType<T>())
//            return jsonAdapter.indent(indent).toJson(src)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return ""
//    }
//
//    @Throws(Exception::class)
//    inline fun <reified T> fromJson(jsonStr: String): T? = fromJson<T>(false, jsonStr)
//
//    @Throws(Exception::class)
//    inline fun <reified T> fromJson(isLenient: Boolean, jsonStr: String): T? {
//        var jsonAdapter = moshi.adapter<T>(getGenericType<T>())
//        if (isLenient) {
//            jsonAdapter = jsonAdapter.lenient()
//        }
//        return jsonAdapter.fromJson(jsonStr)
//
//    }
//
//    inline fun <reified T> getGenericType(): Type {
//        return object :
//            MoshiTypeReference<T>() {}::class.java
//            .genericSuperclass
//            .let { it as ParameterizedType }
//            .actualTypeArguments
//            .first()
//    }
//
//}
//
