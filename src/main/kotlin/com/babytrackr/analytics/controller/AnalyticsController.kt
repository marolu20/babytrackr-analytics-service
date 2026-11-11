package com.babytrackr.analytics.controller

import com.babytrackr.analytics.application.services.ReportingService
import com.babytrackr.analytics.controller.model.response.AggregatedReport
import com.babytrackr.analytics.controller.model.response.DashboardReportResponse
import com.babytrackr.analytics.controller.model.response.DiaperReportResponse
import com.babytrackr.analytics.controller.model.response.FeedReportResponse
import com.babytrackr.analytics.controller.model.response.SleepReportResponse
import com.babytrackr.analytics.domain.DiaperReportBucket
import com.babytrackr.analytics.domain.DiaperReportSummary
import com.babytrackr.analytics.domain.FeedingReportBucket
import com.babytrackr.analytics.domain.FeedingReportSummary
import com.babytrackr.analytics.domain.SleepingReportBucket
import com.babytrackr.analytics.domain.SleepingReportSummary
import com.babytrackr.analytics.domain.enums.Granularity
import com.babytrackr.analytics.domain.enums.Period
import com.babytrackr.analytics.domain.enums.PeriodType
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@Validated
@RequestMapping("/v1/reports/babies")
class AnalyticsController(
    private val reportingService: ReportingService,
) {
    @GetMapping("/{babyId}/feed/summary-detail")
    fun getFeedSummary(
        @PathVariable babyId: Long,
        @RequestParam(required = true) period: PeriodType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): FeedReportResponse {

        return reportingService.getFeedSummaryDetailReport(
            babyId,
            period,
            date,
            startDate,
            endDate
        )
    }

    @GetMapping("/{babyId}/sleep/summary-detail")
    fun getSleepSummary(
        @PathVariable babyId: Long,
        @RequestParam(required = true) period: PeriodType,
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?
    ): SleepReportResponse {
        return reportingService.getSleepSummaryDetailReport(
            babyId,
            period,
            date,
            startDate,
            endDate
        )
    }

    @GetMapping("/{babyId}/diaper/summary-detail")
    fun getDiaperSummary(
        @PathVariable babyId: Long,
        @RequestParam(required = true) period: PeriodType,
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?
    ): DiaperReportResponse {
        return reportingService.getDiaperSummaryDetailReport(
            babyId,
            period,
            date,
            startDate,
            endDate
        )
    }

    @GetMapping("/{babyId}/dashboard")
    fun getDashboard(
        @PathVariable babyId: Long
    ): DashboardReportResponse {
        return reportingService.getDashboard(babyId)
    }

    @GetMapping("/{babyId}/feedings")
    fun getFeedingReport(
        @PathVariable babyId: Long,
        @RequestParam(required = true) period: Period,
        @RequestParam(required = true) granularity: Granularity,
    ): AggregatedReport<FeedingReportSummary, FeedingReportBucket> {
        return reportingService.getFeedingReport(babyId, period, granularity)
    }

    @GetMapping("/{babyId}/sleep")
    fun getSleepingReport(
        @PathVariable babyId: Long,
        @RequestParam(required = true) period: Period,
        @RequestParam(required = true) granularity: Granularity,
    ): AggregatedReport<SleepingReportSummary, SleepingReportBucket> {
        return reportingService.getSleepReport(babyId, period, granularity)
    }

    @GetMapping("/{babyId}/diapers")
    fun getDiaperReport(
        @PathVariable babyId: Long,
        @RequestParam(required = true) period: Period,
        @RequestParam(required = true) granularity: Granularity,
    ): AggregatedReport<DiaperReportSummary, DiaperReportBucket> {
        return reportingService.getDiaperReport(babyId, period, granularity)
    }
}
