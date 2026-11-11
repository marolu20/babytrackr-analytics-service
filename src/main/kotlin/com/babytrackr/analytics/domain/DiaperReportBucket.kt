package com.babytrackr.analytics.domain

import java.time.LocalDate

data class DiaperReportBucket(
    val date: LocalDate,
    val totalWetDiapers: Int,
    val totalSolidDiapers: Int,
    val totalMixedDiapers: Int,
    val totalDiaperChanges: Int,
)
