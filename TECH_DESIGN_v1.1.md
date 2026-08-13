# HabitFlow 技术设计文档（v1.1）

> **文档名称**：HabitFlow 技术设计文档
> **版本**：v1.1 ｜ **状态**：已定案（脚手架定案版） ｜ **最后更新**：2026-08-12
> **版本基线**：Gradle 9.3.1 / AGP 9.1.0 / Kotlin 2.3.20 / Room 2.8.4 / Compose BOM 2026.05.00
> **原文档关系**：由 TECH_DESIGN.md（v1.0）结构优化而来，保留全部有效内容与核心意图；原文件冻结存档不再更新
> **关联文档**：DEVELOPMENT_PLAN_v1.1.md（本文简称 DEVELOPMENT_PLAN）；本文简称 TECH_DESIGN

## 修订记录

| 版本 | 日期 | 变更摘要 |
|---|---|---|
| v1.0 | 脚手架定案版 | 原始创建（TECH_DESIGN.md） |
| v1.1 | 2026-08-12 | ①模块计数全文统一 9→10（§3.1 实际列 10 个模块）；②§2.3 新增版本目录补全清单（KSP/Hilt/lifecycle 等，原 v1.0 缺失）；③§2.1 版本基线差异显式化（文档基线 vs 模板工程基线）；④根 build.gradle.kts 补 `kotlin.jvm` 插件；⑤:app 配置改用 AGP 9 `optimization{}` DSL（替代 isMinifyEnabled/isShrinkResources）；⑥§3.4 新增 gradle.properties 关键属性（`android.builtInKotlin=false`，KSP 与 AGP 9 内置 Kotlin 不兼容）；⑦混淆规则瘦身并补 Frequency enum 保留说明；⑧§4.2 新增 Screen 可注入约定；⑨§4.4 BaseViewModel 补全事件通道定义；⑩§4.5 新增导航与 BottomBar 集成设计；⑪§5.3/§6.1 isExcused 与 excusedDates 对齐；⑫§6.1 补 AppDatabase/TypeConverter 代码；⑬§6.2 DataStore 字段瘦身（移除无 UI 对应的 hasCompletedOnboarding/userName）；⑭§9.1 CI 补 gradlew 授权与分支说明；⑮新增 §11 模板工程迁移清单 |

## 编号稳定约定

- 章节编号：新增章节用 x.y 递进子编号或追加附录章节（§11+），**不重排既有编号**（§4.5、§11 均为此规则下新增）；
- 模块编号与 Gradle 模块名一一对应（§3.1 模块登记表是唯一事实来源）；
- 里程碑编号（M1~M4）与任务编号（DEVELOPMENT_PLAN 1.1~5.7）保持稳定，只勾选不重编号；
- 跨文档引用统一写"文件简称 + 章节号 + 一句话描述"（文件简称见头部关联文档）。

## 目录

1. 项目概述（§1） 2. 技术选型与理由（§2） 3. 模块架构设计（§3） 4. 架构分层与数据流（§4）
5. 核心功能设计（§5） 6. 数据层设计（§6） 7. 网络层设计（§7） 8. 测试策略（§8）
9. CI/CD 设计（§9） 10. 面试讲解要点速查（§10） 11. 附录：模板工程迁移清单（§11）

---

## 1. 项目概述

### 1.1 项目定位

**一句话定位**：本地优先（Local-first）的习惯打卡与数据统计工具 App，面向国内 Android 初级/中级岗位求职练习。

- 业务上：用户创建习惯（如"每天跑步 30 分钟"）、每日打卡、查看连续天数（Streak）与完成率统计；
- 工程上：它是"刻意使用主流技术栈"的练习项目——Kotlin 全量、Compose 全量、协程与 Flow、MVVM+MVI、Hilt、多模块、单元/仪器测试、CI/CD 全链路覆盖。

> **面试可陈述理由**：选择工具类 App 而非 IM/电商/资讯流，是因为它的功能面恰好覆盖主流 JD 的高频技术词（协程、Flow、Room、Hilt、Compose、测试），且业务复杂度可控、2 个月内能做出完整闭环。复杂度太低的 TodoList 撑不起架构展示，复杂度太高的 IM/电商一个人做不完，习惯打卡是两者之间性价比最高的区间。

### 1.2 与 DataCollector 的互补关系

DataCollector 已证明的能力（**这两个项目共同构成"工程能力完整"的证明**）：

| 已有能力 | 体现位置 |
|---|---|
| 协议设计（二进制拆帧、校验、粘包半包） | `GeoDspProtocol` + `TcpClientManager` |
| 并发控制（线程池、单线程串行化、锁） | `DataRepository`、`SyncAuthManager` |
| 失败恢复与幂等（重试、去重、状态机） | `ProjectSyncExecutor`、`DataSyncWorker` |
| 多数据库生命周期管理 | `AppDatabase` |

HabitFlow 要补齐的能力清单（**这是本项目存在的全部意义**）：

| # | 补齐能力 | DataCollector 现状 | HabitFlow 落地方式 |
|---|---|---|---|
| 1 | 协程与 Flow | Java 回调 + ExecutorService | 全链路 suspend + Flow，Room Flow 查询 |
| 2 | MVVM/MVI + UDF | 无统一架构，ViewModel 手写回调 | StateFlow + sealed Action/Effect 约定 |
| 3 | Kotlin 占比 | 约 90% Java | 100% Kotlin |
| 4 | Hilt 依赖注入 | 手动 new / 静态单例 | 全量 Hilt，含测试替换 |
| 5 | 单元测试 | 0 个 | JUnit + MockK + Turbine 覆盖核心逻辑 |
| 6 | 仪器测试 / UI 测试 | 1 个依赖真后端的联调 | Compose UI Test + Room 真库测试 |
| 7 | 模块化 | 单 app 模块 | 10 个 Gradle Module（6 core + 3 feature + 1 壳，见 §3.1） |
| 8 | CI/CD | 无 | GitHub Actions：PR 检查 + 主分支 Release |
| 9 | 架构分层（可讲清楚） | 包名分层，边界模糊 | 模块边界即架构边界，依赖单向 |

> **面试可陈述理由**：两个项目是刻意互补的。DataCollector 证明"协议、并发、状态机"这类硬核底层能力，HabitFlow 证明"现代 Android 工程化"能力。面试时按 JD 侧重讲其中一个：问底层原理讲 DataCollector，问架构/测试/工程化讲 HabitFlow。

### 1.3 功能范围

**MVP 核心功能（必须做，覆盖招聘考察点）**：

