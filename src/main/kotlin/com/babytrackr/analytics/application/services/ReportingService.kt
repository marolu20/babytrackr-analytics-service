package com.babytrackr.analytics.application.services

import com.babytrackr.analytics.application.mapper.ResponseMapper
import com.babytrackr.analytics.controller.model.DateRange
import com.babytrackr.analytics.controller.model.DiaperData
import com.babytrackr.analytics.controller.model.DiaperTotals
import com.babytrackr.analytics.controller.model.FeedData
import com.babytrackr.analytics.controller.model.FeedTotals
import com.babytrackr.analytics.controller.model.QueryDateRange
import com.babytrackr.analytics.domain.ReportParams
import com.babytrackr.analytics.controller.model.SleepData
import com.babytrackr.analytics.controller.model.SleepTotals
import com.babytrackr.analytics.controller.model.response.AggregateDiaperBreakdown
import com.babytrackr.analytics.controller.model.response.AggregatedReport
import com.babytrackr.analytics.controller.model.response.DailyDiaperBreakdown
import com.babytrackr.analytics.controller.model.response.DashboardReportResponse
import com.babytrackr.analytics.controller.model.response.DiaperReportResponse
import com.babytrackr.analytics.controller.model.response.FeedBreakdown
import com.babytrackr.analytics.controller.model.response.FeedReportResponse
import com.babytrackr.analytics.controller.model.response.RecentActivitySummary
import com.babytrackr.analytics.controller.model.response.SleepBreakdown
import com.babytrackr.analytics.controller.model.response.SleepReportResponse
import com.babytrackr.analytics.domain.DiaperReportBucket
import com.babytrackr.analytics.domain.DiaperReportSummary
import com.babytrackr.analytics.domain.FeedingReportBucket
import com.babytrackr.analytics.domain.FeedingReportSummary
import com.babytrackr.analytics.domain.SleepingReportBucket
import com.babytrackr.analytics.domain.SleepingReportSummary
import com.babytrackr.analytics.domain.enums.EventType
import com.babytrackr.analytics.domain.enums.Granularity
import com.babytrackr.analytics.domain.enums.Period
import com.babytrackr.analytics.domain.enums.PeriodType
import com.babytrackr.analytics.domain.exception.UnsupportedReportParametersException
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

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
        private val supportedParams: Set<ReportParams> = setOf(
            ReportParams(Period.DAY, Granularity.DAY),
            ReportParams(Period.WEEK, Granularity.DAY),
            ReportParams(Period.WEEK, Granularity.WEEK),
            ReportParams(Period.MONTH, Granularity.DAY),
            ReportParams(Period.MONTH, Granularity.WEEK),
            ReportParams(Period.MONTH, Granularity.MONTH),
            ReportParams(Period.YEAR, Granularity.WEEK),
            ReportParams(Period.YEAR, Granularity.MONTH),
            ReportParams(Period.YEAR, Granularity.YEAR),
        )
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

    private fun getRecentActivity(babyId: Long): List<RecentActivitySummary> {
        val feedTop10 = feedEventRepository.findTop10ByBabyIdOrderByCreatedOnDesc(babyId)
        val sleepTop10 = sleepEventRepository.findTop10ByBabyIdOrderByCreatedOnDesc(babyId)
        val diaperTop10 = diaperEventRepository.findTop10ByBabyIdOrderByCreatedOnDesc(babyId)

        val feedRecentActivity = feedTop10.map { event ->
            RecentActivitySummary(
                eventType = EventType.FEED.toString().lowercase().replaceFirstChar { it.titlecase() },
                timestamp = event.createdOn,
                summary = "${event.feedingAmountOz.toString()} oz bottle"
            )
        }

        val sleepRecentActivity = sleepTop10.map { event ->
            RecentActivitySummary(
                eventType = EventType.SLEEP.toString().lowercase().replaceFirstChar { it.titlecase() },
                timestamp = event.createdOn,
                summary = "Slept for ${(event.sleepDurationMinutes.toDouble() / 60).toInt()}h ${event.sleepDurationMinutes % 60}m"

            )
        }

        val diaperRecentActivity = diaperTop10.map { event ->
            RecentActivitySummary(
                eventType = EventType.DIAPER.toString().lowercase().replaceFirstChar { it.titlecase() },
                timestamp = event.createdOn,
                summary = "${event.diaperType.toString().lowercase().replaceFirstChar { it.titlecase() }} Diaper"
            )
        }

        val recentActivity = feedRecentActivity + sleepRecentActivity + diaperRecentActivity


        val sortedActivity = recentActivity.sortedByDescending { it.timestamp }.take(5)

        return sortedActivity
    }

    fun getDashboard(babyId: Long): DashboardReportResponse {

        val today = LocalDate.now()

        val feedSummary = dailyFeedSummaryRepository.findByBabyIdAndDate(babyId,today)
        val sleepSummary = dailySleepSummaryRepository.findByBabyIdAndDate(babyId, today)
        val diaperSummary = dailyDiaperSummaryRepository.findByBabyIdAndDate(babyId, today)

        val recentActivity = getRecentActivity(babyId)

        val response = DashboardReportResponse(
            date = today,
            feed = feedSummary?.let {
                FeedData(
                    totalFeedings = it.totalFeedings,
                    totalOunces = it.totalOunces,
                )
            } ?: FeedData(
                totalFeedings = 0,
                totalOunces = 0.0
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
            ),
            recentActivity
        )
        logger.info("Dashboard report generated")
        return response
    }

    private fun validateReportParams(
        period: Period,
        granularity: Granularity
    ): Boolean {

        val incomingParams = ReportParams(
            period = period,
            granularity = granularity
        )

        return supportedParams.contains(incomingParams)

    }

    private fun calculateReportDateRange(
        period: Period,
    ): DateRange {
        // given a period calculates the date range
        val currentDate = LocalDate.now(ZoneOffset.UTC)

        println("Default timezone: ${ZoneId.systemDefault()}")
        println("Local date: ${LocalDate.now()}")
        println("UTC date: ${LocalDate.now(ZoneOffset.UTC)}")


        val startDate = when (period) {
            Period.DAY -> currentDate
            Period.WEEK -> currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            Period.MONTH -> currentDate.withDayOfMonth(1)
            Period.YEAR -> currentDate.withDayOfYear(1)
        }

        val dateRange = DateRange(
           startDate = startDate,
           endDate = currentDate
        )
        print("Date Range $dateRange")
        return dateRange
    }

    private fun aggregateFeedingReport(
        summary: List<DailyFeedSummary>,
        granularity: Granularity
    ): AggregatedReport<FeedingReportSummary, FeedingReportBucket> {

        val groupedSummary = summary.groupBy { item ->
            val date = item.date
             when (granularity) {
                Granularity.DAY -> date
                Granularity.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Granularity.MONTH -> date.withDayOfMonth(1)
                Granularity.YEAR -> date.withDayOfYear(1)
            }
        }

        val overallSummary = FeedingReportSummary(
            totalOunces = summary.sumOf { it.totalOunces },
            totalFeedings = summary.sumOf { it.totalFeedings }
        )

        val reportBuckets = groupedSummary.map { entry ->
            FeedingReportBucket(
                date = entry.key,
                totalOunces = entry.value.sumOf { it.totalOunces },
                totalFeedings = entry.value.sumOf { it.totalFeedings },
            )
        }.sortedBy { it.date }

        return AggregatedReport(
            overall = overallSummary,
            buckets = reportBuckets
        )
    }

    private fun aggregateSleepingReport(
        summary: List<DailySleepSummary>,
        granularity: Granularity
    ): AggregatedReport<SleepingReportSummary, SleepingReportBucket> {

        val groupedSummary = summary.groupBy { item ->
            val date = item.date
            when (granularity) {
                Granularity.DAY -> date
                Granularity.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Granularity.MONTH -> date.withDayOfMonth(1)
                Granularity.YEAR -> date.withDayOfYear(1)
            }
        }

        val overallSummary = SleepingReportSummary(
            totalSleepMinutes = summary.sumOf { it.totalSleepMinutes },
            totalSleepSessions = summary.sumOf { it.totalSleepSessions }
        )

        val reportBuckets = groupedSummary.map { entry ->
            SleepingReportBucket(
                date = entry.key,
                totalSleepMinutes = entry.value.sumOf { it.totalSleepMinutes },
                totalSleepSessions = entry.value.sumOf { it.totalSleepSessions },
            )
        }.sortedBy { it.date }

        return AggregatedReport(
            overall = overallSummary,
            buckets = reportBuckets
        )
    }

    private fun aggregateDiaperReport(
        summary: List<DailyDiaperSummary>,
        granularity: Granularity
    ): AggregatedReport<DiaperReportSummary, DiaperReportBucket> {

        val groupedSummary = summary.groupBy { item ->
            val date = item.date
            when (granularity) {
                Granularity.DAY -> date
                Granularity.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Granularity.MONTH -> date.withDayOfMonth(1)
                Granularity.YEAR -> date.withDayOfYear(1)
            }
        }

        val overallSummary = DiaperReportSummary(
            totalWetDiapers = summary.sumOf { it.totalWetDiapers },
            totalSolidDiapers = summary.sumOf { it.totalSolidDiapers },
            totalMixedDiapers = summary.sumOf { it.totalMixedDiapers },
            totalDiaperChanges = summary.sumOf { it.totalDiaperChanges }
        )

        val reportBuckets = groupedSummary.map { entry ->
            DiaperReportBucket(
                date = entry.key,
                totalWetDiapers = entry.value.sumOf { it.totalWetDiapers },
                totalSolidDiapers = entry.value.sumOf { it.totalSolidDiapers },
                totalMixedDiapers = entry.value.sumOf { it.totalMixedDiapers },
                totalDiaperChanges = entry.value.sumOf { it.totalDiaperChanges },
            )
        }.sortedBy { it.date }

        return AggregatedReport(
            overall = overallSummary,
            buckets = reportBuckets
        )
    }

    fun getFeedingReport(
        babyId: Long,
        period: Period,
        granularity: Granularity
    ): AggregatedReport<FeedingReportSummary, FeedingReportBucket> {

        // validate the api params
        val supported = validateReportParams(period, granularity)

        val response = if (supported) {

            val dateRange = calculateReportDateRange(period)
            println("Data Range HERE: $dateRange")

            val summaryResults = dailyFeedSummaryRepository.findByBabyIdAndDateBetween(
                babyId,
                dateRange.startDate,
                dateRange.endDate
            )
            println("Summary HERE: $summaryResults")
            summaryResults
        }
        else {
            throw UnsupportedReportParametersException("Period $period and Granularity $granularity parameters are not supported")
        }

        val results = aggregateFeedingReport(response, granularity)

        return results
    }


    fun getSleepReport(
        babyId: Long,
        period: Period,
        granularity: Granularity
    ): AggregatedReport<SleepingReportSummary, SleepingReportBucket> {

        // validate the api params
        val supported = validateReportParams(period, granularity)

        val response = if (supported) {

            val dateRange = calculateReportDateRange(period)
            println("Data Range: $dateRange")

            val summaryResults = dailySleepSummaryRepository.findByBabyIdAndDateBetween(
                babyId,
                dateRange.startDate,
                dateRange.endDate
            )
            println("Summary: $summaryResults")
            summaryResults
        }
        else {
            throw UnsupportedReportParametersException("Period $period and Granularity $granularity parameters are not supported")
        }

        val results = aggregateSleepingReport(response, granularity)

        return results
    }

    fun getDiaperReport(
        babyId: Long,
        period: Period,
        granularity: Granularity
    ): AggregatedReport<DiaperReportSummary, DiaperReportBucket> {

        // validate the api params
        val supported = validateReportParams(period, granularity)

        val response = if (supported) {

            val dateRange = calculateReportDateRange(period)
            println("Data Range: $dateRange")

            val summaryResults = dailyDiaperSummaryRepository.findByBabyIdAndDateBetween(
                babyId,
                dateRange.startDate,
                dateRange.endDate
            )
            println("Summary: $summaryResults")
            summaryResults
        }
        else {
            throw UnsupportedReportParametersException("Period $period and Granularity $granularity parameters are not supported")
        }

        val results = aggregateDiaperReport(response, granularity)

        return results
    }

}
