package cn.zjl.habitflow.network

import cn.zjl.habitflow.network.interceptor.AuthInterceptor
import cn.zjl.habitflow.network.interceptor.AuthRefreshInterceptor
import cn.zjl.habitflow.network.interceptor.LoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Retrofit/OkHttp 工厂（TECH_DESIGN_v1.1 §7.1）
 * - 连接超时 10s / 读取超时 30s；
 * - 拦截器链：Logging -> Auth -> AuthRefresh（顺序即执行序）；
 * - HttpLoggingInterceptor 仅 debug 生效（配合 BuildConfig.DEBUG）。
 */
class RetrofitClient @Inject constructor(
    private val loggingInterceptor: LoggingInterceptor,
    private val authInterceptor: AuthInterceptor,
    private val authRefreshInterceptor: AuthRefreshInterceptor,
) {

    fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(authRefreshInterceptor)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC),
            )
        }
        return builder.build()
    }

    fun buildRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)   // 占位：MVP 无真实后端，接入时替换
            .client(okHttpClient)
            .build()

    private companion object {
        const val CONNECT_TIMEOUT_SECONDS = 10L
        const val READ_TIMEOUT_SECONDS = 30L
        const val BASE_URL = "https://api.example.com/"
    }
}
