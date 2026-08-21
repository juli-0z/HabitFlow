package cn.zjl.habitflow.data.di

import cn.zjl.habitflow.data.datasource.DataStoreTokenStore
import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.data.repository.HabitRepositoryImpl
import cn.zjl.habitflow.network.TokenStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository 绑定（TECH_DESIGN_v1.1 §6.3：@Binds 抽象绑定，面试讲"依赖倒置"）
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    /** TokenStore 依赖倒置绑定（§7.1）：接口在 :core:network，实现由 DataStore 完成 */
    @Binds
    abstract fun bindTokenStore(impl: DataStoreTokenStore): TokenStore
}
