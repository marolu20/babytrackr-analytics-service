package com.babytrackr.analytics.application.services

import com.babytrackr.analytics.application.mapper.ResponseMapper
import com.babytrackr.analytics.controller.model.DateRange
import com.babytrackr.analytics.controller.model.DiaperData
import com.babytrackr.analytics.controller.model.DiaperTotals
import com.babytrackr.analytics.controller.model.FeedData
import com.babytrackr.analytics.controller.model.FeedTotals
import com.babytrackr.analytics.controller.model.QueryDateRange
import com.babytrackr.analytics.controller.model.SleepData
import com.babytrackr.analytics.controller.model.SleepTotals
import com.babytrackr.analytics.controller.model.response.AggregateDiaperBreakdown
import com.babytrackr.analytics.controller.model.response.DailyDiaperBreakdown
import com.babytrackr.analytics.controller.model.response.DashboardReportResponse
import com.babytrackr.analytics.controller.model.response.DiaperReportResponse
import com.babytrackr.analytics.controller.model.response.FeedBreakdown
import com.babytrackr.analytics.controller.model.response.FeedReportResponse
import com.babytrackr.analytics.controller.model.response.SleepBreakdown
import com.babytrackr.analytics.controller.model.response.SleepReportResponse
import com.babytrackr.analytics.domain.enums.PeriodType
import com.babytrackr.analytics.infrastructure.repositories.DailyDiaperSummary
import com.babytrackr.analytics.infrastructure.repositories.DailyDiaperSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DailyFeedSummary
import com.babytrackr.analytics.infrastructure.repositories.DailyFeedSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DailySleepSummary
import com.babytrackr.analytics.infrastructure.repositories.DailySleepSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DiaperEventRepository
import com.babytrackr.analytics.infrastructure.repositories.FeedEventRepository
import com.babytrackr.analytics.infrastructure.repositories.SleepEventRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class ReportingService(

    private val dailySleepSummaryRepository: DailySleepSummaryRepository,
    private val dailyFeedSummaryRepository: DailyFeedSummaryRepository,
    private val dailyDiaperSummaryRepository: DailyDiaperSummaryRepository,
    private val feedEventRepository: FeedEventRepository,
    private val diaperEventRepository: DiaperEventRepository,
    private val sleepEventRepository: SleepEventRepository,
    private val mapper: ResponseMapper
) {

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ReportingService::class.java)
        val TIMEFORMATTER = DateTimeFormatter.ofPattern("h:mm a")
    }

    private fun validateRequest(
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ) {
        when (period) {
            PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY -> {
                require(date != null) {
                    "Date must not be null for $period reports"
                }
            }
            PeriodType.CUSTOM -> {
                require(startDate != null) {
                    "startDate must not be null for CUSTOM reports"
                }
                require(endDate != null) {
                    "endDate must not be null for CUSTOM reports"
                }
            }
        }
    }

    private fun logDateRange(
        period: PeriodType,
        start: LocalDate,
        end: LocalDate
    ) {
        logger.info(
            "Date range for {} report: {} -> {}",
            period,
            start,
            end
        )
    }

    private fun calculateDateRange(
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): QueryDateRange {
        when (period) {
            PeriodType.DAILY -> {
                val safeDate = requireNotNull(date)
                val start = safeDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                val end = safeDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()

                logger.info(
                    "Query date range for DAILY report: {} -> {}",
                    start,
                    end
                )
                return QueryDateRange(
                    startDate = start,
                    endDate = end
                )
            }
            PeriodType.WEEKLY -> {
                val safeDate = requireNotNull(date)
                val start = safeDate.minusDays(6).atStartOfDay(ZoneOffset.UTC).toInstant()
                val end = safeDate

                logger.info(
                    "Query date range for WEEKLY report: {} -> {}",
                    start,
                    end
                )
                return QueryDateRange(
                    startDate = start,
                    endDate = end.plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                )
            }
            PeriodType.MONTHLY -> {
                val safeDate = requireNotNull(date)
                val start = safeDate.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                val end = safeDate.atStartOfDay(ZoneOffset.UTC).toInstant()

                logger.info(
                    "Query date range for MONTHLY report: {} -> {}",
                    start,
                    end
                )
                return QueryDateRange(
                    startDate = start,
                    endDate = end
                )
            }

            PeriodType.CUSTOM -> {
                val safeStartDate = requireNotNull(startDate)
                val safeEndDate = requireNotNull(endDate)

                logger.info(
                    "Query date range for CUSTOM report: {} -> {}",
                    safeStartDate,
                    safeStartDate
                )
                return QueryDateRange(
                    startDate = safeStartDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
                    endDate = safeEndDate
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                )
            }
        }
    }

    private fun createDateRange(
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): DateRange {

        return when (period) {

            PeriodType.DAILY -> {
            val safeDate = requireNotNull(date)

            logDateRange(period, safeDate, safeDate)
            DateRange(
                safeDate,
                safeDate
            )
        }

            PeriodType.WEEKLY -> {
                val safeDate = requireNotNull(date)
                val start = safeDate.minusDays(6)
                logDateRange(period, start, safeDate)
                DateRange(
                    start,
                    safeDate
                )
            }

            PeriodType.MONTHLY -> {
                val safeDate = requireNotNull(date)
                val start = safeDate.withDayOfMonth(1)
                logDateRange(period, start, safeDate)
                DateRange(
                    start,
                    safeDate
                )
            }

            PeriodType.CUSTOM -> {
                val safeStartDate = requireNotNull(startDate)
                val safeEndDate = requireNotNull(endDate)
                logDateRange(period, safeStartDate, safeEndDate)
                DateRange(
                    safeStartDate,
                    safeEndDate
                )
            }
        }
    }

    private fun calculateFeedTotals(summaries: List<DailyFeedSummary>): FeedTotals {
        val totalFeedings = summaries.sumOf { it.totalFeedings }

        val totalOunces = summaries.sumOf { it.totalOunces }

        logger.info(
            "Feed Totals: totalFeedings = {}, totalOunces = {}",
            totalFeedings,
            totalOunces
        )
        return FeedTotals(
            totalFeedings = totalFeedings,
            totalOunces = totalOunces
        )
    }

    private fun calculateSleepTotals(summaries: List<DailySleepSummary>): SleepTotals {
        val totalSleepSessions = summaries.sumOf { it.totalSleepSessions }

        val totalSleepMinutes = summaries.sumOf { it.totalSleepMinutes }

        logger.info(
            "Sleep Totals: totalSleepSessions = {}, totalSleepMinute = {}",
            totalSleepMinutes,
            totalSleepSessions
        )
        return SleepTotals(
            totalSleepSessions = totalSleepSessions,
            totalSleepMinutes = totalSleepMinutes
        )
    }

    private fun calculateDiaperTotals(summaries: List<DailyDiaperSummary>): DiaperTotals {
        val totalDiaperChanges = summaries.sumOf { it.totalDiaperChanges }
        val totalWetDiapers = summaries.sumOf { it.totalWetDiapers }
        val totalSolidDiapers = summaries.sumOf { it.totalSolidDiapers }
        val totalMixedDiapers = summaries.sumOf { it.totalMixedDiapers }

        logger.info("Diaper Totals: totalDiaperChanges = {}, totalWetDiapers = {}, totalSolidDiapers = {}, totalMixedDiapers = {}",
            totalDiaperChanges,
            totalWetDiapers,
            totalSolidDiapers,
            totalMixedDiapers
        )
        return DiaperTotals(
            totalDiaperChanges = totalDiaperChanges,
            totalWetDiapers = totalWetDiapers,
            totalSolidDiapers = totalSolidDiapers,
            totalMixedDiapers = totalMixedDiapers
        )
    }

    private fun createQueryRange(
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): DateRange {
        return if (period == PeriodType.CUSTOM) {
            val safeStartDate = requireNotNull(startDate)
            val safeEndDate = requireNotNull(endDate)
            createDateRange(period, null, safeStartDate, safeEndDate)
        } else {
            val safeDate = requireNotNull(date)
            createDateRange(period, safeDate, null, null)
        }
    }


    fun getFeedSummaryDetailReport(
        babyId: Long,
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): FeedReportResponse {

        validateRequest(period, date, startDate, endDate)

        val response = if (period == PeriodType.DAILY) {

            val safeDate = requireNotNull(date)

            val (start, end) = calculateDateRange(period, safeDate, null, null)

            val summary = dailyFeedSummaryRepository.findByBabyIdAndDate(babyId, safeDate)
                ?: throw EntityNotFoundException("Feed summary not found")

            val feedTotals = FeedTotals(
                totalFeedings = summary.totalFeedings,
                totalOunces = summary.totalOunces
            )

            val events = feedEventRepository.findByBabyIdAndCreatedOnBetween(babyId, start, end)

            val breakdown = events.map {
                FeedBreakdown(
                    label = TIMEFORMATTER.format(it.createdOn.atZone(ZoneOffset.UTC)),
                    totalFeedings = 1,
                    totalOunces = it.feedingAmountOz
                )
            }

            val responseDateRange = createDateRange(period, safeDate, startDate, endDate)

            mapper.toFeedReportResponse(period, responseDateRange, feedTotals, breakdown)

        } else {

            val (start, end) = createQueryRange(period, date, startDate, endDate)

            val summaries = dailyFeedSummaryRepository.findByBabyIdAndDateBetween(babyId, start, end)

            val feedTotals = calculateFeedTotals(summaries)

            val breakdown = summaries.map {
                FeedBreakdown(
                    label = it.date.toString(),
                    totalFeedings = it.totalFeedings,
                    totalOunces = it.totalOunces
                )
            }

            val responseDateRange = createDateRange(period, date, startDate, endDate)

            mapper.toFeedReportResponse(period, responseDateRange, feedTotals, breakdown)
        }
        logger.info("Feed summary detail report generated")
        return response
    }

    fun getSleepSummaryDetailReport(
        babyId: Long,
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): SleepReportResponse {

        validateRequest(period, date, startDate, endDate)

        val response = if (period == PeriodType.DAILY) {
            val safeDate = requireNotNull(date)

            val (start, end) = calculateDateRange(period, safeDate, startDate, endDate)

            val summary = dailySleepSummaryRepository.findByBabyIdAndDate(babyId, safeDate)
                ?: throw EntityNotFoundException("Sleep summary not found")

            val sleepTotals = SleepTotals(
                totalSleepSessions = summary.totalSleepSessions,
                totalSleepMinutes = summary.totalSleepMinutes
            )

            val events = sleepEventRepository.findByBabyIdAndCreatedOnBetween(babyId, start, end)

            val breakdown = events.map {
                SleepBreakdown(
                    label = TIMEFORMATTER.format(it.createdOn.atZone(ZoneOffset.UTC)),
                    totalSleepSessions = 1,
                    totalSleepMinutes = it.sleepDurationMinutes
                )
            }

            val responseDateRange = createDateRange(period, safeDate, startDate, endDate)

            mapper.toSleepReportResponse(period, responseDateRange, sleepTotals, breakdown)

        } else {

            val (start, end) = createQueryRange(period, date, startDate, endDate)

            val summaries = dailySleepSummaryRepository.findByBabyIdAndDateBetween(babyId, start, end)

            val sleepTotals = calculateSleepTotals(summaries)

            val breakdown = summaries.map {
                SleepBreakdown(
                    label = it.date.toString(),
                    totalSleepSessions = it.totalSleepSessions,
                    totalSleepMinutes = it.totalSleepMinutes
                )
            }

            val responseDateRange = createDateRange(period, date, startDate, endDate)

            mapper.toSleepReportResponse(period, responseDateRange, sleepTotals, breakdown)
        }

        logger.info("Sleep summary detail report generated")
        return response
    }

    fun getDiaperSummaryDetailReport(
        babyId: Long,
        period: PeriodType,
        date: LocalDate?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): DiaperReportResponse {

        validateRequest(period, date, startDate, endDate)

        val response = if (period == PeriodType.DAILY) {

            val safeDate = requireNotNull(date)

            val (start, end) = calculateDateRange(period, safeDate, startDate, endDate)

            val summary = dailyDiaperSummaryRepository.findByBabyIdAndDate(babyId, safeDate)
                ?: throw EntityNotFoundException("Diaper summary not found")

            val diaperTotals = DiaperTotals(
                totalDiaperChanges = summary.totalDiaperChanges,
                totalWetDiapers = summary.totalWetDiapers,
                totalSolidDiapers = summary.totalSolidDiapers,
                totalMixedDiapers = summary.totalMixedDiapers
            )

            val events = diaperEventRepository.findByBabyIdAndCreatedOnBetween(babyId, start, end)

            val breakdown = events.map {
                DailyDiaperBreakdown(
                    label = TIMEFORMATTER.format(it.createdOn.atZone(ZoneOffset.UTC)),
                    diaperType = it.diaperType
                )
            }

            val responseDateRange = createDateRange(period, date, startDate, endDate)

            mapper.toDiaperReportResponse(period, responseDateRange, diaperTotals, breakdown)
        } else {

            val (start, end) = createQueryRange(period, date, startDate, endDate)

            val summaries = dailyDiaperSummaryRepository.findByBabyIdAndDateBetween(babyId, start, end)

            val diaperTotals = calculateDiaperTotals(summaries)

            val breakdown = summaries.map {
                AggregateDiaperBreakdown(
                    label = it.date.toString(),
                    totalDiaperChanges = it.totalDiaperChanges,
                    totalWetDiapers = it.totalWetDiapers,
                    totalSolidDiapers = it.totalSolidDiapers,
                    totalMixedDiapers = it.totalMixedDiapers
                )
            }

            val responseDateRange = createDateRange(period, date, startDate, endDate)

            mapper.toDiaperReportResponse(period, responseDateRange, diaperTotals, breakdown)
        }
        logger.info("Diaper summary detail report generated")
        return response

    }

    fun getDashboard(babyId: Long): DashboardReportResponse {

        val today = LocalDate.now()

        val feedSummary = dailyFeedSummaryRepository.findByBabyIdAndDate(babyId,today)
        val sleepSummary = dailySleepSummaryRepository.findByBabyIdAndDate(babyId, today)
        val diaperSummary = dailyDiaperSummaryRepository.findByBabyIdAndDate(babyId, today)

        val response = DashboardReportResponse(
            date = today,
            feed = feedSummary?.let {
                FeedData(
                    totalFeedings = it.totalFeedings,
                    totalOunces = it.totalOunces,
                )
            } ?: FeedData(
                    totalFeedings = 0,
                    totalOunces = 0
            ),
            sleep = sleepSummary?.let {
                SleepData(
                totalSleepSessions = it.totalSleepSessions,
                totalSleepMinutes = it.totalSleepMinutes
                )
            } ?: SleepData(
                totalSleepSessions = 0,
                totalSleepMinutes = 0
            ),
            diaper = diaperSummary?.let {
                DiaperData(
                    totalDiaperChanges = it.totalDiaperChanges,
                    totalWetDiapers = it.totalWetDiapers,
                    totalSolidDiapers = it.totalSolidDiapers,
                    totalMixedDiapers = it.totalMixedDiapers
                )
            } ?: DiaperData(
                totalDiaperChanges = 0,
                totalWetDiapers = 0,
                totalSolidDiapers = 0,
                totalMixedDiapers = 0
            )
        )
        logger.info("Dashboard report generated")
        return response
    }
}


