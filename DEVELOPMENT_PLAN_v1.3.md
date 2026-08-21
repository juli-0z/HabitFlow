# HabitFlow 开发计划（v1.3）

> **文档名称**：HabitFlow 开发计划
> **版本**：v1.3 ｜ **状态**：当前生效 ｜ **最后更新**：2026-08-21
> **版本基线**：Gradle 9.4.1 / AGP 9.2.1 / Kotlin 2.3.20 / KSP 2.3.11 / Hilt 2.59.1 / Room 2.8.4 / Compose BOM 2026.05.00 / desugar_jdk_libs 2.1.4 / compileSdk 37 / Java 17（模板基线，与 TECH_DESIGN 一致）
> **原文档关系**：v1.3 替代 v1.2 成为现行版本；DEVELOPMENT_PLAN.md（v1.0）与 DEVELOPMENT_PLAN_v1.1.md / v1.2 冻结存档，不再更新
> **关联文档**：TECH_DESIGN_v1.3.md（本文简称 TECH_DESIGN）；本文简称 DEVELOPMENT_PLAN

## 修订记录

| 版本 | 日期 | 变更摘要 |
|---|---|---|
| v1.0 | 脚手架定案版 | 原始创建（DEVELOPMENT_PLAN.md） |
| v1.1 | 2026-08-12 | checkbox 化、M2 验收修正、时间线图修正、风险表补充等（详见 v1.1 修订记录） |
| v1.2 | 2026-08-15 | ①M1 里程碑达成登记（2026-08-15：assembleDebug 304 tasks 全绿、domain 9 用例全绿、模拟器启动人工验证通过、tag v0.1.0-m1）；②任务勾选状态与工程事实同步——1.1~1.13 全部勾选；③落实审计清单 O：1.8~1.10"编译通过"验收补充说明；④§7 风险表新增 KSP/serialization 兼容观察项；⑤M2 任务前置标注（2.1 已由 1.6 提前完成） |
| v1.3 | 2026-08-21 | ①**M2 里程碑达成登记**：§2.2 两项验收人工确认（2026-08-16~19 闭环回归 + 深色持久化）+ 单测 19 用例 + 仪器测试 9 用例全绿 + CI build/instrumented 双 job 全绿 + Release tag 触发出包；②2.2~2.12 全部勾选（含 2.12 修复链六次尝试后全绿，run 32398123883）；③tag `v0.1.0-m2` 登记（2026-08-19，指向 3633388）；④M3 任务状态更新为"待开始"（3.1 起）；⑤§7 风险表更新（CI emulator 风险已解决，新增 UTP/KVM 排障经验已回写 TECH_DESIGN §9.1） |

## 编号稳定约定

- 里程碑编号 M1~M4 与周次对应关系固定（M1=第 1 周、M2=第 2 周、M3=第 3~4 周、质量版=第 5~6 周、M4=第 7~8 周），不因进度偏移而重编号；
- 任务编号 1.1~1.13、2.1~2.12、3.1~3.11、4.1~4.7、5.1~5.7 保持稳定：**只按编号勾选，不重编号、不删编号**（裁剪任务时编号保留并标注"已裁剪"）；
- 勾选状态是本文档唯一的"进度事实"，不另设状态文件。

---

## 0. 总体时间线与里程碑

```
第 1 周    第 2 周     第 3~4 周      第 5~6 周       第 7~8 周
│脚手架    │基建收尾     │核心功能        │测试加固+打磨    │打磨+发布
│(M1)      │首个端到端    │打卡闭环         │覆盖率补齐/回归  │(M4)
│          │Demo (M2)   │统计页/热力图(M3)│               │
└──────────┴────────────┴───────────────┴──────────────┘
```

里程碑（勾选即验收通过）：

- [x] **M1 脚手架完成**：10 个模块可编译，`assembleDebug` 通过 —— **已达成（2026-08-15）**：304 tasks 全绿、domain 9 用例全绿、模拟器启动人工验证通过、tag `v0.1.0-m1` 已打
- [x] **M2 首个端到端 Demo**：模拟器上"建习惯 → 打卡 → 首页展示"完整闭环 —— **已达成（2026-08-19）**：闭环回归人工验证 + 深色持久化 + 单测 19 用例 + 仪器测试 9 用例全绿 + CI/Release 双链路全绿；tag `v0.1.0-m2` 已打（2026-08-19，commit 3633388）
- [ ] M3 核心功能冻结：CRUD + 打卡 + Streak 计算全绿，feature 冻结不加新需求
- [ ] M4 发布准备：CI 全绿、release 构建通过、可选上架

