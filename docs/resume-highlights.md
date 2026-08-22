# HabitFlow 简历素材（任务 5.5）

> 来源：TECH_DESIGN_v1.3 §10 面试速查表 + 项目实际达成数据 + DEVELOPMENT_PLAN_v1.3 里程碑验收。
> 每条遵循「**业务问题 → 设计决策 → 结果**」三段式，可直接用于简历项目描述与面试讲述。

---

## 亮点 1：多模块架构设计与依赖管理

- **业务问题**：单模块 App 职责混乱（UI/逻辑/数据耦合），功能迭代互相牵连，编译慢、可测试性差。
- **设计决策**：拆分为 **10 个 Gradle 模块**（6 core + 3 feature + 1 壳），按"稳定度分层 + 业务拆 feature"——`model`/`domain` 纯 Kotlin 无 Android 依赖（可直接 JVM 单测），`data`/`network`/`designsystem` 为稳定层，`feature` 按业务隔离；依赖**单向**（feature → core，禁止反向）；全版本统一走 `libs.versions.toml` 版本目录（Gradle 9 + AGP 9，Kotlin 2.3.20）。
- **结果**：换掉一个 feature 不影响其他模块；纯逻辑层（StreakCalculator）零依赖直接单测；版本零漂移。模块化规模控制在"每个模块职责一句话能说完、依赖图能画出来"的面试最优区间。

## 亮点 2：测试分层策略与覆盖率

- **业务问题**：习惯打卡的连续天数（Streak）计算极易出错（断一天、跨月、补卡豁免等边界），且 Room/Hilt/Compose 链路需真实环境验证。
- **设计决策**：三层测试——① **单元测试 45 用例**（StreakCalculator 边界 9 + 参数化豁免 7 + Repository 映射 8 + ViewModel 27），纯 JVM 快速跑；② **Room 真库测试**（HabitDaoTest，inMemory 库验证 upsert/软删除/外键级联）；③ **Compose UI 测试**（HomeScreenTest/StatsScreenTest，fake VM 直构 + testTag 交互断言）；测试公用件（MainDispatcherRule + TestDataFactory）抽到 `:core:testing` 复用。
- **结果**：**61 个测试用例全绿**（单测 45 + 仪器 16），CI 第一道门。Streak 边界规则被表格化用例钉死，任何改动立即被测试发现。

## 亮点 3：CI/CD 全链路建设与排障能力

- **业务问题**：手动构建/测试/发版效率低，代码质量无门禁，发版流程不可复现。
- **设计决策**：GitHub Actions 三流水线——**build job**（testDebugUnitTest → lintDebug → ktlintCheck → assembleDebug）+ **instrumented job**（android-emulator-runner 跑仪器测试，配置 KVM 权限）+ **Release**（`v*` tag 触发 assembleRelease 上传 APK）；接入 ktlint 静态检查作为代码规范门禁。
- **结果**：CI/Release 双链路实测全绿，打 3 个里程碑 tag（v0.1.0-m1/m2/m3）。**排障亮点**：instrumented 模拟器超时经六步证据驱动修复（`chmod +x gradlew` → runner `@v2` → `api-level 34` → 属性预热 → 禁用 UTP → 最终定位 KVM 权限根因），体现系统化排障能力。

## 亮点 4：MVVM+MVI 架构与响应式数据流

- **业务问题**：UI 状态散落各处、状态变更不可追踪，打卡后列表/统计页需手动刷新，易产生状态不一致。
- **设计决策**：**MVVM + MVI 单向数据流**——ViewModel 持 `StateFlow`（UI 只读）+ `Channel`（一次性事件）；UI 调用 `onXxx()` 意图；数据链路 `Room → Repository → ViewModel → Composable` 全响应式（Room Flow 查询，数据变更 UI 自动刷新）；`BaseViewModel` 统一 `launchTask` 协程兜底（CancellationException 必须 rethrow）；UI 用 `collectAsStateWithLifecycle`（后台不收集，生命周期感知）。
- **结果**：打卡/撤销后首页/统计页自动刷新，无需手动刷新机制；状态变更可追踪；抽象控制在"两个横切点"（错误兜底 + 事件通道），避免过度设计。

## 亮点 5：R8 混淆与 Release 构建实践

- **业务问题**：release 包经 R8 混淆/优化后可能误删反射引用的类（Room 实体/Hilt 注入），导致运行时崩溃；发布包体积需优化。
- **设计决策**：`optimization { enable = true }`（AGP 9 DSL）+ 显式 `proguard-rules.pro`（保留 Room 实体 + Frequency 枚举）；**动态签名**（`keystore.properties` 本地读取，缺失时出 unsigned 包兼容 CI，密钥绝不入库）；语义化版本号（versionCode 递增 + versionName "0.3.0"）。
- **结果**：release 包 2.51MB，签名有效（apksigner verify），模拟器安装后核心流程全通过（R8 未破坏 Room/Hilt 反射）；签名配置兼容本地发布与 CI 构建双场景。

---

## 使用建议

- **简历**：选 3 条最有匹配度的（按投递岗位 JD 侧重选，如测后端偏选 1/2/3）；
- **面试**：每条可展开讲 2 分钟，重点讲"为什么这么选"而非"用了什么"；
- **数据支撑**：10 模块、61 测试用例、3 个里程碑、6 步排障、61 用例全绿——均为可核验的真实数据。
