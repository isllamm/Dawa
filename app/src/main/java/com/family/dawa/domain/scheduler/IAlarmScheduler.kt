package com.family.dawa.domain.scheduler

interface IAlarmScheduler {
    suspend fun resync()
    fun scheduleTestAlarmInTenSeconds()
}
