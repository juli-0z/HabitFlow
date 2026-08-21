package cn.zjl.habitflow.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * 主线程替换规则（TECH_DESIGN_v1.1 §8.1，放 :core:testing）
 *
 * 单测中以 [StandardTestDispatcher] 替换 Dispatchers.Main：
 * - viewModelScope 等主线程协程在测试中确定性执行；
 * - 用法：`@get:Rule val mainDispatcherRule = MainDispatcherRule()`。
 */
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
