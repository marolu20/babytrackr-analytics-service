package com.babytrackr.analytics.controller.model.response

import com.babytrackr.analytics.controller.model.DateRange
import com.babytrackr.analytics.domain.enums.PeriodType

class DiaperReportResponse(
    val period: PeriodType,
    val range: DateRange,
    val totalDiaperChanges: Int,
    val totalWetDiapers: Int,
    val totalSolidDiapers: Int,
    val totalMixedDiapers: Int,
    val breakdown: List<DiaperBreakdown>
)
