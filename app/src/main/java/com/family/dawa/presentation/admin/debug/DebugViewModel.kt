package com.family.dawa.presentation.admin.debug

import androidx.lifecycle.viewModelScope
import com.family.dawa.core.base.MviViewModel
import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.domain.scheduler.IAlarmScheduler
import com.family.dawa.domain.usecase.debug.ResetTodayEventsUseCase
import com.family.dawa.domain.usecase.debug.SetDebugTimeOffsetUseCase
import kotlinx.coroutines.launch

class DebugViewModel(
    private val timeProvider: DebugTimeProvider,
    private val setDebugTimeOffsetUseCase: SetDebugTimeOffsetUseCase,
    private val resetTodayEventsUseCase: ResetTodayEventsUseCase,
    private val alarmScheduler: IAlarmScheduler
) : MviViewModel<DebugIntent, DebugState, DebugEffect>(
    DebugState(
        currentOffsetMs = timeProvider.offsetMillis,
        simulatedNow = timeProvider.nowZoned()
    )
) {

    override fun handleIntent(intent: DebugIntent) {
        when (intent) {
            is DebugIntent.FastForward -> {
                viewModelScope.launch {
                    val newOffset = state.value.currentOffsetMs + (intent.addedMinutes * 60 * 1000L)
                    setDebugTimeOffsetUseCase(newOffset)
                    updateState {
                        copy(
                            currentOffsetMs = newOffset,
                            simulatedNow = timeProvider.nowZoned(),
                            statusMessage = "تم تسريع الوقت بمقدار ${intent.addedMinutes} دقيقة"
                        )
                    }
                }
            }
            is DebugIntent.ResetOffset -> {
                viewModelScope.launch {
                    setDebugTimeOffsetUseCase(0L)
                    updateState {
                        copy(
                            currentOffsetMs = 0L,
                            simulatedNow = timeProvider.nowZoned(),
                            statusMessage = "تمت استعادة الوقت الفعلي للهاتف"
                        )
                    }
                }
            }
            is DebugIntent.TriggerTenSecondAlarm -> {
                alarmScheduler.scheduleTestAlarmInTenSeconds()
                updateState { copy(statusMessage = "تم ضبط المنبه بعد ١٠ ثوانٍ! اقفل الشاشة الآن للتجربة 🔔") }
            }
            is DebugIntent.ResetToday -> {
                viewModelScope.launch {
                    resetTodayEventsUseCase()
                    updateState { copy(statusMessage = "تم مسح سجلات اليوم والبدء من جديد ✔") }
                }
            }
        }
    }
}
