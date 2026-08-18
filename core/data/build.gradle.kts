// :core:data — Room(Entity/DAO/DB) + DataStore + Repository 实现 + 数据层 Hilt Module
// TECH_DESIGN_v1.1 §3.3：Room schema 导出 + Hilt + KSP
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.zjl.habitflow.data"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    // Room schema 导出：schema 文件进版本库，迁移时 diff 用
    ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // minSdk 24 使用 java.time（LocalDate）需要 core library desugaring（2.10 lint 实测）
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
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // java.time 脱糖（minSdk 24，§5.2 LocalDate）
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.retrofit)          // 网络骨架
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)  // 真库测试
    testImplementation(project(":core:testing"))
    // §8.2/§8.4：Room 真库测试在 androidTest（inMemory + ApplicationProvider）
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)   // AndroidJUnitRunner（ext-junit 不传递，2.8 实测补）
}
