package com.rdbb.zhiyu.core.util

/**
 * 后端根地址：core:util 是纯 JVM 模块，拿不到 BuildConfig，
 * 由 app 启动时用 BuildConfig.BASE_URL 赋值，供 absoluteUrl() 拼接静态资源地址。
 */
object BackendConfig {
    @Volatile
    var baseUrl: String = "http://192.168.5.10:8000/"

    fun base(): String = baseUrl.trimEnd('/')
}
