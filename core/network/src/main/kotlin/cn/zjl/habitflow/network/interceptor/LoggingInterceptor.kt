package cn.zjl.habitflow.network.interceptor

import cn.zjl.habitflow.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 调试日志拦截器（TECH_DESIGN_v1.1 §7.1）
 * debug 构建下打印请求/响应摘要；release 下直接放行（不泄露业务数据）。
 */
class LoggingInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (!BuildConfig.DEBUG) return chain.proceed(request)

            val started = System.currentTimeMillis()
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - started
            println(
                "[HTTP] ${request.method} ${request.url} -> ${response.code} (${duration}ms)",
            )
            return response
        }
    }
