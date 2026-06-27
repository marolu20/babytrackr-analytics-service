package com.babytrackr.analytics.application.services

import com.babytrackr.analytics.domain.enums.DiaperType
import com.babytrackr.analytics.domain.enums.EventType
import com.babytrackr.analytics.domain.enums.OperationType
import com.babytrackr.analytics.infrastructure.model.EventContext
import com.babytrackr.analytics.infrastructure.model.EventMessage
import com.babytrackr.analytics.infrastructure.repositories.DailyDiaperSummary
import com.babytrackr.analytics.infrastructure.repositories.DailyDiaperSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DailyFeedSummary
import com.babytrackr.analytics.infrastructure.repositories.DailyFeedSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DailySleepSummary
import com.babytrackr.analytics.infrastructure.repositories.DailySleepSummaryRepository
import com.babytrackr.analytics.infrastructure.repositories.DiaperEvent
import com.babytrackr.analytics.infrastructure.repositories.DiaperEventRepository
import com.babytrackr.analytics.infrastructure.repositories.FeedEvent
import com.babytrackr.analytics.infrastructure.repositories.FeedEventRepository
import com.babytrackr.analytics.infrastructure.repositories.SleepEvent
import com.babytrackr.analytics.infrastructure.repositories.SleepEventRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
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

        logger.info(
            "Processing {} event {}",
            message.operationType,
            message.eventId
        )

        when(message.eventType) {
            EventType.FEED -> processFeedEvent(message)
            EventType.DIAPER -> processDiaperEvent(message)
            EventType.SLEEP -> processSleepEvent(message)
            }
        }

    private fun getFeedSummary(babyId: Long, date:LocalDate): DailyFeedSummary {
        return dailyFeedSummaryRepository.findByBabyIdAndDate(babyId, date)
            ?: DailyFeedSummary(
                babyId = babyId,
                date = date,
            )
    }
    private fun getDiaperSummary(babyId: Long, date:LocalDate): DailyDiaperSummary {
        return dailyDiaperSummaryRepository.findByBabyIdAndDate(babyId, date)
            ?: DailyDiaperSummary(
                babyId = babyId,
                date = date,
            )
    }
    private fun getSleepSummary(babyId: Long, date:LocalDate): DailySleepSummary {
        return dailySleepSummaryRepository.findByBabyIdAndDate(babyId, date)
            ?: DailySleepSummary(
                babyId = babyId,
                date = date,
            )
    }

    private fun getFeedEventOrThrow(message: EventMessage): FeedEvent {
        return feedEventRepository.findByEventId(message.eventId)
            ?: throw EntityNotFoundException("Feed event ${message.eventId} not found.")
    }

    private fun getDiaperEventOrThrow(message: EventMessage): DiaperEvent {
        return diaperEventRepository.findByEventId(message.eventId)
            ?: throw EntityNotFoundException("Diaper event ${message.eventId} not found.")
    }

    private fun getSleepEventOrThrow(message: EventMessage): SleepEvent {
        return sleepEventRepository.findByEventId(message.eventId)
            ?: throw EntityNotFoundException("Sleep event ${message.eventId} not found.")
    }

    private fun getEventContext(message: EventMessage): EventContext {
        return EventContext(
            babyId = message.babyId,
            date = message.createdAt
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
        )
    }

    private fun logSummaryUpdate(
        type: EventType,
        babyId: Long
    ) {
        logger.info("Updated {} summary for baby {}", type, babyId)
    }

    private fun logDeleteMessage(
        type: EventType,
        message: EventMessage
    ) {
        logger.info("Deleting {} event {}", type, message.eventId)
    }


    @Transactional
    fun processFeedEvent(message: EventMessage) {
        val (babyId, date) = getEventContext(message)
        val summary = getFeedSummary(babyId, date)

        when (message.operationType) {
           OperationType.CREATE -> {

               val feedEvent = eventTransformer.toFeedEvent(message)

               feedEventRepository.save(feedEvent)

               summary.totalFeedings++
               summary.totalOunces += feedEvent.feedingAmountOz

               dailyFeedSummaryRepository.save(summary)

               logSummaryUpdate(EventType.FEED, babyId)
           }

           OperationType.UPDATE -> {

               val payload = eventTransformer.toFeedPayload(message)

               val newFeedingAmount = payload.feedingAmount

               val existingEvent = getFeedEventOrThrow(message)

               val previousFeedingAmount = existingEvent.feedingAmountOz

               val delta = (newFeedingAmount - previousFeedingAmount)

               existingEvent.feedingAmountOz = newFeedingAmount

               feedEventRepository.save(existingEvent)

               summary.totalOunces += delta

               dailyFeedSummaryRepository.save(summary)

               logSummaryUpdate(EventType.FEED, babyId)
           }

           OperationType.DELETE -> {
               val existingEvent = getFeedEventOrThrow(message)

               summary.totalFeedings--
               summary.totalOunces -= existingEvent.feedingAmountOz

               dailyFeedSummaryRepository.save(summary)

               logDeleteMessage(EventType.FEED, message)

               feedEventRepository.delete(existingEvent)

               logSummaryUpdate(EventType.FEED, babyId)
           }
        }
    }

    @Transactional
    fun processDiaperEvent(message: EventMessage) {

        val (babyId, date) = getEventContext(message)
        val summary = getDiaperSummary(babyId, date)

        when (message.operationType) {
            OperationType.CREATE -> {
                val diaperEvent = eventTransformer.toDiaperEvent(message)

                val diaperType = diaperEvent.diaperType

                diaperEventRepository.save(diaperEvent)

                summary.totalDiaperChanges++

                when (diaperType) {
                    DiaperType.WET -> summary.totalWetDiapers++
                    DiaperType.SOLID -> summary.totalSolidDiapers++
                    DiaperType.MIXED -> summary.totalMixedDiapers++
                }

                dailyDiaperSummaryRepository.save(summary)

                logSummaryUpdate(EventType.DIAPER, babyId)
            }

            OperationType.UPDATE -> {
                val payload = eventTransformer.toDiaperPayload(message)

                val newDiaperType = payload.diaperType

                val existingEvent = getDiaperEventOrThrow(message)

                val previousDiaperType = existingEvent.diaperType

                when (previousDiaperType) {
                    DiaperType.WET -> summary.totalWetDiapers--
                    DiaperType.SOLID -> summary.totalSolidDiapers--
                    DiaperType.MIXED -> summary.totalMixedDiapers--
                }

                when (newDiaperType) {
                    DiaperType.WET -> summary.totalWetDiapers++
                    DiaperType.SOLID -> summary.totalSolidDiapers++
                    DiaperType.MIXED -> summary.totalMixedDiapers++
                }

                existingEvent.diaperType = newDiaperType

                diaperEventRepository.save(existingEvent)

                dailyDiaperSummaryRepository.save(summary)

                logSummaryUpdate(EventType.DIAPER, babyId)
            }

            OperationType.DELETE -> {
                val existingEvent = getDiaperEventOrThrow(message)

                val existingDiaperType = existingEvent.diaperType

                summary.totalDiaperChanges--
                when (existingDiaperType) {
                    DiaperType.WET -> summary.totalWetDiapers--
                    DiaperType.SOLID -> summary.totalSolidDiapers--
                    DiaperType.MIXED -> summary.totalMixedDiapers--
                }

                dailyDiaperSummaryRepository.save(summary)

                logDeleteMessage(EventType.DIAPER, message)

                diaperEventRepository.delete(existingEvent)

                logSummaryUpdate(EventType.DIAPER, babyId)
            }
        }
    }

    @Transactional
    fun processSleepEvent(message: EventMessage) {
        val (babyId, date) = getEventContext(message)
        val summary = getSleepSummary(babyId, date)

        when (message.operationType) {
            OperationType.CREATE -> {
                val sleepEvent = eventTransformer.toSleepEvent(message)

                sleepEventRepository.save(sleepEvent)

                summary.totalSleepSessions++
                summary.totalSleepMinutes += sleepEvent.sleepDurationMinutes

                dailySleepSummaryRepository.save(summary)

                logSummaryUpdate(EventType.SLEEP, babyId)
            }

            OperationType.UPDATE -> {
                val payload = eventTransformer.toSleepPayload(message)

                val newSleepDurationMinutes = payload.sleepDurationMin

                val existingEvent = getSleepEventOrThrow(message)

                val previousSleepDurationMinutes = existingEvent.sleepDurationMinutes

                val delta = (newSleepDurationMinutes - previousSleepDurationMinutes)

                existingEvent.sleepDurationMinutes = newSleepDurationMinutes

                sleepEventRepository.save(existingEvent)

                summary.totalSleepMinutes += delta

                dailySleepSummaryRepository.save(summary)

                logSummaryUpdate(EventType.SLEEP, babyId)
            }

            OperationType.DELETE -> {

                val existingEvent = getSleepEventOrThrow(message)

                summary.totalSleepSessions--
                summary.totalSleepMinutes -= existingEvent.sleepDurationMinutes

                dailySleepSummaryRepository.save(summary)

                logDeleteMessage(EventType.DIAPER, message)

                sleepEventRepository.delete(existingEvent)

                logSummaryUpdate(EventType.SLEEP, babyId)
            }
        }
    }
}
