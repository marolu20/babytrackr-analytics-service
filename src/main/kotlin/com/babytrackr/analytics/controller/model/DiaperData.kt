package com.babytrackr.analytics.controller.model

data class DiaperData(
    val totalDiaperChanges: Int,
    val totalWetDiapers: Int,
    val totalSolidDiapers: Int,
    val totalMixedDiapers: Int,
)
