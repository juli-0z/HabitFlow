// Top-level build file where you can add configuration options common to all sub-projects/modules.
// 插件版本统一由 gradle/libs.versions.toml 管理（TECH_DESIGN_v1.1 §3.3）
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false        // :core:model / :core:domain 需要
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false   // §4.5 类型安全路由
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false   // 代码规范自检（3.11）
}

// ktlint 统一应用于全部 10 个子模块（含纯 Kotlin 的 :core:model/:core:domain），
// 检查所有 *.kt 源码与测试；根级统一配置，各模块无差异化需求（3.11，§3.3）
// 注：backing-property/function-naming 豁免见 .editorconfig（3.11 实测结论）
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
