# HabitFlow

> **本地优先（Local-first）的习惯打卡与数据统计工具 App**，面向国内 Android 初级/中级岗位求职练习。
> 项目定位与选型理由详见 [TECH_DESIGN_v1.3.md](TECH_DESIGN_v1.3.md) §1.1。

## 文档导航

| 文档 | 定位 | 适用读者 | 更新约定 |
|---|---|---|---|
| [TECH_DESIGN_v1.3.md](TECH_DESIGN_v1.3.md) | 技术设计（**现行版**）：选型 / 模块架构 / 数据流 / 功能 / 数据层 / 网络层 / 测试 / CI / 面试速查 | 自己、面试官 | 低频更新；新增章节用 x.y 递进子编号或附录（§11+），不重排既有编号；版本基线表（§2.1）与模块登记表（§3.1）为唯一事实来源 |
| [DEVELOPMENT_PLAN_v1.3.md](DEVELOPMENT_PLAN_v1.3.md) | 开发计划（**现行版**）：里程碑 M1~M4 / 周任务清单（checkbox）/ 必做后补总表 / 风险预案 | 自己（每周更新） | 高频更新；任务按稳定编号勾选（`- [x]`），不重编号 |
| TECH_DESIGN_v1.2.md / DEVELOPMENT_PLAN_v1.2.md | 历史版本（v1.2，冻结） | — | **已冻结**，仅追溯用；内容以 v1.3 为准 |
| TECH_DESIGN_v1.1.md / DEVELOPMENT_PLAN_v1.1.md | 历史版本（v1.1，冻结） | — | **已冻结**，仅追溯用；内容以 v1.3 为准 |
| TECH_DESIGN.md / DEVELOPMENT_PLAN.md | 原始 v1.0 文档（存档） | — | **已冻结**，不再更新 |

## 当前工程状态

**状态：M2 里程碑已达成（2026-08-19，tag `v0.1.0-m2`）**

- **已完成**：10 个 Gradle 模块（6 core + 3 feature + 1 壳）Compose + Hilt + Room + DataStore 架构；首个端到端 Demo（M2：建习惯 → 打卡 → 撤销 → 首页刷新闭环，人工验证通过）；深色模式持久化（重启保持）；单测 19 用例 + 仪器测试 9 用例全绿；GitHub Actions CI（build + instrumented 双 job）与 Release（tag 触发出包）双链路实测全绿；tag `v0.1.0-m1` / `v0.1.0-m2` 已打；
- **进行中**：M3（第 3~4 周）——核心功能冻结：3.1 习惯编辑页完整化起（编辑 → 删除 → 今日视图 → 统计页 → 热力图）；
- **版本基线（已定版，2026-08-13）**：Gradle 9.4.1 / AGP 9.2.1 / compileSdk 37 / Kotlin 2.3.20 / KSP 2.3.11 / Hilt 2.59.1 / Room 2.8.4 / Compose BOM 2026.05.00 / desugar_jdk_libs 2.1.4 / Java 17（详见 TECH_DESIGN_v1.3.md §2.1）。

## 快速开始

```bash
# 构建 Debug 包
./gradlew assembleDebug

# 单元测试（纯 JVM，无需模拟器，CI 第一道门）
./gradlew testDebugUnitTest

# 仪器测试（Room 真库 + Compose UI Test，需模拟器/真机）
./gradlew connectedDebugAndroidTest

# 查看里程碑基线
git tag -n
```

## 文档维护约定

1. **编号稳定**：章节编号、模块编号（与 Gradle 模块名一一对应）、里程碑编号（M1~M4）与任务编号（1.1~5.7）一经发布不再重排，新增内容用递进子编号或追加附录；
2. **单一事实来源**：版本基线表（TECH_DESIGN §2.1）、模块登记表（§3.1）、必做/后补总表（DEVELOPMENT_PLAN §6）只在一处维护，其余位置仅引用、禁止另抄；
3. **提交纪律**：文档与代码同模块同提交（如 `feat: :core:data 实体落定 + 更新 TECH_DESIGN §6.1`），提交信息含文档章节号；
4. **拆分触发**：TECH_DESIGN 超过约 1200 行时按 §11.7 预案拆分；新增重量级主题文档时先登记到本文档导航表并声明读者与更新频率；
5. **差异登记机制**（v1.2 新增）：编码实现与文档约定出现差异时，先在交付说明中登记"待回写清单"，随下一版本文档升级合并回写（v1.1→v1.2 已吸收 14 项差异，见 TECH_DESIGN_v1.2.md 修订记录）。
