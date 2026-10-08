package com.family.dawa.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.family.dawa.DawaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? DawaApp ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.container.alarmSync.resync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? DawaApp ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.container.alarmSync.resync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
