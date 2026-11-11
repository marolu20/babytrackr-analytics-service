package com.babytrackr.analytics.domain

import java.time.LocalDate

data class FeedingReportBucket(
    val date: LocalDate,
    val totalOunces: Double,
    val totalFeedings: Int
)
