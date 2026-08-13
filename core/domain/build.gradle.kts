// :core:domain — 纯逻辑层（零 Android 依赖，最容易被单测，TECH_DESIGN_v1.1 §3.1/§3.3）
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:model"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
