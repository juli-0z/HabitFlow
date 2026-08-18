// :app 壳模块（TECH_DESIGN_v1.1 §3.3：Application、MainActivity、NavGraph 装配、顶层 DI）
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)   // §4.5 类型安全路由（@Serializable）
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.zjl.habitflow"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "cn.zjl.habitflow"   // 旧 DSL（android.newDsl=false）：applicationId 在 defaultConfig 内
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization { enable = true } // AGP 9 DSL：替代 isMinifyEnabled + isShrinkResources
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // 消费方须与依赖模块同步启用 desugaring（:feature:home/:core:data 已启用，2.10 AAR 元数据检查）
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
    testOptions { unitTests.isIncludeAndroidResources = true }  // Robolectric 需要
}

// Java/Kotlin 目标统一 17（TECH_DESIGN_v1.1 §11.1）
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // 与依赖模块同步（AAR 元数据要求，2.10 实测）
    implementation(project(":feature:home"))
    implementation(project(":feature:stats"))
    implementation(project(":feature:settings"))
    implementation(project(":core:designsystem"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)   // 路由序列化
    // 差异标注：§3.3 未列 activity-compose/material3/BOM，MainActivity 空壳编译必需（见交付说明）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)   // BottomBar 三 Tab 图标（material3 不传递 icons-core，§4.5）
    // XML 启动主题 Theme.Material3.DayNight.NoActionBar 需要（§11.5 双轨主题）
    implementation(libs.material)
    testImplementation(libs.junit)
}
