@file:Suppress("unused", "UNUSED_CHANGED_VALUE")

package com.youaji.libs.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.youaji.libs.util.logger.logError
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.DAY_OF_YEAR
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit.YEARS
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.math.abs

val systemZoneId: ZoneId by object : ReadOnlyProperty<Any?, ZoneId> {
    private lateinit var zoneId: ZoneId

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getValue(thisRef: Any?, property: KProperty<*>): ZoneId {
        if (!::zoneId.isInitialized) {
            zoneId = ZoneId.systemDefault()
            application.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_TIMEZONE_CHANGED) {
                        TimeZone.setDefault(null)
                        zoneId = ZoneId.systemDefault()
                    }
                }
            }, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
        }
        return zoneId
    }
}

/**
 * Instant 转字符串
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Instant.format(pattern: String, zone: ZoneId = systemZoneId, locale: Locale? = null): String =
    dateTimeFormatterOf(pattern, locale).withZone(zone).format(this)

/**
 * LocalDateTime 转字符串
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.format(pattern: String, locale: Locale? = null): String =
    dateTimeFormatterOf(pattern, locale).format(this)

/**
 * LocalDate 转字符串
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.format(pattern: String, locale: Locale? = null): String =
    dateTimeFormatterOf(pattern, locale).format(this)

/**
 * LocalDateTime 转 Instant
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toInstant(zone: ZoneId = systemZoneId): Instant =
    atZone(zone).toInstant()

/**
 * Instant 转 LocalDateTime
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Instant.toLocalDateTime(zone: ZoneId = systemZoneId): LocalDateTime =
    LocalDateTime.ofInstant(this, zone)

/**
 * 	LocalDateTime 转秒数
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toEpochSecond(zone: ZoneId = systemZoneId): Long =
    atZone(zone).toEpochSecond()

/**
 * LocalDateTime 转毫秒
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.toEpochMilli(zone: ZoneId = systemZoneId): Long =
    toEpochSecond(zone) * 1000 + toLocalTime().nano / 1000000

/**
 * 字符串转 Instant
 */
@RequiresApi(Build.VERSION_CODES.O)
fun String.toInstant(pattern: String, zone: ZoneId = systemZoneId): Instant =
    ZonedDateTime.parse(this, DateTimeFormatter.ofPattern(pattern).withZone(zone)).toInstant()

/**
 * 字符串转 LocalDateTime
 */
@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDateTime(pattern: String): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))

/**
 * 字符串转 LocalDate
 */
@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDate(pattern: String): LocalDate =
    LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))

/**
 * 字符串转毫秒
 */
@RequiresApi(Build.VERSION_CODES.O)
fun String.toEpochMilliseconds(pattern: String, zone: ZoneId = systemZoneId): Long =
    toInstant(pattern, zone).toEpochMilli()

/**
 * 字符串转秒数
 */
@RequiresApi(Build.VERSION_CODES.O)
fun String.toEpochSeconds(pattern: String, zone: ZoneId = systemZoneId): Long =
    toInstant(pattern, zone).epochSecond

