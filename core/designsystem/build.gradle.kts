// :core:designsystem — Theme(Color/Type/Shape) + 通用组件（无业务依赖）
// TECH_DESIGN_v1.1 §3.3：Compose 编译器插件显式启用
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cn.zjl.habitflow.designsystem"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
    }
    buildFeatures { compose = true }
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core) // 三态组件图标（material3 不再传递 icons-core，显式声明）
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.lifecycle.viewmodel.ktx) // §4.4 BaseViewModel（viewModelScope）
    debugImplementation(libs.compose.ui.tooling)
    // 空测试 APK 的 instrumentation 也需要 runner（2.12 全量实测）
    androidTestImplementation(libs.androidx.test.runner)
}
