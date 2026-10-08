package com.family.dawa.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.family.dawa.domain.scheduler.IAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {
    private val alarmScheduler: IAlarmScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmScheduler.resync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class TimeChangeReceiver : BroadcastReceiver(), KoinComponent {
    private val alarmScheduler: IAlarmScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmScheduler.resync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
