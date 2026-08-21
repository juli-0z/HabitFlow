package cn.zjl.habitflow.network

/**
 * Token 存储接口（TECH_DESIGN_v1.1 §7.1）
 * 依赖倒置：接口定义在 :core:network（消费方），实现由 DataStore 完成（:core:data），
 * 经 Hilt 注入进 network 模块。
 */
interface TokenStore {
    suspend fun getToken(): String?

    suspend fun saveToken(token: String)

    suspend fun clearToken()
}
