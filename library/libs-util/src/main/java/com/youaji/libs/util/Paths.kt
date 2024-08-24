@file:Suppress("unused")
package com.youaji.libs.util

import android.content.Context
import android.os.Environment

/**
 * 获取应用缓存路径（优先外存）
 */
inline val cacheDirPath: String get() = application.cacheDirPath

inline val Context.cacheDirPath: String
    get() = if (isExternalStorageWritable || !isExternalStorageRemovable) {
        externalCacheDirPath.orEmpty()
    } else {
        internalCacheDirPath
    }

/**
 * 获取外存应用缓存路径
 */
inline val externalCacheDirPath: String? get() = application.externalCacheDirPath

inline val Context.externalCacheDirPath: String? get() = externalCacheDir?.absolutePath

/**
 * 获取外存应用文件路径
 */
inline val externalFilesDirPath: String? get() = application.externalFilesDirPath

inline val Context.externalFilesDirPath: String? get() = getExternalFilesDir(null)?.absolutePath

/**
 * 获取外存应用图片路径
 */
inline val externalPicturesDirPath: String? get() = application.externalPicturesDirPath

inline val Context.externalPicturesDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath

/**
 * 获取外存应用视频路径
 */
inline val externalMoviesDirPath: String? get() = application.externalMoviesDirPath

inline val Context.externalMoviesDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath

/**
 * 获取外存应用下载路径
 */
inline val externalDownloadsDirPath: String? get() = application.externalDownloadsDirPath

inline val Context.externalDownloadsDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath

/**
 * 获取外存应用文档路径
 */
inline val externalDocumentsDirPath: String? get() = application.externalDocumentsDirPath

inline val Context.externalDocumentsDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_DOCUMENTS)?.absolutePath

/**
 * 获取外存应用音乐路径
 */
inline val externalMusicDirPath: String? get() = application.externalMusicDirPath

inline val Context.externalMusicDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath

/**
 * 获取外存应用播客路径
 */
inline val externalPodcastsDirPath: String? get() = application.externalPodcastsDirPath

inline val Context.externalPodcastsDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_PODCASTS)?.absolutePath

/**
 * 获取外存应用铃声路径
 */
inline val externalRingtonesDirPath: String? get() = application.externalRingtonesDirPath

inline val Context.externalRingtonesDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_RINGTONES)?.absolutePath

/**
 * 获取外存应用闹铃路径
 */
inline val externalAlarmsDirPath: String? get() = application.externalAlarmsDirPath

inline val Context.externalAlarmsDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_ALARMS)?.absolutePath

/**
 * 获取外存应用通知路径
 */
inline val externalNotificationsDirPath: String? get() = application.externalNotificationsDirPath

inline val Context.externalNotificationsDirPath: String?
    get() = getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS)?.absolutePath

/**
 * 获取内存应用缓存路径
 */
inline val internalCacheDirPath: String get() = application.internalCacheDirPath

inline val Context.internalCacheDirPath: String get() = cacheDir.absolutePath

/**
 * 获取内存应用文件路径
 */
inline val internalFileDirPath: String get() = application.internalFileDirPath

inline val Context.internalFileDirPath: String get() = filesDir.absolutePath

/**
 * 获取内存应用图片路径
 */
inline val internalPicturesDirPath: String? get() = application.internalPicturesDirPath

inline val Context.internalPicturesDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_PICTURES)?.absolutePath

/**
 * 获取内存应用视频路径
 */
inline val internalMoviesDirPath: String? get() = application.internalMoviesDirPath

inline val Context.internalMoviesDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_MOVIES)?.absolutePath

/**
 * 获取内存应用下载路径
 */
inline val internalDownloadsDirPath: String? get() = application.internalDownloadsDirPath

inline val Context.internalDownloadsDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_DOWNLOADS)?.absolutePath

/**
 * 获取内存应用文档路径
 */
inline val internalDocumentsDirPath: String? get() = application.internalDocumentsDirPath

inline val Context.internalDocumentsDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_DOCUMENTS)?.absolutePath

/**
 * 获取内存应用音乐路径
 */
inline val internalMusicDirPath: String? get() = application.internalMusicDirPath

inline val Context.internalMusicDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_MUSIC)?.absolutePath

/**
 * 获取内存应用播客路径
 */
inline val internalPodcastsDirPath: String? get() = application.internalPodcastsDirPath

inline val Context.internalPodcastsDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_PODCASTS)?.absolutePath

/**
 * 获取内存应用铃声路径
 */
inline val internalRingtonesDirPath: String? get() = application.internalRingtonesDirPath

inline val Context.internalRingtonesDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_RINGTONES)?.absolutePath

/**
 * 获取内存应用闹铃路径
 */
inline val internalAlarmsDirPath: String? get() = application.internalAlarmsDirPath

inline val Context.internalAlarmsDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_ALARMS)?.absolutePath

/**
 * 获取内存应用通知路径
 */
inline val internalNotificationsDirPath: String? get() = application.internalNotificationsDirPath

inline val Context.internalNotificationsDirPath: String?
    get() = getFileStreamPath(Environment.DIRECTORY_NOTIFICATIONS)?.absolutePath

/**
 * 判断外存是否可读写
 */
inline val isExternalStorageWritable: Boolean
    get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

/**
 * 判断外存是否可读
 */
inline val isExternalStorageReadable: Boolean
    get() = Environment.getExternalStorageState() in setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)

/**
 * 判断外存是否可移除
 */
inline val isExternalStorageRemovable: Boolean
    get() = Environment.isExternalStorageRemovable()