**触发功能裁剪的条件（提前定好，避免战线失控）**：

- [ ] 条件 1：第 6 周结束时 UI 测试仍未跑通 → 砍掉设置页 UI 测试，只保留单元测试
- [ ] 条件 2：第 7 周时上架流程受阻（缺身份证/签名问题等）→ 砍掉上架，改为"生成 release APK + 自查报告"
- [ ] 条件 3：任何一周进度落后超过 3 天 → 优先砍【后补】任务，绝不动【必做】任务

---

## 1. 第 1 周：脚手架搭建（产出：M1）【已达成】

### 1.1 任务清单

- [x] 1.1 创建项目骨架：`gradle/libs.versions.toml`（补全版本目录，见 TECH_DESIGN §2.3）、根 `build.gradle.kts`（声明全部插件 apply false）、`settings.gradle.kts`（include 10 个模块）、`gradle.properties`（四标志，见 TECH_DESIGN §3.4）、`.gitignore`、wrapper 版本定版（模板基线 9.4.1）【必做】— 验收：`./gradlew help` 通过 ✅
- [x] 1.2 创建 10 个模块目录与各模块 `build.gradle.kts`（见 TECH_DESIGN §3.3）【必做】— 验收：`./gradlew assembleDebug` 全模块编译通过 ✅
- [x] 1.3 :core:model 定义 `Habit`、`HabitRecord`、`Frequency`、`StreakStats`【必做】— 验收：`./gradlew :core:model:test` 通过 ✅
- [x] 1.4 :core:domain 实现 `StreakCalculator` + `HabitValidator`【必做】— 验收：编译通过（正确性由 1.5 单测验证）✅
- [x] 1.5 第一个单元测试：`StreakCalculatorTest` 覆盖 TECH_DESIGN §5.3 全部 5 类边界【必做】— 验收：`./gradlew :core:domain:test` 全绿 ✅（9 用例，M1 验收复核通过）
- [x] 1.6 :core:data 搭 Room：`HabitEntity`、`HabitRecordEntity`、`HabitDao`、`AppDatabase`（version=1，exportSchema 开，@TypeConverters 注册，见 TECH_DESIGN §6.1）【必做】— 验收：`schemas/` 目录生成并提交 Git ✅（1.json 已入库）
- [x] 1.7 :core:data 搭 DataStore：`SettingsDataSource`（仅 `isDarkMode`，见 TECH_DESIGN §6.2）+ `TokenStore`（网络预留）【必做】— 验收：编译通过 ✅
- [x] 1.8 :core:designsystem 建 Theme（Color/Type/Shape）+ 状态组件（Loading/Empty/Error 三态视图）【必做】— 验收：编译通过 ✅（组件行为验证随 2.x 页面接入兑现，3.10 复用）
- [x] 1.9 :core:network 建骨架：`ApiResponse`、`LoggingInterceptor`、`AuthInterceptor`（占位）、`AuthRefreshInterceptor`（占位）、`RetrofitClient`【必做】— 验收：编译通过（无真实调用）✅
- [x] 1.10 :core:testing 建 `MainDispatcherRule` + `TestDataFactory`【必做】— 验收：被 1.5 或 2.7 复用即可 ✅（2.7 已兑现"被复用"验收）
- [x] 1.11 Hilt 装配：:app 的 `HabitFlowApplication`（`@HiltAndroidApp`）+ `RepositoryModule`（@Binds）+ `DataModule`（Room/DataStore 提供者，@ApplicationContext 注入）【必做】— 验收：编译通过 ✅；启动不崩 ✅（模拟器人工验证，2026-08-15）
- [x] 1.12 空 MainActivity + 空 NavHost，`@AndroidEntryPoint`（Screen 以 ViewModel 为构造参数，见 TECH_DESIGN §4.2）【必做】— 验收：安装到模拟器不闪退 ✅（人工验证通过，2026-08-15；导航骨架已扩展为 Scaffold + BottomBar + NavHost + 三路由）
- [x] 1.13 git init + 首次提交【必做】— 验收：`git log` 有记录 ✅（858bb7e 起，M1 共 8 笔提交 + tag v0.1.0-m1）

