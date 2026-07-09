package com.babytrackr.analytics.controller.model.response

import com.babytrackr.analytics.domain.enums.DiaperType

sealed interface DiaperBreakdown

data class DailyDiaperBreakdown(
    val label: String,
    val diaperType: DiaperType,
): DiaperBreakdown

data class AggregateDiaperBreakdown(
    val label: String,
    val totalDiaperChanges: Int,
    val totalWetDiapers: Int,
    val totalSolidDiapers: Int,
    val totalMixedDiapers: Int,
):DiaperBreakdown
