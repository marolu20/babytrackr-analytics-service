package com.babytrackr.analytics.domain

import com.babytrackr.analytics.domain.enums.Granularity
import com.babytrackr.analytics.domain.enums.Period

data class ReportParams(
    val period: Period,
    val granularity: Granularity
)
