# HabitFlow

> **本地优先（Local-first）的习惯打卡与数据统计工具 App**，面向国内 Android 初级/中级岗位求职练习。
> 刻意使用主流技术栈（Kotlin / Compose / 协程与 Flow / MVVM+MVI / Hilt / 多模块 / 测试 / CI）覆盖主流招聘要求。
> 项目定位与选型理由详见 [TECH_DESIGN_v1.3.md](TECH_DESIGN_v1.3.md) §1.1。

## 功能特性

- **习惯管理**：创建/编辑/删除习惯（名称、频率每日/每周、目标量、图标颜色），软删除保留统计数据
- **每日打卡**：勾选打卡、撤销打卡，WEEKLY 型每周上限校验
- **首页**：今日待打卡分组、已完成/未完成分区、今日连续展示
- **统计页**：近 7/30 天完成率、当前/最长连续（Streak）、打卡热力图（Canvas 手绘）
- **深色模式**：设置页开关，持久化（重启保持）
- **每日提醒**：WorkManager 定时通知（POST_NOTIFICATIONS 运行时权限）

## 技术架构

### 技术栈

| 层 | 技术 | 用途 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | 声明式 UI（全量） |
| 架构 | MVVM + MVI | StateFlow 状态 + Channel 事件，单向数据流 |
| 依赖注入 | Hilt | 全模块装配（含 @HiltWorker） |
| 本地持久化 | Room + DataStore | 业务数据（Room）+ 轻量偏好（DataStore） |
| 异步 | 协程 + Flow | 响应式数据流 |
| 后台任务 | WorkManager | 每日提醒调度 |
| 导航 | Navigation Compose | 类型安全路由 |
| 构建 | Gradle 9 + AGP 9 | Version Catalog + Kotlin DSL |
| 代码规范 | ktlint | 静态检查（CI 集成） |
| CI/CD | GitHub Actions | build + instrumented + Release 流水线 |

### 模块依赖图（10 模块：6 core + 3 feature + 1 壳，§3.1）

```
:app（壳）
 ├── :feature:home        首页（今日打卡/打卡/编辑删除）
 ├── :feature:stats       统计页（完成率/连续/热力图）
 └── :feature:settings    设置页（深色模式/提醒）

:feature:* ──→ :core:data ──→ :core:domain ──→ :core:model
         └──→ :core:designsystem（主题/三态组件）
:core:data ──→ :core:network（网络骨架）
:core:testing ←── 各模块 test 源码集（测试公用件，不进生产依赖）
```

> 依赖单向（feature → core，禁止反向）；按稳定度分层，按业务拆 feature。

### 架构模式

- **MVVM + MVI**：ViewModel 持 `StateFlow`（UI 只读）+ `Channel`（一次性事件）；UI 调用 `onXxx()` 意图；
- **单向数据流**：`Room → Repository → ViewModel(StateFlow) → Composable`，数据变更 UI 自动刷新；
- **BaseViewModel**：统一 `launchTask` 协程兜底 + 事件通道，子类不重复样板。

## 运行方式

### 环境要求

- JDK 17
- Android SDK（compileSdk 37 / minSdk 24）
- 模拟器或真机（仪器测试需要）

### 克隆与构建

```bash
git clone <repo-url> && cd HabitFlow

# 构建 Debug 包
./gradlew assembleDebug

# 构建 Release 签名包（需本地 keystore.properties，否则出 unsigned 包）
./gradlew :app:assembleRelease
```

### 测试命令

```bash
./gradlew testDebugUnitTest          # 单元测试（纯 JVM，无需模拟器，45 用例）
./gradlew connectedDebugAndroidTest  # 仪器测试（Room 真库 + Compose UI，需模拟器，16 用例）
./gradlew lintDebug                  # Android Lint
./gradlew ktlintCheck                # ktlint 代码规范
```

## 测试覆盖

| 类型 | 用例数 | 测试类 |
|---|---|---|
| 单元测试 | 45 | StreakCalculatorTest(9) + Streak 参数化(7) + HabitRepositoryImplTest(8) + HomeViewModelTest(19) + SettingsViewModelTest(6) + StatsViewModelTest(5) |
| 仪器测试 | 16 | HabitDaoTest(5, Room 真库) + HomeScreenTest(8) + StatsScreenTest(3) |
| **合计** | **61** | 全部 0 失败 |

> 测试分层：`test/` 纯 JVM 逻辑（快稳，CI 第一道门）；`androidTest/` Room 真库 + Compose 真实渲染。

## CI/CD（GitHub Actions）

