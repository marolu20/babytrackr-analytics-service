package com.babytrackr.analytics.controller.model.response

data class SleepBreakdown(
    val label: String,
    val totalSleepSessions: Int,
    val totalSleepMinutes: Int
)
