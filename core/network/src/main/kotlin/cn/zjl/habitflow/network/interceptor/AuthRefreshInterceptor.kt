package cn.zjl.habitflow.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 401 刷新重放拦截器（TECH_DESIGN_v1.1 §7.1，预留占位）
 *
 * 设计意图（注释清楚，面试可讲）：
 * 1. 请求返回 401 -> 调刷新接口换取新 token（TokenStore.saveToken）；
 * 2. 更新后重放原请求（带重试上限与并发去重）；
 * 3. 刷新失败则清理 token 并返回原 401 响应。
 * MVP 无真实后端，仅保留骨架与注释，逻辑后续接入后端时实现。
 */
class AuthRefreshInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            if (response.code == HTTP_UNAUTHORIZED) {
                // TODO: 预留——刷新 token 并重放原请求（MVP 不实现）
            }
            return response
        }

        private companion object {
            const val HTTP_UNAUTHORIZED = 401
        }
    }
