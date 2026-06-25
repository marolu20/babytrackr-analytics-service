package com.babytrackr.analytics.application.services

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

    fun toFeedEvent(message: EventMessage): FeedEvent {

        val payload = objectMapper.readValue(
            message.payload,
            FeedPayload::class.java
        )

        return FeedEvent(
            babyId = message.babyId,
            feedingAmountOz = payload.feedingAmount,
            createdOn = message.createdAt
        )
    }

    fun toSleepEvent(message: EventMessage): SleepEvent {

        val payload = objectMapper.readValue(
            message.payload,
            SleepPayload::class.java
        )

        return SleepEvent(
            babyId = message.babyId,

            sleepDurationMinutes = payload.sleepDurationMinutes,
            createdOn = message.createdAt
        )
    }

    fun toDiaperEvent(message: EventMessage): DiaperEvent {

        val payload = objectMapper.readValue(
            message.payload,
            DiaperPayload::class.java
        )

        return DiaperEvent(
            babyId = message.babyId,
            diaperType = payload.diaperType,
            createdOn = message.createdAt
        )
    }

}
