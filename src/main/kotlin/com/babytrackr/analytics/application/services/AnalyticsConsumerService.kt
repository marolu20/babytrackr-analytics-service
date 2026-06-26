package com.babytrackr.analytics.application.services

import com.babytrackr.analytics.domain.enums.DiaperType
import com.babytrackr.analytics.domain.enums.EventType
import com.babytrackr.analytics.domain.enums.OperationType
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
        // adding these here for now but this means I'll need to pass these as parameters right?
        val operationType = message.operationType
        val babyId = message.babyId
        val date = message.createdAt.atZone(ZoneId.of("UTC")).toLocalDate()

        when(message.eventType) {
            EventType.FEED -> processFeedEvent(message, babyId, date, operationType)
            EventType.DIAPER -> processDiaperEvent(message, babyId, date, operationType)
            EventType.SLEEP -> processSleepEvent(message, babyId, date, operationType)
            }
        }

    @Transactional
    fun processFeedEvent(message: EventMessage, babyId: Long, date: LocalDate, operationType: OperationType) {

        val summary = dailyFeedSummaryRepository.findByBabyIdAndDate(babyId,date)
            // if row doesn't exist, create a new row with babyId and date
            ?: DailyFeedSummary(
                babyId = babyId,
                date = date,
            )

        // updates the summary table using data from the transformed FeedEvent message
        when (operationType) {
           OperationType.CREATE -> {

               // transform event message to FeedEvent
               val feedEvent = eventTransformer.toFeedEvent(message)

               // persist data in feed_events table
               feedEventRepository.save(feedEvent)

               // update summary daily_feed_summary table
               summary.totalFeedings++
               summary.totalOunces += feedEvent.feedingAmountOz

               dailyFeedSummaryRepository.save(summary)
           }

           OperationType.UPDATE -> {

               val payload = eventTransformer.toFeedPayload(message)

               val newFeedingAmount = payload.feedingAmount

               val existingEvent = feedEventRepository.findByEventId(message.eventId)
                   ?: throw EntityNotFoundException("Cannot event ${message.eventId}")

               val previousFeedingAmount = existingEvent.feedingAmountOz

               val delta = (newFeedingAmount - previousFeedingAmount)

               existingEvent.feedingAmountOz = newFeedingAmount

               feedEventRepository.save(existingEvent)

               summary.totalOunces += delta

               dailyFeedSummaryRepository.save(summary)
           }

           OperationType.DELETE -> {
               val existingEvent = feedEventRepository.findByEventId(message.eventId)
                   ?: throw EntityNotFoundException("Cannot event ${message.eventId}")

               summary.totalFeedings--
               summary.totalOunces -= existingEvent.feedingAmountOz

               dailyFeedSummaryRepository.save(summary)

               feedEventRepository.delete(existingEvent)
           }
        }
    }

    @Transactional
    fun processDiaperEvent(message: EventMessage, babyId: Long, date: LocalDate, operationType: OperationType) {

        val summary = dailyDiaperSummaryRepository.findByBabyIdAndDate(babyId,date)
            ?: DailyDiaperSummary(
                babyId = babyId,
                date = date,
            )

        when (operationType) {
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
            }

            OperationType.UPDATE -> {
                val payload = eventTransformer.toDiaperPayload(message)

                val newDiaperType = payload.diaperType

                val existingEvent = diaperEventRepository.findByEventId(message.eventId)
                    ?: throw EntityNotFoundException("Cannot event ${message.eventId}")

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
            }

            OperationType.DELETE -> {
                val existingEvent = diaperEventRepository.findByEventId(message.eventId)
                    ?: throw EntityNotFoundException("Cannot event ${message.eventId}")

                val existingDiaperType = existingEvent.diaperType

                summary.totalDiaperChanges--
                when (existingDiaperType) {
                    DiaperType.WET -> summary.totalWetDiapers--
                    DiaperType.SOLID -> summary.totalSolidDiapers--
                    DiaperType.MIXED -> summary.totalMixedDiapers--
                }

                dailyDiaperSummaryRepository.save(summary)

                diaperEventRepository.delete(existingEvent)
            }
        }
    }

    @Transactional
    fun processSleepEvent(message: EventMessage, babyId: Long, date: LocalDate, operationType: OperationType) {

        val summary = dailySleepSummaryRepository.findByBabyIdAndDate(babyId,date)
            ?: DailySleepSummary(
                babyId = babyId,
                date = date,
            )
        when (operationType) {
            OperationType.CREATE -> {
                val sleepEvent = eventTransformer.toSleepEvent(message)

                sleepEventRepository.save(sleepEvent)

                summary.totalSleepSessions++
                summary.totalSleepMinutes += sleepEvent.sleepDurationMinutes

                dailySleepSummaryRepository.save(summary)
            }

            OperationType.UPDATE -> {
                val payload = eventTransformer.toSleepPayload(message)

                val newSleepDurationMinutes = payload.sleepDurationMin

                val existingEvent = sleepEventRepository.findByEventId(message.eventId)
                    ?: throw EntityNotFoundException("Cannot event ${message.eventId}")

                val previousSleepDurationMinutes = existingEvent.sleepDurationMinutes

                val delta = (newSleepDurationMinutes - previousSleepDurationMinutes)

                existingEvent.sleepDurationMinutes = newSleepDurationMinutes

                sleepEventRepository.save(existingEvent)

                summary.totalSleepMinutes += delta

                dailySleepSummaryRepository.save(summary)
            }

            OperationType.DELETE -> {

                val existingEvent = sleepEventRepository.findByEventId(message.eventId)
                    ?: throw EntityNotFoundException("Cannot event ${message.eventId}")

                summary.totalSleepSessions--
                summary.totalSleepMinutes -= existingEvent.sleepDurationMinutes

                dailySleepSummaryRepository.save(summary)
                sleepEventRepository.delete(existingEvent)
            }
        }
    }
}
