package com.babytrackr.analytics.controller

import com.babytrackr.analytics.application.services.ReportingService
import com.babytrackr.analytics.controller.model.response.DashboardReportResponse
import com.babytrackr.analytics.controller.model.response.DiaperReportResponse
import com.babytrackr.analytics.controller.model.response.FeedReportResponse
import com.babytrackr.analytics.controller.model.response.SleepReportResponse
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
}
