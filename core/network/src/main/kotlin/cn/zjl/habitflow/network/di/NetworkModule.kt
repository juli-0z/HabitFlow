package cn.zjl.habitflow.network.di

import cn.zjl.habitflow.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * 网络层装配（TECH_DESIGN_v1.1 §7.1）
 * OkHttpClient / Retrofit 应用级单例；拦截器与 RetrofitClient 由 @Inject 构造自动提供。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(retrofitClient: RetrofitClient): OkHttpClient =
        retrofitClient.buildOkHttpClient()

    @Provides
    @Singleton
    fun provideRetrofit(retrofitClient: RetrofitClient, okHttpClient: OkHttpClient): Retrofit =
        retrofitClient.buildRetrofit(okHttpClient)
}
