plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.rdbb.zhiyu.core.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // 仓库的公开签名暴露了这些模块的类型（SessionManager/ApiService/领域模型），需用 api 传递
    api(project(":core:datastore"))
    api(project(":core:network"))
    api(project(":core:model"))

    // DataStore Preferences存储键值对
    implementation(libs.androidx.datastore.preferences)
    // 依赖注入
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // retrofit2.HttpException（safeCall 统一错误处理需要）
    implementation(libs.retrofit)
    // MultipartBody.Part（上传图片接口签名需要）
    implementation(libs.okhttp)
    // 错误响应体解析
    implementation(libs.kotlinx.serialization.json)
}
