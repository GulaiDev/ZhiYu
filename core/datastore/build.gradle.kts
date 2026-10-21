plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.rdbb.zhiyu.core.datastore"
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
    implementation(project(":core:model"))
    // 依赖注入
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // DataStore Preferences存储键值对
    implementation(libs.androidx.datastore.preferences)
    // 用户信息缓存序列化
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}