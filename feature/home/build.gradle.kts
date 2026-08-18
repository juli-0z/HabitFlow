// :feature:home — 首页：今日打卡列表 + 打卡操作 + 新建/编辑习惯
// TECH_DESIGN_v1.1 §3.3 feature 模板
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zjl.habitflow.feature.home"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { compose = true }
    // androidTest 依赖（mockk -> junit-jupiter）多 jar 含 META-INF 许可证文件，通配排除（2.9 实测）
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
            )
        }
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
    implementation(project(":core:model"))          // feature 直接使用 model 类型（UiState/HabitListItem）
    implementation(project(":core:domain"))         // HabitValidator 校验（§5.1，§3.3 模板缺口补齐）
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    // §3.3 feature 模板未列 compose 依赖，feature 直接写 Compose UI 需要（构建实测补齐）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)   // FAB 图标（material3 不传递 icons-core）
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
    androidTestImplementation(libs.mockk.android)   // fake VM 直构（§8.3；仪器测试须用 mockk-android，2.9 实测）
    // 2.8 实测教训：ext-junit 不传递 runner，需显式声明（§8.3 依赖清单）
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.compose.ui.test.manifest)
}
