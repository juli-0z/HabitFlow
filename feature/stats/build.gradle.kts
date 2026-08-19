// :feature:stats — 统计页：完成率、热力图、最长连续
// TECH_DESIGN_v1.1 §3.3 feature 模板
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zjl.habitflow.feature.stats"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // 消费 desugaring 库（:core:data）需同步开启（AAR 元数据检查，2.12 实测）
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
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // 与 :core:data 同步（AAR 元数据要求，2.12 实测）
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    // §3.3 feature 模板未列 compose 依赖，feature 直接写 Compose UI 需要（构建实测补齐）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
