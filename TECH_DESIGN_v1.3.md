# HabitFlow 技术设计文档（v1.3）

> **文档名称**：HabitFlow 技术设计文档
> **版本**：v1.3 ｜ **状态**：当前生效 ｜ **最后更新**：2026-08-21
> **版本基线**：Gradle 9.4.1 / AGP 9.2.1 / Kotlin 2.3.20 / KSP 2.3.11 / Hilt 2.59.1 / Room 2.8.4 / Compose BOM 2026.05.00 / kotlinx-serialization-json 1.11.0 / desugar_jdk_libs 2.1.4 / compileSdk 37 / minSdk 24 / targetSdk 36 / Java 17（模板基线，2026-08-13 定版）
> **原文档关系**：v1.3 替代 v1.2 成为现行版本；TECH_DESIGN.md（v1.0）与 TECH_DESIGN_v1.1.md / v1.2 冻结存档，不再更新
> **关联文档**：DEVELOPMENT_PLAN_v1.3.md（本文简称 DEVELOPMENT_PLAN）；本文简称 TECH_DESIGN

## 修订记录

| 版本 | 日期 | 变更摘要 |
|---|---|---|
| v1.0 | 脚手架定案版 | 原始创建（TECH_DESIGN.md） |
| v1.1 | 2026-08-12 | 结构优化：模块计数统一 10、§2.3 版本目录补全、§3.4 内置 Kotlin 说明、§4.5 导航集成、§11 迁移清单等（详见 v1.1 修订记录） |
| v1.2 | 2026-08-15 | 落实《编码与文档一致性审计》清单 A~O（构建实测回写）：A §2.1 基线定版模板基线；B §3.4 补 newDsl=false 与 r8.gradual.support；C §3.3 :app 模板（applicationId 位置 + compose 依赖）；D §3.3 feature 模板补 compose 依赖；E §3.3 network 模板补 Hilt/KSP + buildConfig；F §2.3 KSP 2.3.11（松散绑定）+ 补 icons-core/serialization-json；G README 同步；H §3.2 补 testing→model 边；I §7.1 补 buildConfig 前提；J §4.5 补类型安全路由依赖；K §3.5 proguard 状态注明；L §5.1/§5.3 validator 规则与 excused 语义回写；M README 维护约定增"差异登记"机制；N §3.3 声明模板为示意；O PLAN 验收标准补充（另文） |
| v1.3 | 2026-08-21 | 吸收 v1.2 以来全部待回写差异（差异登记机制第 2 次合并回写）：①§9.1 CI 修复链六项实测回写（chmod +x gradlew / runner @v2（v3 不存在）/ api-level 34 / script 预热 / UTP 禁用 / KVM 权限）与 CI 优化两项（concurrency、ci-debug/**），ci.yml/release.yml 模板更新为工程实测现行版；②§3.4 补 UTP 禁用第四标志；③§3.5 proguard-rules.pro 已落地（2026-08-19，commit 9614e3b）；④§2.1/§2.3 补 desugar_jdk_libs 2.1.4 与测试依赖（androidx-junit 1.1.5 / androidx-test-runner 1.5.2 / mockk-android）；⑤§3.3 各模块模板补 core library desugaring（6 个 android 模块全量开启，含 AAR 消费方同步规则）、androidTest runner 显式声明、mockk-android、packaging META-INF 通配排除；⑥§4.4 补 launchTask 双函数参数 trailing lambda 陷阱说明；⑦§8.2/§8.3 补 androidTest 依赖实测教训（runner 显式声明、mockk-android）；⑧§11.8 待办表更新（proguard 已办，登记 M3 新观察项） |

## 编号稳定约定

- 章节编号：新增章节用 x.y 递进子编号或追加附录章节（§11+），**不重排既有编号**；
- 模块编号与 Gradle 模块名一一对应（§3.1 模块登记表是唯一事实来源）；
- 里程碑编号（M1~M4）与任务编号（DEVELOPMENT_PLAN 1.1~5.7）保持稳定，只勾选不重编号；
- 跨文档引用统一写"文件简称 + 章节号 + 一句话描述"。

## 目录

1. 项目概述（§1） 2. 技术选型与理由（§2） 3. 模块架构设计（§3） 4. 架构分层与数据流（§4）
5. 核心功能设计（§5） 6. 数据层设计（§6） 7. 网络层设计（§7） 8. 测试策略（§8）
9. CI/CD 设计（§9） 10. 面试讲解要点速查（§10） 11. 附录（§11）

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
| 8 | CI/CD | 无 | GitHub Actions：PR 检查 + 主分支 Release（v1.3：CI/Release 双链路实测全绿） |
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

> **边界说明**：WEEKLY 频率字段进入 MVP 表结构（§6.1 `HabitEntity.frequency`），但"每周上限校验与统计适配"后置（DEVELOPMENT_PLAN 3.7）——MVP 支持选择频率类型，完整语义后补。

> **面试可陈述理由**：功能范围划定原则是"每个功能必须至少对应一个技术考察点，且不引入超出单人 2 个月能力的复杂度"。云同步需要自建后端，会稀释前端工程化展示；本地通知涉及系统级调度，与协程/Flow/架构展示无关，所以都后置。

---

## 2. 技术选型与理由

### 2.1 环境基线（已定版：模板基线，2026-08-13 决策）

| 组件 | 版本 | 说明 |
|---|---|---|
| Gradle | 9.4.1 | 官方兼容表：AGP 9.2 最低/推荐 Gradle 即 9.4.1 |
| AGP | 9.2.1 | 与 AS 模板一致；需 `android.builtInKotlin=false` + `android.newDsl=false`（§3.4） |
| Kotlin | 2.3.20 | K2 编译器完全稳定（Compose 编译器随 Kotlin 2.x 内置） |
| KSP | 2.3.11 | **KSP 2.3.x 与 Kotlin 松散绑定，无 Kotlin 前缀版本号**（实测修正：`2.3.20-2.0.4` 不存在） |
| Hilt | 2.59.1 | 官方明确适配 AGP 9 |
| Room | 2.8.4 | 官方文档当前示例版本，KSP 处理 |
| Compose BOM | 2026.05.00 | 官方 Compose 编译器文档推荐 |
| kotlinx-serialization-json | 1.11.0 | §4.5 类型安全路由依赖 |
| desugar_jdk_libs | 2.1.4 | **core library desugaring（v1.3 补入）**：minSdk 24 使用 java.time（§5.2 LocalDate）必需；6 个 android 模块全量开启（§3.3） |
| compileSdk / minSdk / targetSdk | 37 / 24 / 36 | minSdk 24 覆盖 Android 7.0+ |
| JVM target | 17 | AGP 9 默认要求；Java 与 Kotlin 目标统一 17（§11.1） |

> **面试可陈述理由**：Kotlin 2.x 起 Compose 编译器随 Kotlin 发布，不再需要单独指定 `composeOptions`；版本组合按官方兼容矩阵锁定，把"选型踩坑"风险归零。desugaring 是 minSdk 24 + java.time 的标准解法（面试可讲"低版本兼容的官方路径"）。

### 2.2 逐项选型说明

| 选型 | 所在模块 | 解决什么问题 | 必须用/可后补 |
|---|---|---|---|
| Kotlin + Compose 全量 UI | 所有模块 | 现代 UI 开发范式，声明式 + 状态驱动；避免 View/Compose 混用的互操作成本 | **必须用** |
| Hilt | 所有含 Android 依赖的模块 | 依赖注入，解耦 + 可测试性 | **必须用**（含 :core:network，见 §3.3） |
| Room | :core:data | 结构化业务数据持久化，Flow 响应式查询 | **必须用** |
| DataStore（Preferences） | :core:data | 轻量键值（主题、首次启动标志），与 Room 职责分离 | **必须用** |
| Retrofit + OkHttp | :core:network | 预留网络骨架（拦截器链演示点）；MVP 无真实后端，仅保留接口与拦截器 | 骨架**必做**（见 §7.1），真实后端后补 |
| 协程 + Flow | 所有模块 | 异步与响应式数据流，取代回调 | **必须用** |
| Navigation Compose | :app + :feature:* | 页面导航，类型安全路由 | **必须用**（集成方案见 §4.5） |
| Version Catalog（libs.versions.toml） | 根目录 | 统一版本管理，多模块不漂移 | **必须用** |
| kotlinx-serialization | :app | §4.5 类型安全路由的 @Serializable 前提 | **必须用** |
| core library desugaring | 全部 android 模块 | minSdk 24 使用 java.time（§5.2） | **必须用**（v1.3 明确） |
| MMKV | — | 无使用场景：DataStore 已覆盖 | **不引入** |
| Paging 3 | — | MVP 数据量小（个人习惯 ≤ 100 条），无分页诉求 | 可后补 |

> **面试可陈述理由**：
> - **Room 而非直接 SQLite**：Room 在编译期校验 SQL 与类型，天然支持 Flow 响应式查询和迁移框架，是 JD 高频词且能体现"会用官方推荐方案"。
> - **DataStore 而非 SharedPreferences/MMKV**：DataStore 基于协程 + Flow，无阻塞主线程风险，与整体架构气质一致；Preferences 版足够覆盖主题等少量键值，不需要 Proto 版的 schema 成本。
> - **Navigation Compose 而非单 Activity + 手写路由**：声明式导航与 Compose 生命周期天然集成，`NavBackStackEntry` 与 ViewModelStore 绑定关系是面试常考点，用它就有话讲。

### 2.3 版本目录清单（以工程 `gradle/libs.versions.toml` 为唯一事实来源）

> **v1.3 说明**：在 v1.2 基础上补入 desugar-jdk-libs（2.1.4）、androidx-test-ext-junit（1.1.5）、androidx-test-runner（1.5.2）、mockk-android（与 mockk 同 1.13.13）。

| libs 别名 / 插件 | 版本 | 说明 |
|---|---|---|
| ksp（com.google.devtools.ksp） | 2.3.11 | **KSP 2.3.x 与 Kotlin 2.3.x 松散绑定（无 Kotlin 前缀）** |
| hilt / hilt-compiler（com.google.dagger） | 2.59.1 | 官方明确适配 AGP 9 |
| hilt-navigation-compose（androidx.hilt） | 1.3.0 | 与 Hilt 版本匹配，用于 `hiltViewModel()` |
| navigation-compose | 2.9.0 | 类型安全路由（§4.5） |
| lifecycle-runtime-compose | 2.10.0 | `collectAsStateWithLifecycle` 宿主（§4.3） |
| datastore-preferences | 1.1.7 | §6.2 |
| retrofit / okhttp / okhttp-logging | 2.11.0 / 4.12.0 / 4.12.0 | §7 网络骨架 |
| kotlinx-serialization-json | 1.11.0 | §4.5 类型安全路由 |
| desugar-jdk-libs | 2.1.4 | core library desugaring（v1.3 补入，§3.3） |
| compose material-icons-core | BOM 2026.05.00 | **material3 1.4+ 不再传递 icons-core，需显式声明** |
| compose ui / ui-test-junit4 / ui-test-manifest 等 | BOM 2026.05.00 | §8.3 UI 测试 |
| kotlinx-coroutines-test / mockk / mockk-android / turbine | 1.10.2 / 1.13.13 / 1.13.13 / 1.2.0 | §8.1 单测；**mockk-android 为仪器测试必需**（v1.3 补入，§8.3） |
| room-testing | 2.8.4 | 与 room 同版本（§8.2） |
| androidx-junit（androidx.test.ext:junit） | 1.1.5 | androidTest 运行器依赖（v1.3 补入，§8.3） |
| androidx-test-runner（androidx.test:runner） | 1.5.2 | **AndroidJUnitRunner 需显式声明**（v1.3 补入，§8.3 实测教训） |



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
| `:feature:settings` | cn.zjl.habitflow.feature.settings | 设置页：深色模式、关于 |

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
:core:testing ──→ :core:model   （TestDataFactory 生成 model 测试数据）
```

**约束规则（面试可讲）**：
- 依赖永远指向"更稳定的层"：model 和 domain 不允许依赖任何 Android 框架类 → 它们可以直接跑纯 JVM 单元测试；
- feature 之间不允许互相依赖 → 换掉一个 feature 不影响其他；
- :core:testing 只能被 `testImplementation`/`androidTestImplementation` 依赖，防止测试代码泄漏进生产；其自身可依赖最稳定层（:core:model）；
- Repository 接口定义在 :core:data（或 domain），实现也在 :core:data，通过 Hilt 绑定接口 → 面试讲"依赖倒置"时有实例。

> **面试可陈述理由**：这个拆法（6 个 core + 3 个 feature + 1 个壳，共 10 个模块）是"面试可讲清"的最优规模：每个模块职责一句话能说完，依赖图在简历上能画出来；再多模块会显得为拆而拆。核心原则是"按稳定度分层，按业务拆 feature"。

### 3.3 各模块 build.gradle.kts 关键配置（v1.3：与工程实测一致，均为"照抄可编译"）

> **v1.3 声明**：以下代码块与工程 `build.gradle.kts` 实测一致；在 v1.2 基础上回写：①全部 android 模块开启 core library desugaring（消费 desugar 库的模块必须同步开启，AAR 元数据检查强制——实测教训）；②androidTest 需显式声明 `androidx.test:runner`（ext-junit 不传递，实测教训）与 `mockk-android`（仪器测试不能用 mockk）；③:feature:* 需 packaging 通配排除 META-INF 许可证文件（mockk→junit-jupiter 传递冲突，实测教训）。

**根目录 `build.gradle.kts`**：

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false        // :core:model / :core:domain 需要
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false   // §4.5 类型安全路由
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

**`:core:model` / `:core:domain`（纯 Kotlin，无需 desugaring）**：

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(project(":core:model"))   // domain 依赖 model；model 无依赖
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

**`:core:data`（v1.3：+ desugaring、+ androidTest runner）**：

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // v1.3：java.time（§5.2 LocalDate）需要
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // v1.3
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.room.testing)
    testImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.junit)            // v1.3：Room 真库测试（§8.2）
    androidTestImplementation(libs.androidx.test.runner)      // v1.3：AndroidJUnitRunner 显式声明
}
```

**`:core:network`（v1.2 补 Hilt/KSP + buildConfig；v1.3 + desugaring + runner）**：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zjl.habitflow.network"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { buildConfig = true }   // LoggingInterceptor/RetrofitClient 需要 BuildConfig.DEBUG
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // v1.3：消费 desugar 库需同步开启
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // v1.3
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)   // HttpLoggingInterceptor（仅 debug，§7.1）
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.runner)   // v1.3：空测试 APK 也需要 runner（全量实测）
}
```

**`:core:designsystem`（v1.3 + desugaring + runner）**：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cn.zjl.habitflow.designsystem"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // v1.3
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // v1.3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)   // material3 1.4+ 不再传递 icons-core
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.lifecycle.viewmodel.ktx)   // §4.4 BaseViewModel（viewModelScope）
    debugImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.androidx.test.runner)   // v1.3
}
```

**`:core:testing`（v1.3 + desugaring + runner）**：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "cn.zjl.habitflow.testing"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // v1.3：TestDataFactory 用 LocalDate
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // v1.3
    implementation(project(":core:model"))   // TestDataFactory 生成 model 测试数据（§3.2）
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.runner)   // v1.3
}
```

**`:feature:*`（v1.2 补 compose；v1.3 + desugaring + runner + mockk-android + packaging 排除）**：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cn.zjl.habitflow.feature.home"   // stats/settings 同理
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { compose = true }
    // v1.3：androidTest 依赖（mockk-android -> junit-jupiter）多 jar 含 META-INF 许可证文件，通配排除（实测）
    packaging {
        resources { excludes += setOf("META-INF/LICENSE*", "META-INF/NOTICE*") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // v1.3：消费 :core:data 需同步开启
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // v1.3
    implementation(project(":core:model"))          // feature 直接使用 model 类型
    implementation(project(":core:domain"))         // HabitValidator（§5.1）
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
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
    androidTestImplementation(libs.mockk.android)   // v1.3：仪器测试必须 mockk-android（实测）
    androidTestImplementation(libs.androidx.junit)  // v1.3
    androidTestImplementation(libs.androidx.test.runner)  // v1.3
    debugImplementation(libs.compose.ui.test.manifest)
}
```

**`:app`（v1.3：+ desugaring、+ proguardFiles、+ icons-core、+ runner）**：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)   // §4.5 类型安全路由
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "cn.zjl.habitflow"
    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt()) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "cn.zjl.habitflow"   // 旧 DSL（android.newDsl=false）下必须在此处
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization { enable = true } // AGP 9 DSL；前置标志见 §3.4
            // v1.3：显式引用默认规则 + 项目规则（§3.5，proguard-rules.pro 已落地）
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true   // v1.3：消费方与依赖模块同步（AAR 元数据检查）
    }
    buildFeatures { compose = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)   // v1.3
    implementation(project(":feature:home"))
    implementation(project(":feature:stats"))
    implementation(project(":feature:settings"))
    implementation(project(":core:designsystem"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)   // 路由序列化
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)   // BottomBar 三 Tab 图标（material3 不传递 icons-core）
    implementation(libs.material)   // XML 启动主题 Theme.Material3.DayNight.NoActionBar（§11.5 双轨）
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)   // v1.3：空测试 APK 也需要 runner（全量实测）
}
```

### 3.4 gradle.properties 关键属性（v1.3：四标志）

```properties
# ① KSP 与 AGP 9 内置 Kotlin 不兼容：必须停用内置 Kotlin，并显式应用 kotlin.android 插件
android.builtInKotlin=false
# ② AGP 9 新 DSL 与外部 kotlin.android 插件不兼容（KSP 路径必需）：回退旧 DSL
#    （①与②必须同时设置，缺一不可，否则插件应用失败）
android.newDsl=false
# ③ AGP 9：optimization.enable=true（R8）需要渐进式支持标志
android.r8.gradual.support=true
# ④ v1.3 补：禁用 UTP（Unified Test Platform），回退旧 ddmlib 路径——修复 CI instrumented
#    设备属性读取超时（实测：UTP PropertyFetcher 侧超时导致设备被跳过；AGP 9.2.1 接受该开关）
android.experimental.testOptions.useUnifiedTestPlatform=false

# 构建性能
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
```

> **风险说明（v1.3 完整版）**：AGP 9.0 起默认启用内置 Kotlin，而 KSP 官方明确"与 AGP 内置 Kotlin 不兼容"；同时 AGP 9 新 DSL（`android.newDsl=true` 默认）与外部 `org.jetbrains.kotlin.android` 插件不兼容。本项目 Room + Hilt + Network 全量依赖 KSP，**四个标志为脚手架必配**（①~③为 §3.3 模板前提，④为 CI instrumented 修复链实测结论，2026-08-20）。AGP 10 将移除 ①② 逃生通道（AGP 9 警告已提示），M4 前评估升级窗口（§11.8 观察项）。

### 3.5 混淆规则（v1.3：已落地）

`proguard-rules.pro`（:app）**已于 2026-08-19 落地**（commit 9614e3b，任务 5.1 前置项），规则内容按本文原文，未冗余补充 Hilt/Room 规则（二者自带 consumer rules）：

```proguard
# Room 实体反射（:core:data 的 consumer rules 已兜底大部分，此处仅显式保留业务实体）
-keep class cn.zjl.habitflow.data.entity.** { *; }
# enum 在 R8 优化下可能被移除，Room 实体字段引用需显式保留（或用 @Keep 标注 Frequency）
-keep enum cn.zjl.habitflow.data.entity.Frequency { *; }
```

> **v1.3 状态说明**：assembleRelease 实测出包（app-release-unsigned.apk ≈2.5MB，mapping.txt 34MB 证明 R8 触发且 entity 类完整包名保留）；Release 工作流 tag 触发实测全绿（run 32359573146，2026-08-20）。

**关键版本要点（面试可讲）**：
- Kotlin 2.x 起 Compose 编译器随 Kotlin 版本发布，用 `org.jetbrains.kotlin.plugin.compose` 插件启用，**不再写** `composeOptions { kotlinCompilerExtensionVersion }`；
- Room schema 目录 `schemas/` 提交进 Git，配合 `room.schemaLocation`，任何版本升级都能用 diff 校验迁移正确性；
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
| Activity | 单 Activity | `MainActivity` | 承载 Scaffold + BottomBar + NavHost（§4.5） |
| Composable | 按功能 | `HomeScreen`、`HabitListItem` | 文件同名，一个 Screen 一个文件 |
| ViewModel | 按页面 | `HomeViewModel`、`SettingsViewModel` | 构造器注入 Repository/DataSource |
| 状态 | 页面级 | `HomeUiState` | 在 ViewModel 同文件内定义 |
| 事件 | 页面级 | `HomeUiEvent` | sealed interface，一次性事件 |
| Repository | 接口+实现 | `HabitRepository` / `HabitRepositoryImpl` | 接口在 :core:data，Hilt `@Binds` 绑定 |
| DataSource | 按技术 | `HabitDao`、`SettingsDataSource` | DAO 即 DataSource 实现 |
| 用例 | 按动作 | `CalculateStreakUseCase`（可选） | 纯逻辑进 :core:domain 而非用例层，避免过度设计 |

> **Screen 可注入约定**：Screen 的 ViewModel 一律以构造参数传入（`HomeScreen(viewModel: HomeViewModel)`），**禁止在 Screen 内部调用 `hiltViewModel()`**；VM 的获取只在导航装配层（`NavHost` 的 composable 块内）进行。此约定使 §8.3 的 Compose UI Test 可以用 fake VM 直构（HomeScreenTest 已按此落地，2.9）。

### 4.3 ViewModel 状态持有约定

- 状态一律 `StateFlow`，UI 一律 `collectAsStateWithLifecycle()`（来自 `androidx.lifecycle:lifecycle-runtime-compose`，§2.3 已列依赖）：
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

### 4.4 BaseViewModel 设计（脚手架必建）

```kotlin
abstract class BaseViewModel : ViewModel() {

    // 事件通道统一在此定义
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

> **v1.3 补：双函数参数陷阱（实测教训）**——`launchTask(block = {...}, onError = {...})` 有两个函数类型参数，Kotlin **trailing lambda 会绑定最后一个参数（onError）**；调用时必须显式命名 `launchTask(block = { ... })`，否则编译报 "No value passed for parameter 'block'"（2.3 实测）。面试可讲"Kotlin 多函数参数的重载与调用歧义"。

> **面试可陈述理由**：BaseViewModel 只收敛"错误处理"和"协程作用域"两个横切点，不做更多抽象。太多 Base 类（BaseActivity/BaseFragment/BaseRepository）是 Java 时代的坏味道，Compose + 组合优于继承，这个设计本身就是可讲的架构取舍。

### 4.5 导航与 BottomBar 集成

- 路由定义：类型安全路由对象（`@Serializable` sealed class / data object）集中在 :app 的 `navigation/Routes.kt`，feature 模块不感知路由实现；**实现前提：kotlin-serialization 插件 + kotlinx-serialization-json 依赖（§2.3）**；
- 装配：`:app` 的 MainActivity 内 `Scaffold + BottomBar + NavHost` 注册三个 feature 根目的地（home/stats/settings），feature 内部的二级页面（如编辑弹窗用 Dialog 而非独立目的地，MVP 阶段避免嵌套 NavGraph）；
- BottomBar 选中态同步：`navController.currentBackStackEntryAsState()` + `NavDestination.hierarchy` + `hasRoute` 判断当前目的地（类型安全），与 BottomBar 三项一一对应（任务 2.5 落地）；
- Tab 切换保持状态：`navigate { popUpTo(startDestination){saveState=true}; launchSingleTop=true; restoreState=true }`——三页 ViewModel 状态保留、切换不销毁（NavBackStackEntry 与 ViewModelStore 绑定，面试考点）；
- 状态恢复：配置变更（旋转）由 ViewModel 存活 + `rememberSaveable` 兜底；进程死亡恢复 MVP 阶段可接受重置（不引入 SavedStateHandle 复杂度，面试可讲该取舍）。

---

## 5. 核心功能设计

### 5.1 习惯 CRUD

流程：`HomeScreen` 点 FAB → 弹窗输入名称、频率、目标 → `HomeViewModel.onSaveHabit` → `HabitRepository.saveHabit` → `HabitDao.upsert`（`@Upsert` 或 `@Insert(onConflict = REPLACE)`）。

涉及模块：`:feature:home` → `:core:data` →（校验）`:core:domain`。

- 删除：长按列表项 → 确认对话框 → 逻辑删除（`isArchived` 字段）而非物理删除，保留统计数据完整性（面试可讲"软删除保证统计一致性"）；列表查询须带 `WHERE isArchived = 0` 过滤（统计与列表口径分离）；
- 编辑与新建共用同一数据校验：`HabitValidator.validate(name, frequency, targetPerWeek)` 返回 sealed `ValidationResult`，位于 :core:domain 纯逻辑层，直接可单测；
- 新建/编辑弹窗状态由 ViewModel 持有（`isEditorVisible` / `editingHabit` / `editorErrorMessage`），保存成功即关闭并靠 Room Flow 自动刷新列表（2.3/2.4 落地）。

**校验规则（与实现一致的精确语义）**：
- 名称：trim 后非空，长度 ≤ 30 字符；
- WEEKLY 时 `targetPerWeek` 必须在 1~7；DAILY 时忽略该字段；
- 失败返回 `ValidationResult.Failure(message)`，成功返回 `Success`（sealed，调用方 when 穷举）。

### 5.2 每日打卡与打卡状态管理

- 打卡记录表 `HabitRecord(habitId, date, completedAt, note?)`，`date` 以 **`Long`(epochDay)** 直接存储（`LocalDate.toEpochDay()`，避免时区问题）；Repository 层映射为 `LocalDate` 供 UI 使用。备选方案：实体直接用 `LocalDate` 字段 + TypeConverter（代码见 §6.1），二选一即可，面试可讲两种的取舍；
- 打卡 = `INSERT` 一条当日记录；撤销 = `DELETE` 当日记录；
- 当日"是否已打卡"通过 DAO 查询：`SELECT EXISTS(SELECT 1 FROM habit_record WHERE habit_id=? AND date=? )`，返回 `Flow<Boolean>`，UI 自动刷新（Room Flow 响应式）；
- **首页实现（v1.3 明确，2.4 落地）**：`HomeViewModel.observeCheckedStates` 用 `flatMapLatest + combine` 合并各习惯的 `observeChecked` 当日流为 `Set<Long>`（checkedHabitIds），勾选 CheckBox 即写库、Flow 回推自动刷新（`HabitListItem` 的 Checkbox 带 `testTag("checkin_${habit.id}")`，§8.3 定位约定）。

> **面试可陈述理由**：用"记录表存在与否"表示打卡状态，而不是在习惯表上放 `todayChecked` 布尔位——布尔位无法支撑历史统计（热力图需要每一天的数据），这体现"表结构为查询服务"的设计意识。

### 5.3 Streak 计算规则与边界情况

`StreakCalculator`（:core:domain 纯函数，**最高优先级单测对象**，1.4/1.5 已落地 9 用例全绿）：

```kotlin
object StreakCalculator {
    // currentStreak: 从今天（或昨天，当天未打时）向前连续打卡天数
    fun currentStreak(
        records: Map<LocalDate, Boolean>,
        today: LocalDate,
        excusedDates: Set<LocalDate> = emptySet(),   // 补卡/请假扩展点（后置功能）
    ): Int
    // longestStreak: 历史最长连续
    fun longestStreak(
        records: Map<LocalDate, Boolean>,
        excusedDates: Set<LocalDate> = emptySet()
    ): Int
}
```

**excused 精确语义（与实现/单测一致）**：
- 豁免日**不计入**连续天数、**不中断**连续性（连续性判断时跳过）；
- longestStreak 中，相邻打卡日间隔内的每一天都是豁免日时，两段**桥接合并**；
- MVP 不实现补卡/请假 UI，仅计算器与表结构（`isExcused` 字段，§6.1）预留，面试可讲"扩展点"。

**边界情况清单（写单测时必须覆盖，面试逐条讲）**：
1. 空数据 → 0；
2. 今天未打卡但昨天已打卡 → current 从昨天起算（业界惯例：今天的"进行中"连续不中断）；
3. 中间断一天 → current 归零重新起算；
4. 跨月/跨年连续（2026-01-31 → 2026-02-01）→ 用 `LocalDate.plusDays(1)` 判断连续性，不依赖月份/年份逻辑；
5. 补卡/请假（后置功能）→ `excusedDates` 参数支持（语义见上），MVP 不实现 UI。

> **面试可陈述理由**：Streak 是这类 App 最容易写错、又最适合展示测试价值的逻辑——"今天没打是否中断"这类边界在真实产品里各 App 实现不同，我通过纯函数 + 表格化测试用例把规则钉死，任何改动都能立刻被测试发现。

### 5.4 统计图表

- 数据来源：`StatsRepository.getCompletionStats(days)` 一次查询出 `(date, isCompleted)` 列表（Room 返回 `Flow<List<Pair<LocalDate, Boolean>>>` 或直接在 SQL 里聚合）；
- 完成率：`completedDays / totalDays`（totalDays 含"未开始的未来日期"排除逻辑）；
- 热力图：每周 7 格排列，颜色深浅按"该周完成次数/7"分级——**不引入图表库**（MPAndroidChart/Vico），用 Compose Canvas 手绘约 50 行，面试展示"Compose 图形 API 能力"，同时避免引入重依赖；
- 趋势折线：同样 Canvas 手绘（后置项，时间不足可砍）。

> **面试可陈述理由**：统计图不引第三方库，用 Compose Canvas 手绘。理由：① 功能简单（热力图本质是彩色网格），引库收益为零；② 手绘是"展示 Compose 能力"的绝佳素材；③ 简历上写"实现过自定义绘制组件"比"用过图表库"含金量高。



---

## 6. 数据层设计

### 6.1 Room

**实体（:core:data/entity/）**（列命名规范与实现一致）：

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
        parentColumns = ["id"], childColumns = ["habit_id"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("habit_id"), Index("date")])
data class HabitRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,   // 列名 snake_case（约定见下）
    val date: Long,                  // epochDay（主方案）
    val completedAt: Long,           // 打卡时间戳
    val isExcused: Boolean = false,  // 补卡/请假预留（§5.3 扩展点）
)

// :core:data/db/AppDatabase.kt
@Database(
    entities = [HabitEntity::class, HabitRecordEntity::class],
    version = 1,
    exportSchema = true               // 禁止 fallbackToDestructiveMigration（见迁移策略）
)
@TypeConverters(Converters::class)   // 备选方案（LocalDate 字段）时启用
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

// :core:data/converters/Converters.kt（备选；主方案 date 为 Long 时无需注册）
class Converters {
    @TypeConverter fun fromEpochDay(value: Long): LocalDate = LocalDate.ofEpochDay(value)
    @TypeConverter fun toEpochDay(date: LocalDate): Long = date.toEpochDay()
}
```

**列命名规范（与实现一致，实测教训）**：实体字段 camelCase（`habitId`），数据库列名 snake_case（`habit_id`），实体字段通过 `@ColumnInfo(name = "habit_id")` 显式映射；DAO/Relation/外键中的 SQL 列引用一律与列名一致——**列名不一致会直接报 "no such column" 运行时错误**（1.6 实测）。

**DAO 要点**：
- 全部返回 `Flow`（响应式，UI 无需手动刷新）；
- 组合查询：`@Transaction` + `@Relation` 或 flatMap 组装 `HabitWithRecords`；
- 写操作用 `suspend fun`；
- `observeChecked(habitId, date)`：`SELECT EXISTS(...)` 返回 `Flow<Boolean>`（§5.2）；
- 软删除过滤：`observeActiveHabits` 带 `WHERE isArchived = 0`（§5.1）。

**数据库版本与迁移策略（正面回答 DataCollector 的短板）**：
- `version = 1` 起步，`schemas/` 目录进 Git（1.json 已入库）；
- 每次 schema 变更必须 `exportSchema = true` + 生成新版本 + 编写 `Migration`，**禁止** `fallbackToDestructiveMigration`——这条规则在文档里写死，代码评审照此检查；
- 面试答案："我在第一个项目里用过 destructive fallback 被坑过，这个项目从 v1 开始就规范化 schema 导出与迁移流程"。

### 6.2 DataStore（Preferences DataStore）

- 存什么：`isDarkMode`（无 UI 对应的键不引入，一条数据绝不双写两处）；
- 与 Room 的职责划分：**Room 存"可结构化查询的业务数据"，DataStore 存"需要即时读取的轻量偏好"**；
- 封装：`SettingsDataSource` 暴露 `val isDarkMode: Flow<Boolean>` 与 `suspend fun setDarkMode(Boolean)`；
- 初始化：`PreferenceDataStoreFactory.create` 由 DataModule 以单例提供（Context 注入一次），避免多实例重复读文件（面试可讲）；
- **深色模式全局生效（2.6 落地）**：MainActivity 以 activity 级 `SettingsViewModel` 收集 `isDarkMode` 驱动 `HabitFlowTheme(darkTheme = ...)`（§11.5 双轨），设置页 Switch 乐观更新 + DataStore 持久化（杀进程重启保持，已人工验证）。

> **面试可陈述理由**：两种持久化的边界一句话讲清——"业务数据进 Room 因为它要 SQL 查询和 Flow 响应式更新；偏好进 DataStore 因为它是键值且读频繁写极少"。面试官追问"为什么不用 SharedPreferences"：SP 的 `apply()` 异步不可观察、`commit()` 阻塞主线程，DataStore 原生 Flow 化，与状态驱动 UI 完美配合。

### 6.3 Repository 组织方式

- 接口 + 实现全部放 :core:data（MVP 不把接口放 domain 层——避免为抽象而抽象，面试可讲这个取舍）；
- Hilt 绑定（多模块下模块内组织，:app 只依赖 feature 与 designsystem，不直接感知数据层实现）：
  ```kotlin
  @Module @InstallIn(SingletonComponent::class)
  abstract class RepositoryModule {
      @Binds abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
      // TokenStore 依赖倒置绑定（§7.1）：接口在 :core:network，实现由 DataStore 完成
      @Binds abstract fun bindTokenStore(impl: DataStoreTokenStore): TokenStore
  }
  ```
- `DataModule`：`@Provides @Singleton fun provideDatabase(@ApplicationContext ctx): AppDatabase`、`provideDataStore(...)`、`provideHabitDao(db)`——Room/DataStore 均需 `@ApplicationContext` 注入；
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

**结论：MVP 不接真实后端，但 :core:network 模块必须建**（骨架为必做、接入为后补），包含：

1. `ApiResponse<T>` 统一包装（`Success`/`Error(code,msg)`，参考国内后端常见 `{code,message,data}` 结构）；
2. OkHttp 拦截器链骨架：
   - `LoggingInterceptor`（debug 下打印请求/响应；实现前提：模块开启 `buildConfig`，见 §3.3）；
   - `AuthInterceptor`（从 TokenStore 取 token 注入 `Authorization: Bearer`，依赖倒置见下）；
   - `AuthRefreshInterceptor`（401 时调刷新接口重放原请求，**预留占位**，注释清楚设计意图）；
3. `RetrofitClient` 工厂（OkHttpClient 超时 10s 连接/30s 读取、`HttpLoggingInterceptor` 仅 debug）；
4. `TokenStore` 接口（定义于 :core:network 消费方，实现 `DataStoreTokenStore` 由 :core:data 完成并经 Hilt `@Binds` 注入——**依赖倒置**，编译期 network 不依赖 data）。

### 7.2 面试应对策略（为什么没有网络模块的质疑）

标准回答三段式：
1. **范围决策**："习惯打卡类 App 的核心价值在本地记录与统计，云同步需要自建后端与账号体系，单人 2 个月周期内会严重稀释前端工程化展示，所以 MVP 定为纯本地，这是有意的范围裁剪而非能力缺失"；
2. **能力证明**："但网络层不是没做过——我的 DataCollector 项目有完整实战：Retrofit + JWT + OkHttp 拦截器 + 401 刷新重放 + 指数退避重试 + 幂等去重，这套东西我在 HabitFlow 里以拦截器骨架的形式复用了设计"；
3. **架构证明**："网络层以模块形式存在，任何功能接入后端都无需改 UI 层，Repository 是唯一入口，这是设计时预留的扩展点"。

---

## 8. 测试策略

### 8.1 单元测试（test/ 源码集，JUnit + MockK + Turbine + coroutines-test）

**测试资产现状（v1.3 登记，M2 已全部落地）**：

| 测试类 | 用例数 | 落地任务 | 状态 |
|---|---|---|---|
| `StreakCalculatorTest`（:core:domain） | 9 | 1.5 | ✅ 全绿 |
| `HomeViewModelTest`（:feature:home） | 8 | 2.7（2.2~2.4 提前部分并入） | ✅ 全绿（MockK + MainDispatcherRule + Turbine） |
| `SettingsViewModelTest`（:feature:settings） | 2 | 2.6 | ✅ 全绿 |

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

> **v1.3 补（实测技巧）**：HomeViewModel 的 `observeHabits` 会为每个习惯合并 `observeChecked` 流，单测需 stub `observeChecked`（`coEvery { observeChecked(any(), any()) } returns flowOf(false)`）；`assertEquals(emptySet<Long>(), ...)` 需显式类型参数避免推断失败；事件断言用 `vm.events.test { awaitItem() }`（Turbine）。

### 8.2 Room / DataStore 测试（androidTest 真库，推荐方案）

**推荐方案（2.8 已落地，HabitDaoTest 5 用例全绿）**：Room 真库测试放 **androidTest**（`@RunWith(AndroidJUnit4::class)` + `ApplicationProvider.getApplicationContext()` + `Room.inMemoryDatabaseBuilder` + `allowMainThreadQueries`），原因：
- 真 SQLite 行为最可靠（Robolectric 的 SQLite 是 shadow 实现，偶有差异）；
- androidTest 需要模拟器/真机，CI 里跑 `connectedDebugAndroidTest`（§9.1 emulator job 已全绿）。

> **v1.3 补（实测教训）**：androidTest 依赖必须显式声明 `androidx.test:runner`（ext-junit 不传递，缺失报 `ClassNotFoundException: AndroidJUnitRunner` / `Process crashed`）；`:core:data` 依赖 `androidx-junit`（ext）+ `androidx-test-runner`。

**备选（CI 无模拟器时）**：Robolectric + Room 在 test 里跑，作为本地快速验证（MVP 阶段保证 androidTest 一条路径跑通，避免双写）。

**DataStore 测试**：`PreferencesDataStore` 可在 test 中用 `TemporaryFolder` 指定文件路径（`PreferenceDataStoreFactory.create(scope, produceFile = { file })`），存→读→断言。

### 8.3 Compose UI Test（androidTest 源码集）

最小可运行配置（依赖见 §3.3 :feature:*）：

```kotlin
dependencies {
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)   // v1.3：仪器测试必须 mockk-android（mockk 报 Failed to load plugin）
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
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
        composeRule.onNodeWithTag("checkin_1").performClick()
        composeRule.onNodeWithTag("checkin_1").assertIsOn()
    }
}
```

> 说明：UI 测试采用 **fake VM 直构方案**（不引入 hilt-android-testing），依赖 §4.2 的"Screen 可注入约定"（HomeScreenTest 已落地，2.9：空态/列表渲染/CheckBox 状态变化/FAB 打开弹窗 4 用例）；MockK mock HomeViewModel + `MutableStateFlow` 桩 uiState，onCheckIn/onShowCreateDialog 由测试内联模拟真实行为。若未来需要完整 DI 链路测试，备选 `@HiltAndroidTest` + `HiltTestApplication`（需另加 hilt-android-testing 依赖）。

> **v1.3 补（实测教训）**：① mockk 在仪器测试必须换 `mockk-android`（否则 `Failed to load plugin`）；② mockk→junit-jupiter 传递 jar 含 META-INF 许可证文件，:feature:* 需 `packaging { resources { excludes += setOf("META-INF/LICENSE*", "META-INF/NOTICE*") } }`；③ Checkbox 需 `testTag("checkin_${habit.id}")` 定位（§8.3 约定，HabitListItem 已加）。

**test 与 androidTest 职责划分（面试必讲）**：
- `test/`：纯 JVM 逻辑（计算器、ViewModel 状态机、Repository 映射）——快、稳、CI 必跑；
- `androidTest/`：真库（Room）+ 真 UI（Compose）——慢，需模拟器，CI 用 emulator job 跑，本地用 `gradlew connectedDebugAndroidTest`。

### 8.4 依赖与目录规划（脚手架阶段最小集）

```
:core:domain/src/test/.../StreakCalculatorTest.kt      # 第一个测试（M1 已落地，9 用例全绿）
:core:data/src/androidTest/.../HabitDaoTest.kt          # Room 真库（2.8，5 用例全绿）
:feature:home/src/test/.../HomeViewModelTest.kt         # MockK + MainDispatcherRule（2.7，8 用例全绿）
:feature:home/src/androidTest/.../HomeScreenTest.kt     # Compose UI Test（2.9，4 用例全绿）
:core:data/src/test/.../HabitRepositoryImplTest.kt      # MockK + Turbine（3.9，待开发）
```



---

## 9. CI/CD 设计

### 9.1 GitHub Actions（:app 所在仓库根目录 `.github/workflows/`）

**工作流一 `ci.yml`（v1.3：工程实测现行版，2026-08-20 全绿）**：

```yaml
name: CI

on:
  pull_request:
  push:
    branches: [master, 'ci-debug/**']   # ci-debug 分支：instrumented 修复链先验证再合入 master（v1.3）

# 并发取消：连续 push 时取消旧 run；workflow 名 CI/Release 隔离，tag 触发不受影响（v1.3）
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Make gradlew executable
        run: chmod +x gradlew
      - uses: actions/setup-java@v5
        with: { distribution: temurin, java-version: 17 }
      - name: Gradle cache
        uses: actions/cache@v4
        with:
          # 覆盖依赖/构建缓存 + wrapper 发行版；精确键含 runner.os 与 gradle-wrapper.properties hash；
          # restore-keys 实现部分命中回退（2026-08-21 增补，§11.8 第 6 项）
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', 'gradle/libs.versions.toml', 'gradle/wrapper/gradle-wrapper.properties') }}
          restore-keys: |
            gradle-${{ runner.os }}-
      - name: Unit tests
        run: ./gradlew testDebugUnitTest
      - name: Lint
        run: ./gradlew lintDebug
      - name: Assemble
        run: ./gradlew assembleDebug

  instrumented:
    runs-on: ubuntu-latest
    needs: build
    timeout-minutes: 30
    strategy:
      matrix: { api-level: [34] }   # 34 规避 v2 在 API 30 镜像的 UTP API level 读取 bug（v1.3 实测）
    steps:
      - uses: actions/checkout@v4
      - name: Make gradlew executable
        run: chmod +x gradlew   # 修复：缺此步报 Permission denied（run 32266737466 实测）
      - uses: actions/setup-java@v5
        with: { distribution: temurin, java-version: 17 }
      - name: Gradle cache (instrumented)
        # 键隔离：独立前缀写入（gradle-instrumented-*），回退链复用 build job 缓存（2026-08-21 增补，§11.8 第 6 项）
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-instrumented-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', 'gradle/libs.versions.toml', 'gradle/wrapper/gradle-wrapper.properties') }}
          restore-keys: |
            gradle-instrumented-${{ runner.os }}-
            gradle-${{ runner.os }}-
      - name: Enable KVM permissions
        # 修复：KVM 无权限 → 软件模拟极慢 → ddmlib 属性读取超时（run 32394658855 实测）；
        # 授予读写并验证，失败即退出——这是 instrumented 全绿的关键一步（run 32398123883 实测）
        run: |
          ls -l /dev/kvm
          sudo chmod 666 /dev/kvm
          ls -l /dev/kvm
          test -r /dev/kvm && test -w /dev/kvm
      - uses: reactivecircus/android-emulator-runner@v2   # 恢复 v2：v3 标签不存在（run 32361742157 实测）
        with:
          api-level: ${{ matrix.api-level }}
          arch: x86_64
          script: adb shell getprop ro.build.version.sdk && ./gradlew connectedDebugAndroidTest   # 预热设备属性（adb 侧）
```

**instrumented job 修复链演进（v1.3 登记，2026-08-20 六次尝试后全绿）**：

| # | 修复 | 失败证据（run） | 效果 |
|---|---|---|---|
| ① | `chmod +x gradlew` | 32266737466：Permission denied (exit 126) | 脚本可执行 |
| ② | runner 恢复 @v2（v3 标签不存在） | 32361742157：unable to find version v3 | action 可解析 |
| ③ | api-level 30→34 | 32359568752：API level=1，Cannot install split APKs | 安装通过 |
| ④ | script 前 `adb shell getprop` 预热 | 32371966583：PropertyFetcher CommandUnresponsiveException | adb 侧就绪（UTP 侧未解决） |
| ⑤ | `useUnifiedTestPlatform=false` 禁用 UTP（§3.4 ④） | 32394658855：ddmlib 侧同样超时——根因不在 UTP | 回退 ddmlib 路径 |
| ⑥ | **Enable KVM permissions**（chmod 666 /dev/kvm） | 32394658855：ProbeKVM 无权限——软件模拟极慢致 ddmlib 超时 | **run 32398123883 双 job 全绿（7m53s）** |

> **面试可陈述理由（CI 排障故事）**：instrumented 修复链是一次完整的"根因逐层剥离"实战——从表面症状（Permission denied）逐层下挖（action 版本 → API level → UTP 通道 → KVM 硬件加速），每层都用 `--log-failed` 实测证据定位而非猜测；最终根因是 CI runner 的 KVM 权限导致软件模拟极慢、ddmlib 命令超时。这个故事体现"系统化排障 + 证据驱动"的工程素养。

**CI 缓存优化（2026-08-21 增补，§11.8 第 6 项落地，待 ci-debug 实测回写）**：

- **build job**：`path` 覆盖 `~/.gradle/caches`（依赖/构建缓存）+ `~/.gradle/wrapper`（Gradle 发行版，加速环境准备）；
  精确键含 `runner.os` 与 `gradle-wrapper.properties` hash；`restore-keys: gradle-<os>-` 实现缓存未完全命中时的部分回退；
- **instrumented job**：键隔离——独立前缀 `gradle-instrumented-`（与 build 缓存写入互不覆盖），回退链
  `gradle-instrumented-<os>-` → `gradle-<os>-` 复用 build job 已保存的依赖缓存（`needs: build` 时序保证其先完成）；
- **隔离方式**：键级隔离（两个 job 各占独立 runner，无文件锁竞争），不做 `GRADLE_USER_HOME` 目录隔离（避免依赖重复下载、缓存体积翻倍）；
- **验证状态**：配置定稿版，未实测；推 `ci-debug/**` 验证 cache restore/save 后回写本模板。

**工作流二 `release.yml`（v1.3：tag 触发实测全绿，2026-08-20 run 32359573146）**：

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
      - uses: actions/setup-java@v5
        with: { distribution: temurin, java-version: 17 }
      - name: Build release
        run: ./gradlew assembleRelease
      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: habitflow-release
          path: app/build/outputs/apk/release/*.apk
```

**验收现状（v1.3 登记）**：CI build job 全绿（2.10，run 32251846306）；instrumented job 全绿（2.12，run 32398123883）；Release tag 触发出包 + artifact 上传（2.11，run 32359573146，v0.1.0-m2）。

> **面试可陈述理由**：CI 不只是跑通命令——`testDebugUnitTest` 在 PR 阶段拦截回归、emulator job 保证 UI 测试不烂尾（含 KVM 硬件加速保障），这些闸门对应"质量内建"理念；release 流水线让"发版"从手动操作变成一键产物，是简历上"从代码到交付的完整链路"的直接证据。

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
| CI 排障故事（v1.3 新增） | instrumented 修复链六步：chmod → @v2 → api-level 34 → 预热 → UTP 禁用 → KVM 权限，每步证据驱动 |

---

## 11. 附录

### 11.1 版本基线决策（已定版，2026-08-13）

- **决策结果**：模板基线——Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37（与 AS 模板一致，官方对照表 AGP 9.2 ↔ Gradle 9.4.1 匹配）；
- **Java 统一为 17**（compileOptions + `kotlin { compilerOptions { jvmTarget } }`，全部 android 模块）；
- v1.1 的"文档基线（9.3.1/9.1.0/36）"选项已弃用，不再作为候选；
- 全部模块 SDK 版本统一走 toml（compileSdk/minSdk/targetSdk 条目）。

### 11.2 模板遗留清理清单（已执行完毕，M1）

- [x] 删除 `FirstFragment`/`SecondFragment`、`fragment_first/second.xml`、`nav_graph.xml`（XML 导航整体弃用）
- [x] 删除 `menu_main.xml`、`content_main.xml`、`activity_main.xml`（含 FAB/AppBar 模板逻辑）
- [x] 关闭 `viewBinding`（app/build.gradle.kts `buildFeatures`）
- [x] `MainActivity` 重写为 Compose + `@AndroidEntryPoint`，承载 Scaffold + BottomBar + NavHost（§4.5）
- [x] 评估 `keepRules/rules.keep`、`backup_rules.xml`、`data_extraction_rules.xml`、模板 dimens/colors 的去留（已执行）
- [x] 删除模板字符串资源（first/second_fragment_label 等）

### 11.3 命名空间与包名迁移（已执行完毕，M1）

- [x] `com.example.habitflow` → `cn.zjl.habitflow`：applicationId、namespace、Kotlin 包路径、Manifest 引用
- [x] 各模块 namespace 按 §3.1 登记表配置（AGP 9 默认 `android.uniquePackageNames=true`）

### 11.4 构建脚本重写（已执行完毕，M1）

- [x] `settings.gradle.kts`：`include` 10 个模块（§3.1）
- [x] 根 `build.gradle.kts`：8 个插件 `apply false`（§3.3）
- [x] `gradle/libs.versions.toml`：按 §2.3 落地
- [x] `gradle.properties`：四标志（§3.4）+ 配置缓存
- [x] wrapper 版本按 §11.1 决策定版（9.4.1）

### 11.5 主题与 Manifest（已执行完毕，M1；2.6 扩展）

- [x] 模板 `Theme.Material3.DayNight.NoActionBar` 保留为启动主题，Compose 内由 `HabitFlowTheme` 接管（双轨）
- [x] Manifest 注册 `HabitFlowApplication`（`@HiltAndroidApp`）
- [x] 深色模式全局驱动：MainActivity 收集 SettingsViewModel.isDarkMode → `HabitFlowTheme(darkTheme = ...)`（2.6）

### 11.6 迁移后自检（已执行完毕，M1 验收）

- [x] `./gradlew assembleDebug` 通过（10 模块全编译，304 tasks）
- [x] 安装启动不闪退（模拟器人工验证）
- [x] `git log` 迁移历史完整（M1 各任务独立提交 + tag v0.1.0-m1）

### 11.7 文档拆分预案（超规模触发）

本文超过约 1200 行时：①`build-config.md`——§2 版本表 + §3.3/§3.4/§3.5 构建配置（最易变层）；②`interfaces/`——各 feature/core 接口契约（模块落定后按模块拆）；③本文保留稳定架构叙事（§1/§4/§5/§6/§7/§10）。拆分后同步更新 README 导航表。

### 11.8 已知约束与待办登记（v1.3 更新）

| # | 事项 | 归属 | 状态 |
|---|---|---|---|
| 1 | :app `proguard-rules.pro`（§3.5 内容） | 任务 5.1 前置 | ✅ 已办（2026-08-19，commit 9614e3b） |
| 2 | feature 占位 Screen 中文文案迁移至字符串资源 | 任务 4.7 | 待办 |
| 3 | 源码注释中 `TECH_DESIGN_v1.2 §x.x` 引用迁移至 v1.3 | 文档升级配套 | 待办（不阻塞功能） |
| 4 | `android.r8.gradual.support` 实验性标志在 AGP 10 的兼容性跟踪 | 构建配置 | 观察 |
| 5 | `useUnifiedTestPlatform=false` 与 builtInKotlin/newDsl 逃生通道在 AGP 10 的移除跟踪（v1.3 新增） | 构建配置 | 观察 |
| 6 | CI 缓存优化（Gradle restore-keys/wrapper + instrumented 键隔离）——build/instrumented 已实施（2026-08-21，§9.1 配置定稿，待 ci-debug 实测回写）；AVD 缓存仍按需评估 | CI 优化 | 进行中 |
| 7 | actions 升级项：setup-java v4→v5——**v5 已升级（2026-08-21，ci.yml/release.yml + §9.1 模板同步）**；Node.js 20 deprecation（checkout/cache@v4 由 GitHub 强制回退跑 Node 24，持续观察是否需升 v5） | CI 维护 | 部分完成 |
