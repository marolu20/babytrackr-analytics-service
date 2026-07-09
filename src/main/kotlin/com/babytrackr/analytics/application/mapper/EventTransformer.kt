package com.babytrackr.analytics.application.mapper

import com.babytrackr.analytics.infrastructure.model.DiaperPayload
import com.babytrackr.analytics.infrastructure.model.EventMessage
import com.babytrackr.analytics.infrastructure.model.FeedPayload
import com.babytrackr.analytics.infrastructure.model.SleepPayload
import com.babytrackr.analytics.infrastructure.repositories.DiaperEvent
import com.babytrackr.analytics.infrastructure.repositories.FeedEvent
import com.babytrackr.analytics.infrastructure.repositories.SleepEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class EventTransformer(
    private val objectMapper: ObjectMapper
) {

    /*
    Converts the payload JSON into a FeedPayload object
     */
    fun toFeedPayload(message: EventMessage): FeedPayload {
        return objectMapper.readValue(
            message.payload,
            FeedPayload::class.java
        )
    }

    /*
    Converts the Kafka massage into a FeedEvent database entity record
     */
    fun toFeedEvent(message: EventMessage): FeedEvent {
        val payload = toFeedPayload(message)
        return FeedEvent(
            eventId = message.eventId,
            babyId = message.babyId,
            feedingAmountOz = payload.feedingAmount,
            createdOn = message.createdAt
        )
    }

    /*
    Converts the payload JSON into a SleepPayload object
     */
    fun toSleepPayload(message: EventMessage): SleepPayload {
        return objectMapper.readValue(
            message.payload,
            SleepPayload::class.java
        )
    }

    /*
    Converts the Kafka massage into a SleepEvent database entity record
     */
    fun toSleepEvent(message: EventMessage): SleepEvent {
        val payload = toSleepPayload(message)
        return SleepEvent(
            eventId = message.eventId,
            babyId = message.babyId,
            sleepDurationMinutes = payload.sleepDurationMin,
            createdOn = message.createdAt
        )
    }

    /*
    Converts the payload JSON into a DiaperPayload object
     */
    fun toDiaperPayload(message: EventMessage): DiaperPayload {
        return objectMapper.readValue(
            message.payload,
            DiaperPayload::class.java
        )
    }

    /*
    Converts the Kafka massage into a DiaperEvent database entity record
     */
    fun toDiaperEvent(message: EventMessage): DiaperEvent {
        val payload = toDiaperPayload(message)
        return DiaperEvent(
            eventId = message.eventId,
            babyId = message.babyId,
            diaperType = payload.diaperType,
            createdOn = message.createdAt
        )
    }

}
