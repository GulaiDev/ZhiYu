package com.rdbb.zhiyu

import android.app.Application
import com.rdbb.zhiyu.core.util.BackendConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZhiYuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // core:util 是纯 JVM 模块，用它拼接静态资源地址
        BackendConfig.baseUrl = BuildConfig.BASE_URL
    }
}
