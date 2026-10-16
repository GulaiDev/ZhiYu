package com.rdbb.zhiyu.core.util

import com.rdbb.zhiyu.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** 相对路径 /static/... 拼接后端 host；完整 http(s) 地址原样返回。 */
fun String.absoluteUrl(): String {
    if (isBlank()) return this
    if (startsWith("http://") || startsWith("https://")) return this
    val host = BuildConfig.BASE_URL.trimEnd('/')
    return host + if (startsWith("/")) this else "/$this"
}

private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("Asia/Shanghai")
}

/** ISO 时间转“刚刚 / x分钟前 / x小时前 / 昨天 / MM-dd”。 */
fun timeAgo(iso: String?): String {
    iso ?: return ""
    return try {
        val raw = iso.substringBefore('.').replace("Z", "")
        val date = isoFormat.parse(raw) ?: return ""
        val diff = Date().time - date.time
        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour
        when {
            diff < minute -> "刚刚"
            diff < hour -> "${diff / minute}分钟前"
            diff < day -> "${diff / hour}小时前"
            diff < 2 * day -> "昨天"
            diff < 7 * day -> "${diff / day}天前"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        ""
    }
}

/** 1200 -> 1.2k，10000 -> 1w。 */
fun compactCount(n: Int): String = when {
    n >= 10_000 -> "${"%.1f".format(Locale.getDefault(), n / 10_000.0).trimEnd('0').trimEnd('.')}w"
    n >= 1_000 -> "${"%.1f".format(Locale.getDefault(), n / 1_000.0).trimEnd('0').trimEnd('.')}k"
    else -> n.toString()
}

/** 取昵称首字符用于占位头像。 */
fun firstChar(name: String?): String =
    name?.trimStart()?.takeIf { it.isNotEmpty() }?.substring(0, 1) ?: "?"
