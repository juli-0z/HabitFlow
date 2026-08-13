// :core:model — 纯业务模型（纯 Kotlin，零依赖，TECH_DESIGN_v1.1 §3.1/§3.3）
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}