- **build job**：testDebugUnitTest → lintDebug → ktlintCheck → assembleDebug（每次 push/PR 触发）；
- **instrumented job**：android-emulator-runner 跑 connectedDebugAndroidTest（KVM 权限已配置）；
- **Release**：`v*` tag 触发 assembleRelease → 上传 APK artifact。

> CI 修复链故事（排障经验）：instrumented 六步修复——`chmod +x gradlew` → `@v2` → `api-level 34` → 属性预热 → 禁用 UTP → KVM 权限，每步证据驱动。

## 里程碑进度

| 里程碑 | 状态 | tag |
|---|---|---|
| M1 脚手架 | ✅ 已达成 | `v0.1.0-m1` |
| M2 首个端到端 Demo | ✅ 已达成 | `v0.1.0-m2` |
| M3 核心功能冻结 | ✅ 已达成 | `v0.1.0-m3` |
| M4 发布准备 | 🔄 进行中 | — |

## 项目结构

```
HabitFlow/
├── app/                    # 壳模块（Application/MainActivity/NavGraph）
├── core/
│   ├── model/              # 纯业务模型
│   ├── domain/             # 纯逻辑（StreakCalculator/Validator）
│   ├── data/               # Room + DataStore + Repository
│   ├── network/            # 网络骨架
│   ├── designsystem/       # 主题/三态组件
│   └── testing/            # 测试公用件
├── feature/
│   ├── home/  stats/  settings/
├── docs/                   # 项目文档
├── gradle/libs.versions.toml   # 版本目录（唯一事实来源）
├── TECH_DESIGN_v1.3.md     # 技术设计（现行版）
└── DEVELOPMENT_PLAN_v1.3.md    # 开发计划（现行版）
```

**版本基线（已定版）**：Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37 / Kotlin 2.3.20 / KSP 2.3.11 / Hilt 2.59.1 / Room 2.8.4 / Compose BOM 2026.05.00 / desugar_jdk_libs 2.1.4 / Java 17（详见 TECH_DESIGN_v1.3.md §2.1）。

## 文档导航

| 文档 | 定位 | 适用读者 | 更新约定 |
|---|---|---|---|
| [TECH_DESIGN_v1.3.md](TECH_DESIGN_v1.3.md) | 技术设计（**现行版**）：选型 / 模块架构 / 数据流 / 功能 / 数据层 / 网络层 / 测试 / CI / 面试速查 | 自己、面试官 | 低频更新；新增章节用 x.y 递进子编号或附录（§11+），不重排既有编号；版本基线表（§2.1）与模块登记表（§3.1）为唯一事实来源 |
| [DEVELOPMENT_PLAN_v1.3.md](DEVELOPMENT_PLAN_v1.3.md) | 开发计划（**现行版**）：里程碑 M1~M4 / 周任务清单（checkbox）/ 必做后补总表 / 风险预案 | 自己（每周更新） | 高频更新；任务按稳定编号勾选（`- [x]`），不重编号 |
| [docs/resume-highlights.md](docs/resume-highlights.md) | 简历素材（5.5）：3~5 条项目亮点三段式提炼 | 自己（求职用） | 里程碑更新时同步 |
| TECH_DESIGN_v1.2.md / DEVELOPMENT_PLAN_v1.2.md | 历史版本（v1.2，冻结） | — | **已冻结**，仅追溯用；内容以 v1.3 为准 |
| TECH_DESIGN_v1.1.md / DEVELOPMENT_PLAN_v1.1.md | 历史版本（v1.1，冻结） | — | **已冻结**，仅追溯用；内容以 v1.3 为准 |
| TECH_DESIGN.md / DEVELOPMENT_PLAN.md | 原始 v1.0 文档（存档） | — | **已冻结**，不再更新 |

## 文档维护约定

1. **编号稳定**：章节编号、模块编号（与 Gradle 模块名一一对应）、里程碑编号（M1~M4）与任务编号（1.1~5.7）一经发布不再重排，新增内容用递进子编号或追加附录；
2. **单一事实来源**：版本基线表（TECH_DESIGN §2.1）、模块登记表（§3.1）、必做/后补总表（DEVELOPMENT_PLAN §6）只在一处维护，其余位置仅引用、禁止另抄；
3. **提交纪律**：文档与代码同模块同提交（如 `feat: :core:data 实体落定 + 更新 TECH_DESIGN §6.1`），提交信息含文档章节号；
4. **拆分触发**：TECH_DESIGN 超过约 1200 行时按 §11.7 预案拆分；新增重量级主题文档时先登记到本文档导航表并声明读者与更新频率；
5. **差异登记机制**（v1.2 新增）：编码实现与文档约定出现差异时，先在交付说明中登记"待回写清单"，随下一版本文档升级合并回写。