### 1.2 第 1 周产出物与验收总结【已达成】

- [x] M1 验收：`./gradlew assembleDebug` 通过（304 tasks 全绿，2026-08-15 复核）
- [x] M1 验收：`./gradlew :core:domain:test` 全绿（9 用例）
- [x] M1 验收：安装到模拟器不闪退（人工验证）
- 产出：可编译的 10 模块工程 + `StreakCalculatorTest` 全绿 + Room schema v1 入 Git + :app 模板迁移完成 + 导航骨架 + tag `v0.1.0-m1`。

---

## 2. 第 2 周：基建收尾 + 首个端到端 Demo（产出：M2）【已达成】

### 2.1 任务清单

- [x] 2.1 Repository 层：`HabitRepository` 接口 + `HabitRepositoryImpl`（DAO 映射、Flow 组装）【必做】— 验收：编译通过（**已由任务 1.6 提前完成，无需重复执行**）✅
- [x] 2.2 :feature:home 页面：`HomeScreen` + `HomeViewModel`（StateFlow + 事件 Channel）+ 习惯列表 UI【必做】— 验收：模拟器显示列表（空态视图可接受）✅
- [x] 2.3 新建/编辑习惯弹窗（`HabitEditorDialog`）+ `HabitValidator` 校验接入【必做】— 验收：弹窗可打开、输入校验生效（空名称禁点保存）✅
- [x] 2.4 打卡/撤销交互：列表项 CheckBox → `onCheckIn/onCheckOut` → DAO 写库 → Flow 自动刷新【必做】— 验收：**M2 核心**：模拟器完成"建习惯 → 打卡 → 首页刷新显示已打卡"闭环 ✅
- [x] 2.5 导航：BottomBar（首页/统计/设置）+ Navigation Compose 路由（`currentBackStackEntryAsState` 同步选中态，见 TECH_DESIGN §4.5）【必做】— 验收：三 Tab 可切换不崩 ✅
- [x] 2.6 深色模式：设置页开关 → DataStore → Theme 动态切换【必做】— 验收：切换立即生效并持久化（重启保持）✅（2026-08-19 人工确认杀进程重启保持）
- [x] 2.7 首个 ViewModel 测试：`HomeViewModelTest`（MockK + MainDispatcherRule + Turbine，覆盖打卡成功/失败/校验拒绝）【必做】— 验收：`:feature:home:test` 全绿（8 用例，**兑现 1.10 的"被复用"验收**）✅
- [x] 2.8 首个 Room 真库测试：`HabitDaoTest`（androidTest，inMemory 库：插入/查询/级联删除）【必做】— 验收：`./gradlew :core:data:connectedDebugAndroidTest` 在模拟器上通过 ✅（5 用例，Pixel_8_API_35 实跑）
- [x] 2.9 首个 Compose UI Test：`HomeScreenTest`（渲染列表 + 点击打卡断言状态变化）【必做】— 验收：模拟器上通过 ✅（4 用例）
- [x] 2.10 GitHub Actions：`ci.yml`（PR 触发 testDebugUnitTest + lintDebug + assembleDebug）【必做】— 验收：push 到 GitHub 后 Actions 全绿 ✅（run 32251846306，2026-08-19 人工确认）
- [x] 2.11 `release.yml`（tag 触发 assembleRelease + 上传 APK 产物）【后补】— 验收：打一个 v0.1 tag 能出 APK ✅（v0.1.0-m2 tag 触发，run 32359573146 success，artifact habitflow-release 已上传；v1.3 注：`v*` 通配与既有 tag v0.1.0-m1/m2 命名兼容）
- [x] 2.12 集成 CI emulator job（android-emulator-runner 跑 connectedDebugAndroidTest）【后补】— 验收：PR 上 CI 含 instrumented job 全绿 ✅（**修复链六次尝试后达成**：chmod → @v2 → api-level 34 → script 预热 → UTP 禁用 → KVM 权限；run 32398123883 build + instrumented 双 job success，7m53s，2026-08-20；修复链完整演进见 TECH_DESIGN §9.1）

### 2.2 第 2 周产出物与验收总结【已达成】