1. 习惯管理：创建/编辑/删除习惯（名称、频率：每日/每周 N 次、目标量、提醒文案）
2. 每日打卡：当天打勾、撤销打卡
3. 首页（Home）：今日待打卡清单 + 快捷打卡 + 连续天数展示
4. 统计页（Stats）：近 7/30 天完成率、打卡热力图（GitHub 贡献图样式）、当前最长连续
5. 设置页：深色模式切换（持久化到 DataStore，验证 Preferences DataStore 用法）

**可裁剪边界（后置，不影响架构展示）**：

| 后置功能 | 说明 |
|---|---|
| 本地通知提醒 | 需要 WorkManager/AlarmManager，涉及系统权限，放功能阶段后期或砍掉（DEVELOPMENT_PLAN 3.8） |
| 每周 N 次型习惯的完整日历视图 | 统计页只做每日型即可，周型用"本周完成次数"简单展示 |
| 多设备云同步 | 需要后端，超出 2 个月范围；网络层只留骨架 |
| 习惯分组/标签/归档 | 纯业务扩展，不影响任何架构点 |
| Widget 小组件 | 上架后加分项，不进入 MVP |

> **边界说明（v1.1）**：WEEKLY 频率字段进入 MVP 表结构（§6.1 `HabitEntity.frequency`），但"每周上限校验与统计适配"后置（DEVELOPMENT_PLAN 3.7）——MVP 支持选择频率类型，完整语义后补。

> **面试可陈述理由**：功能范围划定原则是"每个功能必须至少对应一个技术考察点，且不引入超出单人 2 个月能力的复杂度"。云同步需要自建后端，会稀释前端工程化展示；本地通知涉及系统级调度，与协程/Flow/架构展示无关，所以都后置。

---

## 2. 技术选型与理由

### 2.1 环境基线（版本组合与验证结论）

| 组件 | 版本 | 说明 |
|---|---|---|
| Gradle | 9.3.1 | 官方兼容表：AGP 9.1 最低/推荐 Gradle 即 9.3.1 |
| AGP | 9.1.0 | 官方 release notes：最低/默认 Gradle 9.3.1、JDK 17、最高支持 API 36/36.1 |
| Kotlin | 2.3.20 | K2 编译器完全稳定（Compose 编译器随 Kotlin 2.x 内置） |
| KSP | 2.3.20-2.0.4 | 与 Kotlin 2.3.20 配套（v1.1 补全，见 §2.3） |
| Hilt | 2.59.1 | 官方明确适配 AGP 9（v1.1 补全，见 §2.3） |
| Room | 2.8.4 | 官方文档当前示例版本，KSP 处理 |
| Compose BOM | 2026.05.00 | 官方 Compose 编译器文档推荐的最新 BOM |
| compileSdk / minSdk / targetSdk | 36 / 24 / 36 | minSdk 24 覆盖 Android 7.0+ |
| JVM target | 17 | AGP 9.x 默认要求 |

> **⚠️ 版本基线差异（v1.1 显式化）**：当前模板工程实际为 **Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37 / Java 11**（与本文基线不同，但两套组合各自自洽）。**不能混用**：AGP 9.1 最高支持 API 36.1，若用 AGP 9.1.0 则 compileSdk 必须 ≤36；保留 compileSdk 37 则需 AGP 9.2.x（官方对照表：AGP 9.2 ↔ Gradle 9.4.1）。迁移第 0 步二选一统一，见 §11.1。

> **面试可陈述理由**：Kotlin 2.x 起 Compose 编译器随 Kotlin 发布，不再需要单独指定 `composeOptions`；版本组合按官方兼容矩阵锁定，把"选型踩坑"风险归零，时间花在功能与测试上而不是版本兼容上。

### 2.2 逐项选型说明

| 选型 | 所在模块 | 解决什么问题 | 必须用/可后补 |
|---|---|---|---|
| Kotlin + Compose 全量 UI | 所有模块 | 现代 UI 开发范式，声明式 + 状态驱动；避免 View/Compose 混用的互操作成本 | **必须用** |
| Hilt | 所有含 Android 依赖的模块 | 依赖注入，解耦 + 可测试性 | **必须用** |
| Room | :core:data | 结构化业务数据持久化，Flow 响应式查询 | **必须用** |
| DataStore（Preferences） | :core:data | 轻量键值（主题、首次启动标志），与 Room 职责分离 | **必须用**（量很小，但能讲清两种持久化边界） |
| Retrofit + OkHttp | :core:network | 预留网络骨架（拦截器链演示点）；MVP 无真实后端，仅保留接口与拦截器 | 骨架**必做**（见 §7.1 决策），真实后端后补 |
| 协程 + Flow | 所有模块 | 异步与响应式数据流，取代回调 | **必须用** |
| Navigation Compose | :app + :feature:* | 页面导航，类型安全路由 | **必须用**（导航图组织是高频面试点，集成方案见 §4.5） |
| Version Catalog（libs.versions.toml） | 根目录 | 统一版本管理，多模块不漂移 | **必须用** |
| MMKV | — | 无使用场景：DataStore 已覆盖 | **不引入** |
| Paging 3 | — | MVP 数据量小（个人习惯 ≤ 100 条），无分页诉求 | 可后补（面试官问就答"数据量级不需要，但了解其与 Flow 的集成方式"） |

> **面试可陈述理由**：
> - **Room 而非直接 SQLite**：Room 在编译期校验 SQL 与类型，天然支持 Flow 响应式查询和迁移框架，是 JD 高频词且能体现"会用官方推荐方案"。
> - **DataStore 而非 SharedPreferences/MMKV**：DataStore 基于协程 + Flow，无阻塞主线程风险，与整体架构气质一致；Preferences 版足够覆盖主题等少量键值，不需要 Proto 版的 schema 成本。MMKV 是腾讯方案，性能好但非官方标准，国内大厂 JD 常见但面试官更认可"先讲清为什么需要，再选型"。
> - **Navigation Compose 而非单 Activity + 手写路由**：声明式导航与 Compose 生命周期天然集成，`NavBackStackEntry` 与 ViewModelStore 绑定关系是面试常考点，用它就有话讲。

### 2.3 版本目录补全清单（v1.1 新增）

原 v1.0 文档引用了 `libs.room.runtime`、`libs.hilt.android` 等依赖，但 `gradle/libs.versions.toml` 无对应条目（模板仅含基础依赖）。以下为脚手架阶段必须补全的条目（迁移时逐项核对 Google Maven 最新稳定版）：