/**
 * 判断是不是今天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.isToday(zone: ZoneId = systemZoneId): Boolean = toLocalDate().isToday(zone)

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.isToday(zone: ZoneId = systemZoneId): Boolean = this == LocalDate.now(zone)

/**
 * 判断是不是昨天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.isYesterday(zone: ZoneId = systemZoneId): Boolean = toLocalDate().isYesterday(zone)

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.isYesterday(zone: ZoneId = systemZoneId): Boolean =
    this == LocalDate.now(zone).minus(1, ChronoUnit.DAYS)

/**
 * 今年的第一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstDayOfYear(): LocalDateTime = with(TemporalAdjusters.firstDayOfYear())

/**
 * 今年的最后一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.lastDayOfYear(): LocalDateTime = with(TemporalAdjusters.lastDayOfYear())

/**
 * 明年的第一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstDayOfNextYear(): LocalDateTime = with(TemporalAdjusters.firstDayOfNextYear())

/**
 * 去年的第一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstDayOfLastYear(): LocalDateTime = with { it.with(DAY_OF_YEAR, 1).minus(1, YEARS) }

/**
 * 这个月的第一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstDayOfMonth(): LocalDateTime = with(TemporalAdjusters.firstDayOfMonth())

/**
 * 这个月的最后一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.lastDayOfMonth(): LocalDateTime = with(TemporalAdjusters.lastDayOfMonth())

/**
 * 下个月的第一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstDayOfNextMonth(): LocalDateTime = with(TemporalAdjusters.firstDayOfNextMonth())

/**
 * 上个月的第一天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstDayOfLastMonth(): LocalDateTime = with { it.with(DAY_OF_MONTH, 1).minus(1, MONTHS) }

/**
 * 这个月的第一个周几
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.firstInMonth(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.firstInMonth(dayOfWeek))

/**
 * 这个月的最后一个周几
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.lastInMonth(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.lastInMonth(dayOfWeek))

/**
 * 这个月的第几个周几
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.dayOfWeekInMonth(ordinal: Int, dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek))

/**
 * 下一个周几，不包含今天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.next(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.next(dayOfWeek))

/**
 * 下一个周几，包含今天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.nextOrSame(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.nextOrSame(dayOfWeek))

/**
 * 上一个周几，不包含今天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.previous(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.previous(dayOfWeek))

/**
 * 上一个周几，包含今天
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.previousOrSame(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.previousOrSame(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstDayOfYear(): LocalDate = with(TemporalAdjusters.firstDayOfYear())

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.lastDayOfYear(): LocalDate = with(TemporalAdjusters.lastDayOfYear())

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstDayOfNextYear(): LocalDate = with(TemporalAdjusters.firstDayOfNextYear())

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstDayOfLastYear(): LocalDate = with { it.with(DAY_OF_YEAR, 1).minus(1, YEARS) }

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstDayOfMonth(): LocalDate = with(TemporalAdjusters.firstDayOfMonth())

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.lastDayOfMonth(): LocalDate = with(TemporalAdjusters.lastDayOfMonth())

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstDayOfNextMonth(): LocalDate = with(TemporalAdjusters.firstDayOfNextMonth())

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstDayOfLastMonth(): LocalDate = with { it.with(DAY_OF_MONTH, 1).minus(1, MONTHS) }

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.firstInMonth(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.firstInMonth(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.lastInMonth(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.lastInMonth(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.dayOfWeekInMonth(ordinal: Int, dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.next(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.next(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.nextOrSame(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.nextOrSame(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.previous(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.previous(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.previousOrSame(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.previousOrSame(dayOfWeek))

/**
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun dateTimeFormatterOf(pattern: String, locale: Locale?): DateTimeFormatter =
    if (locale != null) {
        DateTimeFormatter.ofPattern(pattern, locale)
    } else {
        DateTimeFormatter.ofPattern(pattern)
    }


fun Long.toDate(pattern: String = "yyyy-MM-dd"): String = format(pattern)

fun Long.toDateTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): String = format(pattern)

@SuppressLint("SimpleDateFormat")
fun Long.format(pattern: String, locale: Locale? = null): String {
    val date = Date(this)
    val format = if (locale == null) {
        SimpleDateFormat(pattern)
    } else {
        SimpleDateFormat(pattern, locale)
    }
    format.timeZone
    return format.format(date)
}

@SuppressLint("SimpleDateFormat")
fun String.parse(pattern: String, locale: Locale? = null): Long =
    try {
        val format = if (locale == null) {
            SimpleDateFormat(pattern)
        } else {
            SimpleDateFormat(pattern, locale)
        }
        format.parse(this)?.time ?: Long.MAX_VALUE
    } catch (e: Exception) {
        e.printStackTrace()
        logError("日期解析异常：$e")
        Long.MAX_VALUE
    }

fun String.formatCorgiDatetime(formatPattern: String = "yyyy年MM月dd日 HH:dd", pattern: String = pattern_yyyyMMdd_hhmmss()): String = this.parseDate(pattern).formatDate(formatPattern)

fun String.formatDate(formatPattern: String, pattern: String = pattern_yyyyMMdd_hhmmss()): String = this.parseDate(pattern).formatDate(formatPattern)

fun String.parseDate(pattern: String = pattern_yyyyMMdd()): Long = this.parse(pattern)

fun Long.formatDate(pattern: String = pattern_yyyyMMdd()): String = this.format(pattern)

/**
 * 默认：yy/MM/dd
 * 23/01/01
 */
fun pattern_yyyyMMdd(space: String = "/") = "yyyy${space}MM${space}dd"

/**
 * 默认：yyyy/MM/dd
 * 2023/01/01
 */
fun pattern_yyMMdd(space: String = "/") = "yy${space}MM${space}dd"

/**
 * 默认：yyyy/MM/dd hh:mm
 * 2023/01/01 01:30
 */
fun pattern_yyyyMMdd_hhmm(dateSpace: String = "/", timeSpace: String = ":") = "yyyy${dateSpace}MM${dateSpace}dd hh${timeSpace}mm"

/**
 * 默认：yyyy/MM/dd HH:mm
 * 2023/01/01 13:30
 */
fun pattern_yyyyMMdd_HHmm(dateSpace: String = "/", timeSpace: String = ":") = "yyyy${dateSpace}MM${dateSpace}dd HH${timeSpace}mm"

/**
 * 默认：yyyy/MM/dd hh:mm:ss
 * 2023/01/01 01:30:00
 */
fun pattern_yyyyMMdd_hhmmss(dateSpace: String = "/", timeSpace: String = ":") = "yyyy${dateSpace}MM${dateSpace}dd hh${timeSpace}mm${timeSpace}ss"

/**
 * 默认：yyyy/MM/dd HH:mm:ss
 * 2023/01/01 13:30:00
 */