- [x] M2 验收：模拟器上完整走通"创建习惯 → 打卡 → 撤销 → 首页显示已打卡状态刷新"（人工确认，2026-08-19）
- [x] M2 验收：`./gradlew testDebugUnitTest`（19 用例：Home 8 + Settings 2 + Streak 9）与 `./gradlew connectedDebugAndroidTest`（9 用例：HabitDao 5 + HomeScreen 4）全绿

产出：首个端到端 Demo（M2）+ 三个"第一"（ViewModel 单测、Room 真库测试、Compose UI Test）+ CI/Release 双链路实测全绿 + tag `v0.1.0-m2`。

- M2 里程碑验收证据：闭环回归人工验证 ✅（2026-08-19）、深色持久化 ✅、测试全绿 ✅、CI build/instrumented 全绿 ✅、Release 出包 ✅。
- v1.3 风险闭环：2.9 兼容性风险（BOM + API 34）已解决；CI emulator 太慢风险已解决（KVM 权限修复，7m53s）。

---

## 3. 第 3~4 周：核心功能开发（产出：M3）

### 3.1 任务清单

- [x] 3.1 习惯编辑页完整化：图标/颜色选择、频率（每日/每周 N 次）、目标量【必做】— 验收：编辑后数据正确落库、列表刷新（**实现+单测完成 2026-08-21，模拟器验收待复核**）
- [x] 3.2 习惯删除：长按 → 确认对话框 → 软删除（isArchived）+ 记录保留【必做】— 验收：删除后列表消失、统计仍计入历史（**实现+单测完成 2026-08-21，模拟器验收待复核；"统计计入历史"的数据层保留已保证，界面呈现随 3.4 统计页验证**）
- [ ] 3.3 首页今日视图完善：今日待打卡分组、已完成/未完成分区、今日连续展示【必做】— 验收：数据与打卡状态一致（**实现+单测完成 2026-08-21，模拟器验收待复核**）
- [ ] 3.4 :feature:stats 统计页：近 7/30 天完成率 + 最长连续 + 当前连续【必做】— 验收：与手工计算结果一致（用已知数据人工核对）（**实现+单测完成 2026-08-21，模拟器验收待复核**）
- [x] 3.5 热力图：Compose Canvas 手绘（周粒度颜色分级）【必做】— 验收：有数据的月份颜色正确分级（实现+单测完成 2026-08-21；**用户模拟器验收通过 2026-08-21**；分级口径：当日完成习惯数 0/1/2/3/≥4 五级）
- [ ] 3.6 StreakCalculator 补测：补卡/请假（excused）参数化用例【后补】— 验收：参数化测试全绿（**excused 语义已在 TECH_DESIGN §5.3 明确**）
- [ ] 3.7 每周 N 次型习惯：打卡校验（本周已达上限禁止再打）+ 统计适配【后补】— 验收：上限生效
- [ ] 3.8 本地通知提醒（WorkManager 每日定时 + POST_NOTIFICATIONS 权限）【后补】（建议直接裁剪，理由见 TECH_DESIGN §1.3 后置说明）— 验收：模拟器触发一次通知
- [x] 3.9 Repository 映射逻辑单测：`HabitRepositoryImplTest`（mock DAO，测 entity→model 映射与 Flow 组装）【必做】— 验收：`:core:data:test` 全绿（**实现 2026-08-21，`:core:data:testDebugUnitTest` 7 用例全绿 ✅**）
- [ ] 3.10 空态/错误态组件接入所有页面（Loading/Empty/Error 复用 :core:designsystem）【必做】— 验收：断库（无数据）时各页显示 Empty 而非白屏（**随 3.3/3.4 页面完成顺手做**）
- [ ] 3.11 代码规范自检：ktlint 或 detekt 接入（可选其一，跑通即可）【后补】— 验收：`./gradlew ktlintCheck` 通过

### 3.2 第 3~4 周产出物与验收总结

- [ ] M3 验收：模拟器完成一次"连续 3 天打卡 → 统计页显示连续 3 天 → 第 4 天不打卡 → 第 5 天打卡后连续显示 1"的完整验证（与 TECH_DESIGN §5.3 边界规则对齐）
- [ ] M3 验收：`./gradlew assembleDebug` + `testDebugUnitTest` 全绿

产出：功能冻结版（M3）——CRUD + 打卡 + Streak + 统计 + 深色模式全部可用。

