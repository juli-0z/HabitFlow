package cn.zjl.habitflow.designsystem.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 业务 ViewModel 基类（TECH_DESIGN_v1.2 §4.4，脚手架必建）
 *
 * 只收敛两个横切点：统一错误兜底（[launchTask]）与事件通道（[events]）；
 * 不强制全部业务走 launchTask——需要精细错误处理的场景自行 try/catch。
 */
abstract class BaseViewModel : ViewModel() {
    // 事件通道统一在此定义（一次性事件，receiveAsFlow 保证每个事件只被消费一次）
    // 3.11：private backing property 满足 ktlint 规则；子类经 events 消费、错误处理走 launchTask
    private val _events = Channel<BaseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * 统一错误兜底：业务代码无需写 try/catch，异常经 [defaultErrorHandler] 收敛。
     * CancellationException 必须重新抛出，否则 viewModelScope 取消逻辑被破坏。
     */
    protected fun launchTask(
        block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError?.invoke(e) ?: defaultErrorHandler(e)
            }
        }
    }

    private fun defaultErrorHandler(e: Exception) {
        _events.trySend(BaseEvent.ShowError(e.toUserMessage()))
    }

    /** 子类发射一次性事件的标准封装（§4.4：事件通道集中在此）；trySend 满时不阻塞 */
    protected fun emitEvent(event: BaseEvent) {
        _events.trySend(event)
    }
}

/** 全局一次性事件（页面级事件继承扩展，如 HomeUiEvent : BaseEvent 或独立定义） */
sealed interface BaseEvent {
    data class ShowError(
        val message: String,
    ) : BaseEvent
}

/** 异常 -> 用户可读文案（兜底实现，业务可覆盖） */
fun Throwable.toUserMessage(): String = message ?: "未知错误"
