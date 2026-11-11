package com.babytrackr.analytics.domain

import java.time.LocalDate

data class SleepingReportBucket(
    val date: LocalDate,
    val totalSleepMinutes: Int,
    val totalSleepSessions: Int
)
