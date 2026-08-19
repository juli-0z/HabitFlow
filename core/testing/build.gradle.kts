// :core:testing — 测试公用件（仅 testImplementation/androidTestImplementation 暴露，绝不进入主依赖）
// TECH_DESIGN_v1.1 §3.1/§3.2
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "cn.zjl.habitflow.testing"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // TestDataFactory 用 java.time（minSdk 24）需要 desugaring（2.10 lint 实测）
        isCoreLibraryDesugaringEnabled = true
    }
}

// Java/Kotlin 目标统一 17（TECH_DESIGN_v1.1 §11.1）
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // TestDataFactory 用 LocalDate（2.10 lint 实测）
    implementation(project(":core:model"))   // TestDataFactory 生成 model 测试数据（§3.2 单向：testing -> model）
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    // 空测试 APK 的 instrumentation 也需要 runner（2.12 全量实测）
    androidTestImplementation(libs.androidx.test.runner)
}
