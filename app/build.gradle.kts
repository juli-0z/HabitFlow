// :app 壳模块（TECH_DESIGN_v1.1 §3.3：Application、MainActivity、NavGraph 装配、顶层 DI）
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization) // §4.5 类型安全路由（@Serializable）
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// M4 5.3：release 签名配置读取（keystore.properties 含敏感密码、绝不入库；缺失时出 unsigned 包）
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

android {
    namespace = "cn.zjl.habitflow"
    compileSdk {
        version =
            release(
                libs.versions.compileSdk
                    .get()
                    .toInt(),
            ) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "cn.zjl.habitflow" // 旧 DSL（android.newDsl=false）：applicationId 在 defaultConfig 内
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "0.3.0" // M4 5.3：语义化版本号（对应 M3 功能冻结版）

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // M4 5.3：release 签名（keystore.properties 缺失时不创建签名配置，出 unsigned 包——修复 CI run 32585649482：getProperty null 崩溃）
    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (keystorePropertiesFile.exists()) signingConfigs.getByName("release") else null
            optimization { enable = true } // AGP 9 DSL：替代 isMinifyEnabled + isShrinkResources
            // 显式引用默认规则 + 项目规则（§3.5，proguard-rules.pro 前置项落地）
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
    testOptions { unitTests.isIncludeAndroidResources = true } // Robolectric 需要
}

// Java/Kotlin 目标统一 17（TECH_DESIGN_v1.1 §11.1）
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs) // 与依赖模块同步（AAR 元数据要求，2.10 实测）
    implementation(project(":feature:home"))
    implementation(project(":feature:stats"))
    implementation(project(":feature:settings"))
    implementation(project(":core:designsystem"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // M3 3.8：WorkManager + Hilt 集成（Application Configuration.Provider 需 HiltWorkerFactory，§5）
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json) // 路由序列化
    // 差异标注：§3.3 未列 activity-compose/material3/BOM，MainActivity 空壳编译必需（见交付说明）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core) // BottomBar 三 Tab 图标（material3 不传递 icons-core，§4.5）
    // XML 启动主题 Theme.Material3.DayNight.NoActionBar 需要（§11.5 双轨主题）
    implementation(libs.material)
    testImplementation(libs.junit)
    // 空测试 APK 的 instrumentation 也需要 runner（2.12 全量实测）
    androidTestImplementation(libs.androidx.test.runner)
}