| libs 别名 / 插件 | 建议版本 | 说明 |
|---|---|---|
| ksp（com.google.devtools.ksp） | 2.3.20-2.0.4 | 与 Kotlin 2.3.20 配套（KSP 版本前缀必须匹配 Kotlin 版本） |
| hilt / hilt-compiler（com.google.dagger） | 2.59.1 | 官方明确适配 AGP 9；Hilt 文档示例 2.57.1 亦可 |
| hilt-navigation-compose（androidx.hilt） | 1.3.0 | 与 Hilt 版本匹配，用于 `hiltViewModel()` |
| navigation-compose | 2.9.x | 类型安全路由（§4.5） |
| lifecycle-runtime-compose | 2.10.0 | `collectAsStateWithLifecycle` 宿主（§4.3 依赖，v1.0 缺失） |
| datastore-preferences | 1.1.x | §6.2 |
| retrofit / okhttp | 2.11.0 / 4.12.0 | §7 网络骨架 |
| kotlinx-coroutines-test | 1.10.x | §8.1 单测 |
| mockk / turbine | 1.13.x / 1.2.0 | §8.1 单测 |
| room-testing | 2.8.4 | 与 room 同版本（§8.2） |
| compose ui-test-junit4 / ui-test-manifest | BOM 2026.05.00 | §8.3 UI 测试 |
| androidx.test.ext junit / espresso-core | 1.2.x / 3.6.x | 以 Google Maven 最新稳定为准 |

---

## 3. 模块架构设计

### 3.1 模块划分与职责（模块登记表：唯一事实来源）

| 模块 | namespace | 职责 |
|---|---|---|
| `:app` | cn.zjl.habitflow | 壳模块：Application、MainActivity、NavGraph 装配、顶层 DI |
| `:core:model` | cn.zjl.habitflow.model | 纯业务模型：Habit、HabitRecord、StreakStats（纯 Kotlin 数据类） |
| `:core:domain` | cn.zjl.habitflow.domain | 纯逻辑：StreakCalculator、HabitValidator（零 Android 依赖，最容易被单测） |
| `:core:data` | cn.zjl.habitflow.data | Room(Entity/DAO/DB) + DataStore + Repository 实现 + 数据层 Hilt Module |
| `:core:network` | cn.zjl.habitflow.network | Retrofit/OkHttp 骨架 + ApiResponse 包装 + 拦截器链（预留） |
| `:core:designsystem` | cn.zjl.habitflow.designsystem | Theme(Color/Type/Shape/Theme.kt) + 通用组件（状态视图/按钮/卡片） |
| `:core:testing` | cn.zjl.habitflow.testing | 测试公用件：MainDispatcherRule、TestDataFactory（仅 testImplementation 暴露） |
| `:feature:home` | cn.zjl.habitflow.feature.home | 首页：今日打卡列表 + 打卡操作 + 新建/编辑习惯 |
| `:feature:stats` | cn.zjl.habitflow.feature.stats | 统计页：完成率、热力图、最长连续 |
| `:feature:settings` | cn.zjl.habitflow.feature.settings | 设置页：深色模式、关于（可后补，但建议建好占位以展示多 Feature 拆分） |

> **v1.1 修正**：模块计数由"9 个"统一为 10 个；namespace 补全（AGP 9 默认 `android.uniquePackageNames=true`，各库 namespace 必须唯一）。

### 3.2 依赖方向（单向，禁止反向）

```
:app
  ├── :feature:home
  ├── :feature:stats
  └── :feature:settings
:feature:home ─┐
:feature:stats ├──→ :core:data ──→ :core:domain ──→ :core:model
:feature:settings ┘         │         └──→ :core:model
:feature:* ──→ :core:designsystem（UI 通用件）
:core:data ──→ :core:network（数据层可调用网络骨架）
:core:testing ←── 各模块 test 源码集（testImplementation，绝不进入主依赖）
```

**约束规则（面试可讲）**：
- 依赖永远指向"更稳定的层"：model 和 domain 不允许依赖任何 Android 框架类 → 它们可以直接跑纯 JVM 单元测试；
- feature 之间不允许互相依赖 → 换掉一个 feature 不影响其他；
- :core:testing 只能被 `testImplementation`/`androidTestImplementation` 依赖，防止测试代码泄漏进生产；
- Repository 接口定义在 :core:data（或 domain），实现也在 :core:data，通过 Hilt 绑定接口 → 面试讲"依赖倒置"时有实例。

> **面试可陈述理由**：这个拆法（6 个 core + 3 个 feature + 1 个壳，共 10 个模块）是"面试可讲清"的最优规模：每个模块职责一句话能说完，依赖图在简历上能画出来；再多模块（如 :core:database 与 :core:datastore 分开）会显得为拆而拆，面试官反而会追问收益。核心原则是"按稳定度分层，按业务拆 feature"。

### 3.3 各模块 build.gradle.kts 关键配置

**根目录 `build.gradle.kts`（v1.1 修正：补 kotlin.jvm 插件声明）**：

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false        // :core:model / :core:domain 需要
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

**`:core:model` / `:core:domain`（纯 Kotlin，最简配置，面试重点讲"可测性"）**：

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(projects.core.model)   // domain 依赖 model；model 无依赖
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

**`:core:data`（Room schema 导出 + Hilt + KSP）**：

```kotlin
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
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.network)
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
    testImplementation(projects.core.testing)
}
```

**`:core:designsystem`（无业务依赖）**：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)  // Compose 编译器插件显式启用
}

