package com.babytrackr.analytics.controller.model

import java.time.Instant

data class QueryDateRange(
    val startDate: Instant,
    val endDate: Instant
)