fun pattern_yyyyMMdd_HHmmss(dateSpace: String = "/", timeSpace: String = ":") = "yyyy${dateSpace}MM${dateSpace}dd HH${timeSpace}mm${timeSpace}ss"

/**
 * 默认：yyyy/MM/dd hh:mm a
 * 2023/01/01 01:30 AM
 */
fun pattern_yyyyMMdd_hhmm_a(dateSpace: String = "/", timeSpace: String = ":") = "yyyy${dateSpace}MM${dateSpace}dd hh${timeSpace}mm a"

/** 字符串转时间戳 */
fun String.toTimestamp(): Long {
    if (this.isEmpty()) return 0
    var timestamp = 0L
    try {
        timestamp = this.toLong()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
    }
    return timestamp
}

/** 判断是否是时间戳 */
fun String.isTimestamp(): Boolean {
    if (this.isEmpty()) return false
    var timestamp = 0L
    try {
        timestamp = this.toLong()
    } catch (e: NumberFormatException) {
        e.printStackTrace()
    }
    return timestamp > 0
}

/** 多少年前 */
fun yearsAgo(year: Int = 18): Long {
    val currDate = Calendar.getInstance()
    currDate.add(Calendar.YEAR, -year)
    return currDate.timeInMillis
}

/** 生日日期转年龄，默认 18 */
@SuppressLint("SimpleDateFormat")
fun String?.toAge(default: String = "18"): String {
    var age = default
    this?.let {
        try {
            val date = SimpleDateFormat("yyyy/MM/dd").parse(it)
            val calendar = Calendar.getInstance()
            date?.let { birthdate ->
                if (!calendar.before(birthdate)) {
                    val yearNow: Int = calendar.get(Calendar.YEAR) //当前年份
                    val monthNow: Int = calendar.get(Calendar.MONTH) //当前月份
                    val dayOfMonthNow: Int = calendar.get(Calendar.DAY_OF_MONTH) //当前日期
                    calendar.time = birthdate
                    val yearBirth: Int = calendar.get(Calendar.YEAR)
                    val monthBirth: Int = calendar.get(Calendar.MONTH)
                    val dayOfMonthBirth: Int = calendar.get(Calendar.DAY_OF_MONTH)
                    var ageInt = yearNow - yearBirth //计算整岁数
                    if (monthNow <= monthBirth) {
                        if (monthNow == monthBirth) {
                            if (dayOfMonthNow < dayOfMonthBirth) {
                                ageInt--
                            }//当前日期在生日之前，年龄减一
                        } else {
                            ageInt-- //当前月份在生日之前，年龄减一
                        }
                    }
                    age = ageInt.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logError("转换年龄异常：${e.message}")
        }
    }
    return age
}

fun Long.formatDate(): String {
    val targetCalendar = Calendar.getInstance()
    targetCalendar.timeInMillis = this

    val todayCalendar = Calendar.getInstance()
    var formatDate = ""
    formatDate += if (targetCalendar.get(Calendar.YEAR) == todayCalendar[Calendar.YEAR]) {
        when (targetCalendar.get(Calendar.DAY_OF_YEAR) - todayCalendar[Calendar.DAY_OF_YEAR]) {
            0 -> "今天 "
            1 -> "昨天 "
            2 -> "前天 "
            else -> (targetCalendar.get(Calendar.MONTH) + 1).toString() + "月" + targetCalendar.get(Calendar.DAY_OF_MONTH) + "日 "
        }
    } else {
        targetCalendar.get(Calendar.YEAR).toString() + "年" + (targetCalendar.get(Calendar.MONTH) + 1) + "月" + targetCalendar.get(Calendar.DAY_OF_MONTH) + "日 "
    }

//    val dateTime: Int = targetCalendar.get(Calendar.HOUR_OF_DAY)
//    formatDate += if (dateTime <= 5) {
//        "凌晨"
//    } else if (dateTime <= 12) {
//        "上午"
//    } else {
//        "下午"
//    }

    val hourOfDay: String = if (targetCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
        "0" + targetCalendar.get(Calendar.HOUR_OF_DAY)
    } else {
        targetCalendar.get(Calendar.HOUR_OF_DAY).toString()
    }

    val minute: String = if (targetCalendar.get(Calendar.MINUTE) < 10) {
        "0" + targetCalendar.get(Calendar.MINUTE)
    } else {
        targetCalendar.get(Calendar.MINUTE).toString()
    }

    formatDate += "$hourOfDay:$minute"

    return formatDate
}

fun Long.toTimeDiffDesc(default: String = "3天"): String {
    var backTime = default
    if (this > 10000) {
        var tc = abs(Date().time - this)
        tc /= 1000
        if (tc <= 0) {
            backTime = "不久"
        } else if (tc <= 60) {
            backTime = "${tc}秒"
        } else if (tc <= 3600) {
            backTime = "${tc / 60}分钟"
        } else if (tc <= 86400) {
            backTime = "${tc / 3600}小时"
        } else if (tc <= 259200) {
            backTime = "${tc / 86400}天"
        }
    }
    return backTime
}