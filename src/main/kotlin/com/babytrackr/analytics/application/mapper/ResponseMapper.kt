package com.babytrackr.analytics.application.mapper

import com.babytrackr.analytics.controller.model.DateRange
import com.babytrackr.analytics.controller.model.DiaperTotals
import com.babytrackr.analytics.controller.model.FeedTotals
import com.babytrackr.analytics.controller.model.SleepTotals
import com.babytrackr.analytics.controller.model.response.DiaperBreakdown
import com.babytrackr.analytics.controller.model.response.DiaperReportResponse
import com.babytrackr.analytics.controller.model.response.FeedBreakdown
import com.babytrackr.analytics.controller.model.response.FeedReportResponse
import com.babytrackr.analytics.controller.model.response.SleepBreakdown
import com.babytrackr.analytics.controller.model.response.SleepReportResponse
import com.babytrackr.analytics.domain.enums.PeriodType
import org.springframework.stereotype.Component

@Component
class ResponseMapper {

    fun toFeedReportResponse(
        period: PeriodType,
        dateRange: DateRange,
        totals: FeedTotals,
        breakdown: List<FeedBreakdown>
    ): FeedReportResponse {
        return FeedReportResponse(
            period = period,
            range = dateRange,
            totalFeedings = totals.totalFeedings,
            totalOunces = totals.totalOunces,
            breakdown = breakdown
        )
    }

    fun toSleepReportResponse(
        period: PeriodType,
        dateRange: DateRange,
        totals: SleepTotals,
        breakdown: List<SleepBreakdown>
    ): SleepReportResponse {
        return SleepReportResponse(
            period = period,
            range = dateRange,
            totalSleepSessions = totals.totalSleepSessions,
            totalSleepMinutes = totals.totalSleepMinutes,
            breakdown = breakdown
        )
    }

    fun toDiaperReportResponse(
        period: PeriodType,
        dateRange: DateRange,
        totals: DiaperTotals,
        breakdown: List<DiaperBreakdown>
    ): DiaperReportResponse {

        return DiaperReportResponse(
            period = period,
            range = dateRange,
            totalDiaperChanges = totals.totalDiaperChanges,
            totalWetDiapers = totals.totalWetDiapers,
            totalSolidDiapers = totals.totalSolidDiapers,
            totalMixedDiapers = totals.totalMixedDiapers,
            breakdown = breakdown
        )
    }
}
