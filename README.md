# HabitFlow

> **本地优先（Local-first）的习惯打卡与数据统计工具 App**，面向国内 Android 初级/中级岗位求职练习。
> 项目定位与选型理由详见 [TECH_DESIGN_v1.1.md](TECH_DESIGN_v1.1.md) §1.1。

## 文档导航

| 文档 | 定位 | 适用读者 | 更新约定 |
|---|---|---|---|
| [TECH_DESIGN_v1.1.md](TECH_DESIGN_v1.1.md) | 技术设计：技术选型 / 模块架构 / 数据流 / 核心功能 / 数据层 / 网络层 / 测试策略 / CI/CD / 面试速查 | 自己（写代码时按章节查）、面试官（线性通读） | 低频更新；新增章节用 x.y 递进子编号或附录（§11+），不重排既有编号；版本基线表（§2.1）与模块登记表（§3.1）为唯一事实来源 |
| [DEVELOPMENT_PLAN_v1.1.md](DEVELOPMENT_PLAN_v1.1.md) | 开发计划：里程碑 M1~M4 / 周任务清单（checkbox）/ 必做后补总表 / 风险预案 | 自己（每周更新） | 高频更新；任务按稳定编号勾选（`- [x]`），不重编号；里程碑与任务编号保持稳定 |
| TECH_DESIGN.md / DEVELOPMENT_PLAN.md | 原始 v1.0 文档（存档） | — | **已冻结，不再更新**，内容以 v1.1 为准 |

## 当前工程状态

**状态：迁移进行中（尚未开始）**

- **现状**：仍为 Android Studio 模板工程——`:app` 单模块（`settings.gradle.kts` 仅 `include(":app")`），MainActivity 为传统 AppCompatActivity + ViewBinding，FirstFragment/SecondFragment + XML 导航（nav_graph.xml）；构建环境为 Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37 / Java 11；
- **目标架构**：10 个 Gradle 模块（6 core + 3 feature + 1 壳）的 Compose + Hilt + Room + DataStore 架构（模块清单见 TECH_DESIGN_v1.1.md §3.1）；
- **过渡步骤**：见 TECH_DESIGN_v1.1.md §11 附录《模板工程迁移清单》，自检项见 §11.6；
- **待决策项**：版本基线二选一——文档基线（Gradle 9.3.1 / AGP 9.1.0 / compileSdk 36）与模板基线（Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37）当前不一致，迁移第 0 步须统一（见 TECH_DESIGN_v1.1.md §2.1 与 §11.1）。

## 快速开始

```bash
# 构建 Debug 包（当前模板工程状态）
./gradlew assembleDebug

# 单元测试（纯 JVM，无需模拟器，CI 第一道门）
./gradlew testDebugUnitTest

# 仪器测试（Room 真库 + Compose UI Test，需模拟器/真机）
./gradlew connectedDebugAndroidTest
```

> 注意：当前为模板工程，以上命令验证的是模板状态；迁移后按 DEVELOPMENT_PLAN_v1.1.md 的里程碑逐项验证（M1：`assembleDebug` + `:core:domain:test` 全绿）。

## 文档维护约定

1. **编号稳定**：章节编号、模块编号（与 Gradle 模块名一一对应）、里程碑编号（M1~M4）与任务编号（1.1~5.7）一经发布不再重排，新增内容用递进子编号或追加附录；
2. **单一事实来源**：版本基线表（TECH_DESIGN §2.1）、模块登记表（§3.1）、必做/后补总表（DEVELOPMENT_PLAN §6）只在一处维护，其余位置仅引用、禁止另抄；
3. **提交纪律**：文档与代码同模块同提交（如 `feat: :core:data 实体落定 + 更新 TECH_DESIGN §6.1`），提交信息含文档章节号；
4. **拆分触发**：TECH_DESIGN 超过约 1200 行时按 §11.7 预案拆分；新增重量级主题文档时先登记到本文档导航表并声明读者与更新频率。
