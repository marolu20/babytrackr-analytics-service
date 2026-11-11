package com.babytrackr.analytics.domain

data class DiaperReportSummary(
    val totalWetDiapers: Int,
    val totalSolidDiapers: Int,
    val totalMixedDiapers: Int,
    val totalDiaperChanges: Int,
)
