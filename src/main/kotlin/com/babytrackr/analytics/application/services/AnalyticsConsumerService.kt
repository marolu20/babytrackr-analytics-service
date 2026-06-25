package com.babytrackr.analytics.application.services

import com.babytrackr.analytics.domain.enums.DiaperType
import com.babytrackr.analytics.domain.enums.EventType
import com.babytrackr.analytics.infrastructure.model.EventMessage
import com.babytrackr.analytics.infrastructure.repositories.DailyDiaperSummary
import com.babytrackr.analytics.infrastructure.repositories.DailyDiaperSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DailyFeedSummary
import com.babytrackr.analytics.infrastructure.repositories.DailyFeedSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DailySleepSummary
import com.babytrackr.analytics.infrastructure.repositories.DailySleepSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DiaperEventRepository
import com.babytrackr.analytics.infrastructure.repositories.FeedEventRepository
import com.babytrackr.analytics.infrastructure.repositories.SleepEventRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
class AnalyticsConsumerService(
    private val dailySleepSummaryRepository: DailySleepSummaryRepository,
    private val dailyFeedSummaryRepository: DailyFeedSummaryRepository,
    private val dailyDiaperSummaryRepository: DailyDiaperSummaryRepository,
    private val feedEventRepository: FeedEventRepository,
    private val diaperEventRepository: DiaperEventRepository,
    private val sleepEventRepository: SleepEventRepository,
    private val eventTransformer: EventTransformer
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AnalyticsConsumerService::class.java)
    }

    fun processEvent(message: EventMessage) {
        when(message.eventType) {
            EventType.FEED -> processFeedEvent(message)
            EventType.DIAPER -> processDiaperEvent(message)
            EventType.SLEEP -> processSleepEvent(message)
            }
        }

    fun processFeedEvent(message: EventMessage) {
        val babyId = message.babyId
        val date = message.createdAt.atZone(ZoneId.of("UTC")).toLocalDate()

        // transform event message to FeedEvent
        val feedEvent = eventTransformer.toFeedEvent(message)

        // persist data in feed_events table
        feedEventRepository.save(feedEvent)

        // update summary daily_feed_summary table
        val summary = dailyFeedSummaryRepository.findByBabyIdAndDate(babyId,date)
        // if row doesn't exist, create a new row with babyId and date
            ?: DailyFeedSummary(
                babyId = babyId,
                date = date,
            )

        // updates the summary table using data from the transformed FeedEvent message
        summary.totalFeedings += 1
        summary.totalOunces += feedEvent.feedingAmountOz

        logger.info("Saving feed event for baby {}", babyId)

        dailyFeedSummaryRepository.save(summary)
    }
    fun processDiaperEvent(message: EventMessage) {
        val babyId = message.babyId
        val date = message.createdAt.atZone(ZoneId.of("UTC")).toLocalDate()

        val diaperEvent = eventTransformer.toDiaperEvent(message)

        val diaperType = diaperEvent.diaperType

        diaperEventRepository.save(diaperEvent)

        val summary = dailyDiaperSummaryRepository.findByBabyIdAndDate(babyId,date)
            ?: DailyDiaperSummary(
                babyId = babyId,
                date = date,
            )

        summary.totalDiaperChanges += 1

        when (diaperType) {
            DiaperType.WET -> summary.totalWetDiapers += 1
            DiaperType.SOLID -> summary.totalSolidDiapers += 1
            DiaperType.MIXED -> summary.totalMixedDiapers += 1
        }

        dailyDiaperSummaryRepository.save(summary)
    }
    fun processSleepEvent(message: EventMessage) {
        val babyId = message.babyId
        val date = message.createdAt.atZone(ZoneId.of("UTC")).toLocalDate()

        val sleepEvent = eventTransformer.toSleepEvent(message)

        sleepEventRepository.save(sleepEvent)

        val summary = dailySleepSummaryRepository.findByBabyIdAndDate(babyId,date)
            ?: DailySleepSummary(
                babyId = babyId,
                date = date,
            )

        summary.totalSleepSessions += 1
        summary.totalSleepMinutes += sleepEvent.sleepDurationMinutes

        dailySleepSummaryRepository.save(summary)
    }
}