android {
    namespace = "cn.zjl.habitflow.designsystem"
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
```

**`:feature:*`（以 :feature:home 为例；stats/settings 同构）**：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zjl.habitflow.feature.home"
    buildFeatures { compose = true }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.lifecycle.runtime.compose)   // collectAsStateWithLifecycle
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(projects.core.testing)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

**`:app`（壳模块，Hilt 插件 + 混淆开启；v1.1 修正为 AGP 9 DSL）**：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.zjl.habitflow"
    applicationId = "cn.zjl.habitflow"     // 与 namespace 一致（v1.1 明确）
    buildFeatures { compose = true }
    buildTypes {
        release {
            optimization { enable = true } // AGP 9 DSL：替代 isMinifyEnabled + isShrinkResources
        }
    }
    testOptions { unitTests.isIncludeAndroidResources = true }  // Robolectric 需要
}

dependencies {
    implementation(projects.feature.home)
    implementation(projects.feature.stats)
    implementation(projects.feature.settings)
    implementation(projects.core.designsystem)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
}
```

### 3.4 gradle.properties 关键属性（v1.1 新增）

```properties
# KSP 与 AGP 9 内置 Kotlin 不兼容：必须停用内置 Kotlin，并显式应用 org.jetbrains.kotlin.android 插件
android.builtInKotlin=false

# 构建性能（模板已配置）
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
```

> **风险说明**：AGP 9.0 起默认启用内置 Kotlin（官方 release notes：`android.builtInKotlin` false → true），而 KSP 官方明确"与 AGP 内置 Kotlin 不兼容"。本项目 Room + Hilt 全量依赖 KSP，**漏配此属性是脚手架第 1 周最可能的卡点**（DEVELOPMENT_PLAN §7 风险 1）。若需升级 KGP/KSP 版本，在根 build.gradle.kts 用 buildscript classpath 声明（见 AGP 9 release notes）。

### 3.5 混淆规则（v1.1 修正：瘦身 + 补 enum 保留）

`proguard-rules.pro`（:app）：

```proguard
# Room 实体反射（:core:data 的 consumer rules 已兜底大部分，此处仅显式保留业务实体）
-keep class cn.zjl.habitflow.data.entity.** { *; }
# enum 在 R8 优化下可能被移除，Room 实体字段引用需显式保留（或用 @Keep 标注 Frequency）
-keep enum cn.zjl.habitflow.data.entity.Frequency { *; }
```

> **v1.1 修正说明**：删除 v1.0 中冗余的 `-keep class dagger.hilt.** { *; }`（Hilt 自带 consumer rules，v1.0 注释自己都承认"AGP 8+/9 通常自动处理"）；补充 enum `Frequency` 保留规则（v1.0 遗漏的真实风险点）。keep 规则越少，R8 收益越大。

**关键版本要点（面试可讲）**：
- Kotlin 2.x 起 Compose 编译器随 Kotlin 版本发布，用 `org.jetbrains.kotlin.plugin.compose` 插件启用，**不再写** `composeOptions { kotlinCompilerExtensionVersion }`——这是 Kotlin 2.x 与 1.x 时代最明显的配置差异；
- Room schema 目录 `schemas/` 提交进 Git，配合 `room.schemaLocation`，之后任何版本升级都能用 diff 校验迁移正确性；
- 各 library 模块默认 `consumerProguardFiles` 可省，MVP 阶段只在 :app 开 R8 即可。

---

## 4. 架构分层与数据流

### 4.1 MVVM + MVI 的结合方式

**决策**：ViewModel 采用 **MVVM 的职责划分 + MVI 的单向数据流（UDF）约定**，但不上完整的 MVI 框架（如 Orbit、MVIKotlin）。

具体约定：
- **状态**：`data class XxxUiState(...)`，用 `MutableStateFlow` 持有，UI 只读；
- **意图**：UI 调用 ViewModel 的 `fun onXxx(...)` 方法（等价于 MVI 的 Intent），ViewModel 内部用 `_state.update { }` 变更状态；
- **事件**：一次性事件（Toast、导航）用 `Channel<XxxEvent>` 或 `SharedFlow(extraBufferCapacity=1)` 发射，UI 用 `LaunchedEffect` 收集一次；
- **副作用**：全部在 `viewModelScope` 中执行，异常在 BaseViewModel 统一兜底。

```
┌────────────┐  collectAsStateWithLifecycle  ┌────────────┐  suspend调用  ┌────────────┐  Flow查询  ┌──────────┐
│ Composable │ ←──────────────────────────── │ ViewModel  │ ────────────→ │ Repository │ ─────────→ │ Room DAO │
│            │ ── onCheckIn(habitId) ──────→ │ (StateFlow)│ ←──────────── │            │ ←───────── │          │
└────────────┘                               └────────────┘  StateFlow    └────────────┘   emit     └──────────┘
        UI 只读状态、只发意图                    单向数据流：State 向下，Event 向上（事件→UI→意图→状态）
```

> **面试可陈述理由**：MVVM 解决"UI 与业务解耦"，MVI 解决"状态变更可追踪、不可预测的 setState 散落各处"。完整 MVI 框架（Orbit 等）增加学习成本且面试官未必熟悉，手写约定版既能讲清 UDF 原理，又能展示"不盲目引框架"的取舍判断——面试官追问"为什么不用 Orbit"时这就是标准答案。

### 4.2 类命名规范（面试可整段复述）

| 层 | 命名 | 示例 | 约定 |
|---|---|---|---|
| Activity | 单 Activity | `MainActivity` | 仅承载 NavHost |
| Composable | 按功能 | `HomeScreen`、`HabitListItem` | 文件同名，一个 Screen 一个文件 |
| ViewModel | 按页面 | `HomeViewModel`、`StatsViewModel` | 构造器注入 Repository |
| 状态 | 页面级 | `HomeUiState` | 在 ViewModel 同文件内定义 |
| 事件 | 页面级 | `HomeUiEvent` | sealed interface，一次性事件 |
| Repository | 接口+实现 | `HabitRepository` / `HabitRepositoryImpl` | 接口在 :core:data，Hilt `@Binds` 绑定 |
| DataSource | 按技术 | `HabitDao`、`SettingsDataSource` | DAO 即 DataSource 实现 |
| 用例 | 按动作 | `CalculateStreakUseCase`（可选） | 纯逻辑进 :core:domain 而非用例层，避免过度设计 |

> **Screen 可注入约定（v1.1 新增，UI 测试替换依赖的前提）**：Screen 的 ViewModel 一律以构造参数传入（`HomeScreen(viewModel: HomeViewModel)`），**禁止在 Screen 内部调用 `hiltViewModel()`**；VM 的获取只在导航装配层（`NavHost` 的 composable 块内）进行。此约定使 §8.3 的 Compose UI Test 可以用 fake VM 直构（对应 DEVELOPMENT_PLAN 2.9）。

### 4.3 ViewModel 状态持有约定

- 状态一律 `StateFlow`，UI 一律 `collectAsStateWithLifecycle()`（来自 `androidx.lifecycle:lifecycle-runtime-compose`，§2.3 已补依赖）：
  - 用 `collectAsStateWithLifecycle` 而非 `collectAsState`：前者在页面进入后台时停止收集，省电且避免后台重组，这是面试高频考点（"StateFlow 与 Compose 生命周期如何配合"）；
- 一次性事件：
  ```kotlin
  private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)
  val events = _events.receiveAsFlow()   // receiveAsFlow 保证每个事件只被消费一次
  ```
  UI 侧：
  ```kotlin
  LaunchedEffect(Unit) {
      viewModel.events.collect { event -> when (event) {
          is HomeUiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
      } }
  }
  ```
- 列表类数据（打卡记录）用 `StateFlow<List<T>>` 直接持流，不包一层 `UiState` 的复杂嵌套——只有"加载中/空/错误"需要三态时才包 `sealed interface`。

### 4.4 BaseViewModel 设计（脚手架必建；v1.1 补全事件通道定义）

```kotlin
abstract class BaseViewModel : ViewModel() {

    // 事件通道统一在此定义（v1.1 修正：v1.0 示例中 _events 未定义）
    protected val _events = Channel<BaseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // 统一错误兜底：业务代码无需写 try/catch，异常经 handleError 收敛
    protected fun launchTask(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e   // 协程取消必须重新抛出，不能吞
            } catch (e: Exception) {
                onError?.invoke(e) ?: defaultErrorHandler(e)
            }
        }
    }

    private fun defaultErrorHandler(e: Exception) {
        _events.trySend(BaseEvent.ShowError(e.toUserMessage()))
        // Log.e 兜底
    }
}

sealed interface BaseEvent {
    data class ShowError(val message: String) : BaseEvent
}
```

关键点：
- `CancellationException` 必须 rethrow，否则 `viewModelScope` 取消逻辑被破坏（面试常考）；
- `trySend` 而非 `send`：Channel 满时不挂起阻塞，事件丢失由 UI 层可接受；
- 业务页面继承 `BaseViewModel`，但**不强制**全部走 `launchTask`——需要精细错误处理的场景自己写 try/catch。

> **面试可陈述理由**：BaseViewModel 只收敛"错误处理"和"协程作用域"两个横切点，不做更多抽象。太多 Base 类（BaseActivity/BaseFragment/BaseRepository）是 Java 时代的坏味道，Compose + 组合优于继承，这个设计本身就是可讲的架构取舍。

### 4.5 导航与 BottomBar 集成（v1.1 新增，落实 DEVELOPMENT_PLAN 2.5）

- 路由定义：类型安全路由对象（`@Serializable` sealed class / data object）集中在 :app 的 `navigation/Routes.kt`，feature 模块不感知路由实现；
- 装配：`:app` 的 MainActivity 内 `NavHost` 注册三个 feature 根目的地（home/stats/settings），feature 内部的二级页面（如编辑弹窗用 Dialog 而非独立目的地，MVP 阶段避免嵌套 NavGraph）；
- BottomBar 选中态同步：`navController.currentBackStackEntryAsState()` + `NavDestination.hierarchy` 判断当前目的地，与 BottomBar 三项一一对应；
- 状态作用域：`NavBackStackEntry` 与 ViewModelStore 绑定（面试考点）——BottomBar 三页各自持有独立 ViewModel，切换 Tab 不销毁；
- 状态恢复：配置变更（旋转）由 ViewModel 存活 + `rememberSaveable` 兜底；进程死亡恢复 MVP 阶段可接受重置（不引入 SavedStateHandle 复杂度，面试可讲该取舍）。

---

## 5. 核心功能设计

### 5.1 习惯 CRUD

流程：`HomeScreen` 点 FAB → 底部弹窗/独立页输入名称、频率、目标 → `HomeViewModel.onSaveHabit` → `HabitRepository.saveHabit` → `HabitDao.upsert`（`@Upsert` 或 `@Insert(onConflict = REPLACE)`）。

涉及模块：`:feature:home` → `:core:data` →（校验）`:core:domain`。

- 删除：长按列表项 → 确认对话框 → 逻辑删除（`isArchived` 字段）而非物理删除，保留统计数据完整性（面试可讲"软删除保证统计一致性"）；列表查询须带 `WHERE isArchived = 0` 过滤（v1.1 补充，统计与列表口径分离）；
- 编辑与新建共用同一数据校验：`HabitValidator.validate(name, frequency, target)` 返回 `sealed Result`，位于 :core:domain 纯逻辑层，直接可单测。

### 5.2 每日打卡与打卡状态管理

- 打卡记录表 `HabitRecord(habitId, date, completedAt, note?)`，`date` 以 **`Long`(epochDay)** 直接存储（`LocalDate.toEpochDay()`，避免时区问题）；Repository 层映射为 `LocalDate` 供 UI 使用（v1.1 统一 §5.2 与 §6.1 的存储表述）。备选方案：实体直接用 `LocalDate` 字段 + TypeConverter（代码见 §6.1），二选一即可，面试可讲两种的取舍；
- 打卡 = `INSERT` 一条当日记录；撤销 = `DELETE` 当日记录；
- 当日"是否已打卡"通过 DAO 查询：`SELECT EXISTS(SELECT 1 FROM habit_record WHERE habit_id=? AND date=? )`，返回 `Flow<Boolean>`，UI 自动刷新（Room Flow 响应式）。

> **面试可陈述理由**：用"记录表存在与否"表示打卡状态，而不是在习惯表上放 `todayChecked` 布尔位——布尔位无法支撑历史统计（热力图需要每一天的数据），这体现"表结构为查询服务"的设计意识。

### 5.3 Streak 计算规则与边界情况

`StreakCalculator`（:core:domain 纯函数，**最高优先级单测对象**；v1.1 签名补 excusedDates，与 §6.1 实体对齐）：

```kotlin
object StreakCalculator {
    // currentStreak: 从今天（或昨天，当天未打时）向前连续打卡天数
    fun currentStreak(
        records: Map<LocalDate, Boolean>,
        today: LocalDate,
        excusedDates: Set<LocalDate> = emptySet()   // 补卡/请假扩展点（后置功能，v1.1 落地）
    ): Int
    // longestStreak: 历史最长连续
    fun longestStreak(
        records: Map<LocalDate, Boolean>,
        excusedDates: Set<LocalDate> = emptySet()
    ): Int
}
```

**边界情况清单（写单测时必须覆盖，面试逐条讲）**：
1. 空数据 → 0；
2. 今天未打卡但昨天已打卡 → current 从昨天起算（业界惯例：今天的"进行中"连续不中断）；
3. 中间断一天 → current 归零重新起算；
4. 跨月/跨年连续（2026-01-31 → 2026-02-01）→ 用 `LocalDate.plusDays(1)` 判断连续性，不依赖月份/年份逻辑；
5. 补卡/请假（后置功能）→ 表结构已预留 `isExcused` 字段（§6.1），计算器支持 `excusedDates` 参数（v1.1 落地，v1.0 仅文字承诺），MVP 不实现 UI，面试可讲"扩展点"。

> **面试可陈述理由**：Streak 是这类 App 最容易写错、又最适合展示测试价值的逻辑——"今天没打是否中断"这类边界在真实产品里各 App 实现不同，我通过纯函数 + 表格化测试用例把规则钉死，任何改动都能立刻被测试发现。

### 5.4 统计图表

- 数据来源：`StatsRepository.getCompletionStats(days)` 一次查询出 `(date, isCompleted)` 列表（Room 返回 `Flow<List<Pair<LocalDate, Boolean>>>` 或直接在 SQL 里聚合）；
- 完成率：`completedDays / totalDays`（totalDays 含"未开始的未来日期"排除逻辑）；
- 热力图：每周 7 格排列，颜色深浅按"该周完成次数/7"分级——**不引入图表库**（MPAndroidChart/Vico），用 Compose Canvas 手绘约 50 行，面试展示"Compose 图形 API 能力"，同时避免引入重依赖；
- 趋势折线：同样 Canvas 手绘（后置项，时间不足可砍）。

> **面试可陈述理由**：统计图不引第三方库，用 Compose Canvas 手绘。理由：① 功能简单（热力图本质是彩色网格），引库收益为零；② 手绘是"展示 Compose 能力"的绝佳素材，面试可直接讲 Canvas 绘制流程；③ 简历上写"实现过自定义绘制组件"比"用过图表库"含金量高。

---

## 6. 数据层设计

### 6.1 Room

**实体（:core:data/entity/）**（v1.1：HabitRecordEntity 补 `isExcused` 字段）：

```kotlin
@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val frequency: Frequency,        // DAILY / WEEKLY
    val targetPerWeek: Int = 0,      // WEEKLY 时有效
    val iconRes: String?,            // 存资源名而非资源 id，避免混淆后资源 id 漂移
    val colorHex: String,            // 同上，存字符串
    val isArchived: Boolean = false,
    val createdAt: Long,
)

@Entity(tableName = "habit_record",
    foreignKeys = [ForeignKey(entity = HabitEntity::class,
        parentColumns = ["id"], childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("habitId"), Index("date")])
data class HabitRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: Long,                  // epochDay（v1.1：明确为主方案，见 §5.2）
    val completedAt: Long,           // 打卡时间戳
    val isExcused: Boolean = false,  // 补卡/请假预留（v1.1：与 §5.3 扩展点对齐）
)

// :core:data/db/AppDatabase.kt（v1.1 补全：v1.0 只提到类名）
@Database(
    entities = [HabitEntity::class, HabitRecordEntity::class],
    version = 1,
    exportSchema = true               // 禁止 fallbackToDestructiveMigration（见下文迁移策略）
)
@TypeConverters(Converters::class)   // 备选方案（LocalDate 字段）时启用
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

// :core:data/converters/Converters.kt（v1.1 补全；主方案 date 为 Long 时无需注册）
class Converters {
    @TypeConverter fun fromEpochDay(value: Long): LocalDate = LocalDate.ofEpochDay(value)
    @TypeConverter fun toEpochDay(date: LocalDate): Long = date.toEpochDay()
}
```

**DAO 要点**：
- 全部返回 `Flow`（响应式，UI 无需手动刷新）；
- 组合查询：`@Transaction` + `@Relation` 或 flatMap 组装 `HabitWithRecords`；
- 写操作用 `suspend fun`。

**数据库版本与迁移策略（正面回答 DataCollector 的短板）**：
- `version = 1` 起步，`schemas/` 目录进 Git；
- 每次 schema 变更必须 `exportSchema = true` + 生成新版本 + 编写 `Migration`，**禁止** `fallbackToDestructiveMigration`——这条规则在文档里写死，代码评审照此检查；
- 面试答案："我在第一个项目里用过 destructive fallback 被坑过，这个项目从 v1 开始就规范化 schema 导出与迁移流程"。

### 6.2 DataStore（Preferences DataStore）

- 存什么：`isDarkMode`（v1.1 瘦身：v1.0 的 `hasCompletedOnboarding`/`userName` 无对应 UI 流程，已移除）；
- 与 Room 的职责划分：**Room 存"可结构化查询的业务数据"，DataStore 存"需要即时读取的轻量偏好"**；一条数据绝不双写两处；
- 封装：`SettingsDataSource` 暴露 `val isDarkMode: Flow<Boolean>` 与 `suspend fun setDarkMode(Boolean)`；
- 初始化：`PreferenceDataStoreFactory.create` 由 DataModule 以单例提供（Context 注入一次），避免多实例重复读文件（面试可讲）。

> **面试可陈述理由**：两种持久化的边界一句话讲清——"业务数据进 Room 因为它要 SQL 查询和 Flow 响应式更新；偏好进 DataStore 因为它是键值且读频繁写极少"。面试官追问"为什么不用 SharedPreferences"：SP 的 `apply()` 异步不可观察、`commit()` 阻塞主线程，DataStore 原生 Flow 化，与状态驱动 UI 完美配合。

### 6.3 Repository 组织方式

- 接口 + 实现全部放 :core:data（MVP 不把接口放 domain 层——避免为抽象而抽象，面试可讲这个取舍）；
- Hilt 绑定（多模块下模块内 `internal` 组织，:app 只依赖 feature 与 designsystem，不直接感知数据层实现）：
  ```kotlin
  @Module @InstallIn(SingletonComponent::class)
  abstract class RepositoryModule {
      @Binds abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
  }
  ```
- `DataModule`（v1.1 补充说明）：`@Provides @Singleton fun provideDatabase(@ApplicationContext ctx): AppDatabase`、`provideDataStore(...)`、`provideHabitDao(db)`——Room/DataStore 均需 `@ApplicationContext` 注入；
- Repository 不持有 Android 依赖（Context 由 DataStore 初始化时传入一次），保证可单测。

### 6.4 本地优先（Local-first）数据流

MVP 纯本地：数据只写 Room，UI 只订阅 Room Flow。网络同步为后置扩展点：

```
Room ──emit──→ Repository(stateIn) ──→ ViewModel(StateFlow) ──→ UI
```

- Repository 用 `flow.map { }` 做实体 → 模型映射（entity 与 domain model 分离，面试可讲"数据层模型与 UI 模型解耦"）；
- 未来加云同步时，只需在 Repository 内叠加"本地写 + 队列上传"，UI 层零改动——这就是"Repository 是数据层唯一入口"设计带来的扩展性。

---

## 7. 网络层设计

### 7.1 决策：MVP 纯本地 + 网络骨架预留

**结论：MVP 不接真实后端，但 :core:network 模块必须建**（v1.1 明确：骨架为必做、接入为后补，消除 v1.0 §2.2 表"可后补"的歧义），包含：

1. `ApiResponse<T>` 统一包装（`Success`/`Error(code,msg)`，参考国内后端常见 `{code,message,data}` 结构）；
2. OkHttp 拦截器链骨架：
   - `LoggingInterceptor`（debug 下打印请求/响应）；
   - `AuthInterceptor`（从 TokenProvider 取 token 注入 `Authorization: Bearer`，**预留占位**）；
   - `AuthRefreshInterceptor`（401 时调刷新接口重放原请求，**预留占位**，注释清楚设计意图）；
3. `RetrofitClient` 工厂（OkHttpClient 超时 10s 连接/30s 读取、`HttpLoggingInterceptor` 仅 debug）；
4. `TokenStore` 接口（实现由 DataStore 完成，注入进 network 模块——体现"依赖倒置"）。

### 7.2 面试应对策略（为什么没有网络模块的质疑）

标准回答三段式：
1. **范围决策**："习惯打卡类 App 的核心价值在本地记录与统计，云同步需要自建后端与账号体系，单人 2 个月周期内会严重稀释前端工程化展示，所以 MVP 定为纯本地，这是有意的范围裁剪而非能力缺失"；
2. **能力证明**："但网络层不是没做过——我的 DataCollector 项目有完整实战：Retrofit + JWT + OkHttp 拦截器 + 401 刷新重放 + 指数退避重试 + 幂等去重，这套东西我在 HabitFlow 里以拦截器骨架的形式复用了设计"；
3. **架构证明**："网络层以模块形式存在，任何功能接入后端都无需改 UI 层，Repository 是唯一入口，这是设计时预留的扩展点"。

---

## 8. 测试策略

### 8.1 单元测试（test/ 源码集，JUnit + MockK + Turbine + coroutines-test）

**三个最高价值测试对象**：

| 对象 | 测什么 | 关键技巧 |
|---|---|---|
| `StreakCalculatorTest` | §5.3 全部边界：空数据、今天未打、中断、跨年、excused | 纯 JVM 测试，无任何 mock，**零依赖跑通**，作为 CI 第一道门 |
| `HabitViewModelTest` | 打卡成功→状态更新；打卡失败→Error 事件；onSave 校验失败→不落库 | MockK mock Repository + `MainDispatcherRule` 替换主线程 + Turbine 断言 StateFlow/事件 |
| `HabitRepositoryImplTest` | 真实 Room（见 §8.2），或 mock DAO 测映射逻辑 | 二选一：映射逻辑用 mock DAO 快测；DAO 本身进真库测试 |

**MainDispatcherRule（放 :core:testing）**：

```kotlin
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

**Turbine 断言示例（面试可直接讲）**：

```kotlin
@Test
fun `checkIn updates state`() = runTest {
    val repo = mockk<HabitRepository>()
    coEvery { repo.checkIn(any(), any()) } returns Unit
    val vm = HomeViewModel(repo)
    vm.onCheckIn(habitId = 1)
    vm.uiState.test {
        assertEquals(CheckInState.Idle, awaitItem().checkInState)
        vm.onCheckIn(habitId = 1)
        assertEquals(CheckInState.Success, awaitItem().checkInState)
    }
}
```

### 8.2 Room / DataStore 测试（test/ 内用 Robolectric 或 androidTest 真机）

**推荐方案**：Room 真库测试放 **androidTest**（`InstrumentedTestCase`，用 `ApplicationProvider.getApplicationContext()` + `Room.inMemoryDatabaseBuilder`），原因：
- 真 SQLite 行为最可靠（Robolectric 的 SQLite 是 shadow 实现，偶有差异）；
- 但 androidTest 需要模拟器/真机，CI 里跑 `connectedDebugAndroidTest` 需启动 emulator（GitHub Actions 有 `reactivecircus/android-emulator-runner`）。

**备选（CI 无模拟器时）**：Robolectric + Room 在 test 里跑，作为本地快速验证（MVP 阶段先保证 androidTest 一条路径跑通，避免双写）。

**DataStore 测试**：`PreferencesDataStore` 可在 test 中用 `TemporaryFolder` 指定文件路径（`PreferenceDataStoreFactory.create(scope, produceFile = { file })`），存→读→断言。

### 8.3 Compose UI Test（androidTest 源码集）

最小可运行配置（依赖见 §3.3 :feature:home）：

```kotlin
dependencies {
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.test.ext.junit)
    debugImplementation(libs.compose.ui.test.manifest)  // 必须 debug，因 androidx.test 规则要求
}
```

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun checkInFlow_showsSuccess() {
        composeRule.setContent {
            HabitFlowTheme { HomeScreen(viewModel = fakeVm) }  // fakeVm 前提：§4.2 Screen 可注入约定
        }
        composeRule.onNodeWithText("晨跑").assertIsDisplayed()
        composeRule.onNodeWithTag("checkin_btn_1").performClick()
        composeRule.onNodeWithText("已打卡").assertIsDisplayed()
    }
}
```

