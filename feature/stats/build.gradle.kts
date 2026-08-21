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
    // androidTest 依赖（mockk -> junit-jupiter）多 jar 含 META-INF 许可证文件，通配排除（§8.3/2.9 实测，
    // 3.4 引入 stats androidTest 后补齐——缺失会导致 mergeDebugAndroidTestJavaResource 重复文件失败）
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
            )
        }
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
    implementation(project(":core:model"))          // Habit/StreakStats 类型（§3.3 feature 模板）
    implementation(project(":core:data"))
    implementation(project(":core:domain"))          // StreakCalculator 统计计算（M3 3.4）
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
    androidTestImplementation(libs.mockk.android)   // fake VM 直构（§8.3，仪器测试须用 mockk-android）
    androidTestImplementation(libs.androidx.junit)   // AndroidJUnit4（ext-junit 不传递 runner，§8.3 依赖清单）
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.compose.ui.test.manifest)
}
