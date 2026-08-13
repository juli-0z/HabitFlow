// :core:network — Retrofit/OkHttp 骨架 + ApiResponse 包装 + 拦截器链（预留）
// TECH_DESIGN_v1.1 §3.1/§3.3
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // §3.3 network 模板未列 Hilt，但 §2.2 约定"Hilt 用于所有含 Android 依赖的模块"（构建实测补齐）
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zjl.habitflow.network"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures {
        buildConfig = true   // LoggingInterceptor/RetrofitClient 需要 BuildConfig.DEBUG
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Java/Kotlin 目标统一 17（TECH_DESIGN_v1.1 §11.1）
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)   // HttpLoggingInterceptor（仅 debug，§7.1）
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