> **v1.1 说明**：UI 测试采用 fake VM 直构方案（不引入 hilt-android-testing），依赖 §4.2 的"Screen 可注入约定"；若未来需要完整 DI 链路测试，备选 `@HiltAndroidTest` + `HiltTestApplication`（需另加 hilt-android-testing 依赖）。

**test 与 androidTest 职责划分（面试必讲）**：
- `test/`：纯 JVM 逻辑（计算器、ViewModel 状态机、Repository 映射）——快、稳、CI 必跑；
- `androidTest/`：真库（Room）+ 真 UI（Compose）——慢，需模拟器，CI 用 emulator job 跑，本地用 `gradlew connectedDebugAndroidTest`。

### 8.4 依赖与目录规划（脚手架阶段最小集）

```
:core:domain/src/test/.../StreakCalculatorTest.kt      # 第一个测试，当天跑绿
:core:data/src/test/.../HabitRepositoryImplTest.kt      # MockK + Turbine
:feature:home/src/test/.../HomeViewModelTest.kt         # MockK + MainDispatcherRule
:feature:home/src/androidTest/.../HomeScreenTest.kt     # Compose UI Test（第二周内）
:core:data/src/androidTest/.../HabitDaoTest.kt          # Room 真库（第二周内）
```

---

## 9. CI/CD 设计

