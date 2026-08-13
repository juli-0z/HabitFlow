package cn.zjl.habitflow.network

/**
 * 统一响应包装（TECH_DESIGN_v1.1 §7.1）
 *
 * 参考国内后端常见 {code, message, data} 结构；sealed 保证调用方 when 穷举。
 * MVP 纯本地无真实后端，本类型为骨架预留（面试可讲"统一返回结构"）。
 */
sealed interface ApiResponse<out T> {

    data class Success<T>(val data: T) : ApiResponse<T>

    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>
}
