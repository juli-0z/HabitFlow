# HabitFlow R8 混淆规则（TECH_DESIGN_v1.2 §3.5）
# 按 §3.5 原文落实，未额外补充：Hilt/Room 自带 consumer rules 已兜底（§3.5 声明），
# 避免冗余规则降低 R8 收益。

# Room 实体反射（:core:data 的 consumer rules 已兜底大部分，此处显式保留业务实体）
-keep class cn.zjl.habitflow.data.entity.** { *; }

# enum 在 R8 优化下可能被移除，Room 实体字段引用需显式保留（或用 @Keep 标注 Frequency）
-keep enum cn.zjl.habitflow.data.entity.Frequency { *; }