### 9.1 GitHub Actions（:app 所在仓库根目录 `.github/workflows/`）

**工作流一 `ci.yml`（PR 触发，必须做；v1.1：补 gradlew 授权步骤，分支名按仓库实际调整）**：

```yaml
name: CI
on:
  pull_request:
  push:
    branches: [master]   # 按仓库实际默认分支调整（常见为 main）

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Make gradlew executable
        run: chmod +x gradlew
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: ~/.gradle/caches
          key: gradle-${{ hashFiles('**/*.gradle.kts', 'gradle/libs.versions.toml') }}
      - name: Unit tests
        run: ./gradlew testDebugUnitTest
      - name: Assemble
        run: ./gradlew assembleDebug
      # lintDebug 建议第 3 周再纳入（DEVELOPMENT_PLAN 2.10），避免阻塞 CI 首绿

  instrumented:
    runs-on: ubuntu-latest
    needs: build
    strategy:
      matrix: { api-level: [30] }   # 一条即可，覆盖 Android 11
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          script: ./gradlew connectedDebugAndroidTest
```

**工作流二 `release.yml`（主分支 tag 触发，可后补但建议早期建好）**：

```yaml
name: Release
on:
  push:
    tags: ['v*']

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Make gradlew executable
        run: chmod +x gradlew
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - name: Build release
        run: ./gradlew assembleRelease
      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: habitflow-release
          path: app/build/outputs/apk/release/*.apk
```

