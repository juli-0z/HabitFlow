package cn.zjl.habitflow.domain

import cn.zjl.habitflow.model.Frequency

/**
 * 习惯表单校验（TECH_DESIGN_v1.1 §5.1）——新建与编辑共用同一校验，
 * 位于 :core:domain 纯逻辑层，直接可单测。
 */
object HabitValidator {
    private const val MAX_NAME_LENGTH = 30

    fun validate(
        name: String,
        frequency: Frequency,
        targetPerWeek: Int,
    ): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return ValidationResult.Failure("习惯名称不能为空")
        if (trimmed.length > MAX_NAME_LENGTH) {
            return ValidationResult.Failure("习惯名称不能超过 $MAX_NAME_LENGTH 个字符")
        }
        // WEEKLY 时 targetPerWeek 必须落在 1~7（一周最多 7 次）；DAILY 时忽略该字段
        if (frequency == Frequency.WEEKLY && (targetPerWeek < 1 || targetPerWeek > 7)) {
            return ValidationResult.Failure("每周目标次数需在 1~7 之间")
        }
        return ValidationResult.Success
    }
}

/** 校验结果（sealed，调用方 when 穷举） */
sealed interface ValidationResult {
    data object Success : ValidationResult

    data class Failure(
        val message: String,
    ) : ValidationResult
}
