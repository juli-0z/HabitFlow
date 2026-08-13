package cn.zjl.habitflow.data.di

import cn.zjl.habitflow.data.repository.HabitRepository
import cn.zjl.habitflow.data.repository.HabitRepositoryImpl
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
}