**脚手架阶段验收：** CI 第一次全绿（PR 打上"CI passing"徽章）+ 一条 release 流水线能出 APK 产物。

> **面试可陈述理由**：CI 不只是跑通命令——`testDebugUnitTest` 在 PR 阶段拦截回归、emulator job 保证 UI 测试不烂尾，这些闸门对应"质量内建"理念；release 流水线让"发版"从手动操作变成一键产物，是简历上"从代码到交付的完整链路"的直接证据。

---

## 10. 面试讲解要点速查

| 面试官问 | 你要讲的核心 |
|---|---|
| 为什么多模块 | 稳定度分层 + 业务拆 feature，依赖单向，换功能不动其他模块 |
| 为什么 MVVM+MVI | MVVM 解耦、MVI 状态可追踪；不引框架是因为手写约定已够且面试官能看懂 |
| 为什么 collectAsStateWithLifecycle | 后台不收集、省资源，生命周期感知 |
| 为什么 Room Flow | 数据变更 UI 自动刷新，无需手动刷新机制 |
| 为什么 Streak 计算在 domain | 纯 JVM 可测，边界用例表格化 |
| 为什么 DataStore | Flow 化、非阻塞，与 Room 职责分离 |
| 为什么没后端 | 范围决策 + DataCollector 已证明网络能力 + Repository 是扩展点 |
| 测试怎么分层 | test 快稳跑 CI，androidTest 真库真 UI |
| 混淆配置 | AGP 9 `optimization{}`、Hilt/Room 规则、资源名不存 int 存 String |
| 为什么这套版本组合 | 官方兼容矩阵锁定（Kotlin 2.x 起 Compose 编译器随 Kotlin 发布） |

