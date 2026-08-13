// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 插件版本统一由 gradle/libs.versions.toml 管理（TECH_DESIGN_v1.1 §3.3）
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false        // :core:model / :core:domain 需要
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