- 风险点：3.5 热力图 Canvas 绘制可能超预期耗时——若超过 2 天未完成，简化为"7 天柱状图"（30 行代码），热力图移到后补清单；
- 功能冻结规则：第 4 周周日为功能冻结日，之后只修 bug 不加需求（此规则面试可讲，体现工程纪律）。

---

## 4. 第 5~6 周：测试加固 + 打磨（产出：质量版）

### 4.1 任务清单

- [ ] 4.1 测试覆盖率盘点：以"高价值逻辑"为准补齐——`HabitValidatorTest`、`StreakCalculatorTest` 参数化全量、`HomeViewModelTest` 覆盖取消/异常分支、`StatsViewModelTest`【必做】— 验收：`./gradlew test` 全模块全绿；核心类行覆盖 ≥ 80%（用 IntelliJ 自带覆盖率工具查看，本地指标）
- [ ] 4.2 DataStore 测试：`SettingsDataSourceTest`（TemporaryFolder 指定文件：写入 → 重建 → 读出）【必做】— 验收：通过
- [ ] 4.3 端到端手工回归清单：按 §3.2 验收脚本走一遍 + 深色模式 + 旋转屏幕状态保持【必做】— 验收：清单全过（清单存入仓库 `docs/regression-checklist.md`）
- [ ] 4.4 配置/视觉打磨：字体、间距、动效（Material3 默认即可）、图标替换【后补】— 验收：截图自评可接受
- [ ] 4.5 崩溃与异常收集：接入 Firebase Crashlytics 或 本地 log 落盘（二选一）【后补】— 验收：模拟器制造一次异常能看到记录
- [ ] 4.6 性能自查：列表滚动流畅度、数据库操作耗时【后补】— 验收：无卡顿；写操作 < 100ms
- [ ] 4.7 中文文案统一 + 无障碍语义（`contentDescription`、`testTag` 覆盖交互元素）【必做】— 验收：无障碍扫描无红色告警（**含 feature 占位 Screen 的硬编码中文迁移，见 TECH_DESIGN §11.8**）

### 4.2 第 5~6 周产出物与验收总结

- [ ] 验收：`./gradlew test` 全绿
- [ ] 验收：`connectedDebugAndroidTest` 全绿
- [ ] 验收：回归清单全部通过

产出：质量加固版——测试覆盖补齐、回归清单跑通、无障碍达标。

- 风险点：测试补强阶段最容易烂尾——务必遵守"先补高价值测试（4.1 中加粗部分），再做视觉打磨"的优先级，视觉打磨随时可砍。

---

## 5. 第 7~8 周：发布准备（产出：M4）

### 5.1 任务清单

- [ ] 5.1 Release 构建：`./gradlew assembleRelease`（AGP 9 `optimization { enable = true }` + **`proguard-rules.pro` 已补齐（TECH_DESIGN §3.5，2026-08-19 前置项完成，assembleRelease 实测出包）**）【必做】— 验收：出 APK；安装后核心流程可跑（重点验证 R8 未破坏 Room/Hilt）
- [ ] 5.2 混淆回归：release 包在模拟器跑一遍 §3.2 回归脚本【必做】— 验收：全过
- [ ] 5.3 版本号与签名：`versionCode/versionName` 规划、release 签名（keystore 纳入 gitignore）【必做】— 验收：签名包可安装
- [ ] 5.4 README：项目简介、架构图（模块依赖图）、测试截图、运行方式【必做】— 验收：一个陌生人按 README 能跑起来
- [ ] 5.5 简历素材整理：从 README + 本计划中提炼 3~5 条项目亮点（见 TECH_DESIGN §10 速查表）【必做】— 验收：每条含"业务问题→设计决策→结果"三段式
- [ ] 5.6 上架流程：注册开发者账号 → 上传 APK → 应用信息填写 → 过审【后补】— 验收：应用商店可见（或至少完成提审）
- [ ] 5.7 面试模拟：针对 §10 速查表逐条自问自答录音【后补】— 验收：每条能脱稿讲 2 分钟

### 5.2 第 7~8 周产出物与验收总结

- [ ] M4 验收：release APK 在模拟器全流程通过
- [ ] M4 验收：GitHub 仓库 `README` 展示完整
- [ ] M4 验收：`Actions` 面板全绿徽章

产出：M4——release 版 APK + README + 简历素材 + （可选）上架。

---