---

## 11. 附录：模板工程迁移清单（v1.1 新增）

> 适用现状：AS 模板工程（:app 单模块 + AppCompatActivity + Fragment + ViewBinding + XML 导航），迁移至本文 §3 目标架构。对应 DEVELOPMENT_PLAN 第 1 周前置条件（任务 1.1 前完成）。

### 11.1 版本基线决策（二选一，第 0 步）

- **选项 A（推荐）**：跟随模板工程——Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37 / Java 11→17 统一；无需降级任何组件，模板即官方当前稳定组合；同步改写 §2.1 基线表并登记修订记录；
- **选项 B**：按本文基线降级——Gradle 9.3.1 / AGP 9.1.0 / compileSdk 36；须降 wrapper 与 compileSdk。
- 无论选哪个：**Java 统一为 17**（`compileOptions` + `kotlin { jvmToolchain(17) }`），`gradle.properties` 补 `android.builtInKotlin=false`（§3.4）。

### 11.2 模板遗留清理清单

- [ ] 删除 `FirstFragment`/`SecondFragment`、`fragment_first/second.xml`、`nav_graph.xml`（XML 导航整体弃用）
- [ ] 删除 `menu_main.xml`、`content_main.xml`、`activity_main.xml`（含 FAB/AppBar 模板逻辑）
- [ ] 关闭 `viewBinding`（app/build.gradle.kts `buildFeatures`）
- [ ] `MainActivity` 重写为 Compose + `@AndroidEntryPoint`，仅承载 NavHost（§4.5）
- [ ] 评估 `keepRules/rules.keep`、`backup_rules.xml`、`data_extraction_rules.xml`、模板 dimens/colors 的去留
- [ ] 删除模板字符串资源（first/second_fragment_label 等），补应用名与文案

### 11.3 命名空间与包名迁移

- [ ] `com.example.habitflow` → `cn.zjl.habitflow`：applicationId、:app namespace、Kotlin 包路径、Manifest `.MainActivity` 引用、残留 XML 中的类全名
- [ ] 各模块 namespace 按 §3.1 登记表配置（AGP 9 默认 `android.uniquePackageNames=true`）

### 11.4 构建脚本重写

- [ ] `settings.gradle.kts`：`include` 10 个模块（§3.1）
- [ ] 根 `build.gradle.kts`：7 个插件 `apply false`（§3.3）
- [ ] `gradle/libs.versions.toml`：按 §2.3 补全版本目录（替换模板默认条目）
- [ ] `gradle.properties`：`android.builtInKotlin=false` + 保留配置缓存配置
- [ ] wrapper 版本按 §11.1 决策定版

### 11.5 主题与 Manifest

- [ ] 模板 `Theme.Material3.DayNight.NoActionBar` 保留为启动主题，Compose 内用 :core:designsystem 的 `HabitFlowTheme`（双轨：Manifest XML 主题启动 → Compose 主题接管）
- [ ] Manifest 注册 `HabitFlowApplication`（`@HiltAndroidApp`，DEVELOPMENT_PLAN 1.11）

### 11.6 迁移后自检（进入 DEVELOPMENT_PLAN 1.1 前）

- [ ] `./gradlew assembleDebug` 通过（10 模块全编译）
- [ ] 安装启动不闪退（Hilt 装配 + 空 NavHost）
- [ ] `git log` 无模板遗留文件遗漏（`git status` 干净）

### 11.7 文档拆分预案（超规模触发）

本文超过约 1200 行时：①`build-config.md`——§2 版本表 + §3.3/§3.4/§3.5 构建配置（最易变层）；②`interfaces/`——各 feature/core 接口契约（模块落定后按模块拆）；③本文保留稳定架构叙事（§1/§4/§5/§6/§7/§10）。拆分后同步更新 README 导航表。
