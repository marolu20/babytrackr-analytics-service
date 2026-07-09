package com.babytrackr.analytics.controller.model.response

import com.babytrackr.analytics.controller.model.DateRange
import com.babytrackr.analytics.domain.enums.PeriodType

data class FeedReportResponse(
    val period: PeriodType,
    val range: DateRange,
    val totalFeedings: Int,
    val totalOunces: Int,
    val breakdown: List<FeedBreakdown>
)