## 6. 必须做 / 可后补 总表（面试裁剪谈判用）

| 任务 | 标注 | 砍掉的影响 |
|---|---|---|
| 多模块拆分 | 【必做】 | 无模块化 = 本项目失去一半意义 |
| Kotlin + Compose 全量 | 【必做】 | 无法展示现代栈 |
| Hilt 全量接入 | 【必做】 | 面试被问 DI 无实例 |
| StreakCalculator + 边界单测 | 【必做】 | 测试故事无法展开（M1 已落地） |
| ViewModelTest（MockK+Turbine） | 【必做】 | 测试故事缩水（M2 已落地） |
| Room 真库测试 + Compose UI Test 各一个 | 【必做】 | "测试"只能讲单测，说服力降档（M2 已落地） |
| CI（单测+lint+编译+instrumented） | 【必做】 | 工程化链缺失一环（M2 已落地，双 job 全绿） |
| 打卡闭环 Demo（M2） | 【必做】 | 没有可演示的东西（已达成） |
| 统计页 + 热力图 | 【必做】 | 功能故事不完整（可降级为柱状图） |
| 本地通知提醒 | 【后补】 | 无影响（建议直接裁剪，见 3.8） |
| 每周 N 次型习惯 | 【后补】 | 无影响（每日型已覆盖核心）；WEEKLY 字段进 MVP 表结构（TECH_DESIGN §6.1），校验与统计后置 |
| 上架 | 【后补】 | 只影响"上架经验"这一条 JD 词 |
| Crashlytics | 【后补】 | 无影响 |
| 性能自查 | 【后补】 | 面试被问性能时用 DataCollector 的协议/缓存回答 |

---

## 7. 关键风险与预案

| 风险 | 概率 | 预案 |
|---|---|---|
| AGP 9 内置 Kotlin 与 KSP 不兼容（首次编译即失败） | 高 | 已解决（M1 落地）：`gradle.properties` 四标志（builtInKotlin=false / newDsl=false / r8.gradual.support / useUnifiedTestPlatform=false，TECH_DESIGN §3.4）；新增模块照 §3.3 模板执行不会再踩 |
| AGP 9 + KSP + Hilt 首编版本冲突 | 中 | 已解决（M1 验证通过）：KSP 2.3.11 与 Kotlin 松散绑定、Hilt 2.59.1 适配 AGP 9（TECH_DESIGN §2.3）；禁止自行升级 |
| 配置缓存（configuration-cache）与 KSP 兼容波动 | 中 | 首编报错时先 `--no-configuration-cache` 验证隔离问题（M1 已验证正常） |
| CI emulator job 失败（UTP 属性读取超时 / KVM 无权限） | 高→已解决 | **已解决（M2，修复链六步，TECH_DESIGN §9.1）**：KVM 权限 step + UTP 禁用 + api-level 34 + 预热；若 runner 环境再波动，优先走 `ci-debug/**` 分支验证 |
| 模板遗留代码清理遗漏 | 中 | 已解决（M1 完成 §11.2~§11.5 迁移）；§11.6 自检通过 |
| 功能战线失控 | 高 | 功能冻结日（第 4 周周日）硬性执行；落后即砍【后补】 |
| 测试补强烂尾 | 中 | 先补高价值测试再做视觉打磨；覆盖率以"核心类"计而非全量 |
| 上架流程受阻 | 中 | 第 7 周前不投入上架；受阻立即转"release APK + 自查报告" |
| KSP/serialization 与 AGP 10 兼容性（观察项） | 低 | AGP 10 将移除 builtInKotlin/newDsl/UTP 开关等逃生通道（AGP 9 警告已提示）；M4 前评估升级窗口，面试可讲"技术演进跟踪"（TECH_DESIGN §11.8 观察项） |

---

## 8. 与面试讲解的衔接（每周都做）

- [ ] 每周日晚花 30 分钟更新 README 的"本周做了什么"（一句话 + 一个截图）
- [ ] 每个里程碑完成时，按 TECH_DESIGN §10 速查表演练一遍对应问题
- 最终简历叙事："HabitFlow 是刻意按国内主流岗位技术栈搭建的练习项目，覆盖协程/Flow、MVVM+MVI、Hilt、多模块、测试与 CI 全链路；DataCollector 则是真实业务场景（工业数据采集）的完整实现，两者互补构成完整工程能力证明。"
