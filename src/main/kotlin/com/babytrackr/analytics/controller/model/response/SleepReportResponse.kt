package com.babytrackr.analytics.controller.model.response

import com.babytrackr.analytics.controller.model.DateRange
import com.babytrackr.analytics.domain.enums.PeriodType

data class SleepReportResponse(
    val period: PeriodType,
    val range: DateRange,
    val totalSleepSessions: Int,
    val totalSleepMinutes: Int,
    val breakdown: List<SleepBreakdown>
)
