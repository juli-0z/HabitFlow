package cn.zjl.habitflow.network.interceptor

import cn.zjl.habitflow.network.TokenStore
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 鉴权拦截器（TECH_DESIGN_v1.1 §7.1）
 *
 * 依赖倒置：仅依赖 [TokenStore] 接口（定义于 :core:network），
 * 实现（DataStoreTokenStore）由 Hilt 在运行时注入，编译期 network 不依赖 data。
 * MVP 无真实后端，token 注入为预留占位。
 */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // OkHttp 拦截器为同步 API，TokenStore 为 suspend -> runBlocking（网络层标准妥协）
        val token = runBlocking { tokenStore.getToken() }
        val authorizedRequest = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(authorizedRequest)
    }
}
