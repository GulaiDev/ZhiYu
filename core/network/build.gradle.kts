import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// 后端地址配置在 local.properties 中：BASE_URL=http://host:port/（Retrofit 要求以 / 结尾）
val localProperties: Properties = Properties().run {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { load(it) }
    }
    this
}

val baseUrl: String = localProperties.getProperty("BASE_URL", "http://10.0.2.2:8000/")
    .let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "com.rdbb.zhiyu.core.network"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        // 后端地址注入 BuildConfig.BASE_URL
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true // 开启 BuildConfig 类，用于在运行时访问构建配置
    }
}

dependencies {
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 网络请求
    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
}
