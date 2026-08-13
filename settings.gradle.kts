pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HabitFlow"
// 10 个 Gradle 模块（TECH_DESIGN_v1.1 §3.1 模块登记表）
include(":app")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:designsystem")
include(":core:testing")
include(":feature:home")
include(":feature:stats")
include(":feature:settings")
 