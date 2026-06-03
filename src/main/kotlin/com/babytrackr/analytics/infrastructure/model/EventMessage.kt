package com.babytrackr.analytics.infrastructure.model

import com.babytrackr.analytics.domain.enums.EventType
import java.time.Instant

data class EventMessage(
    val eventId: Long,
    val babyId: Long,
    val userId: Long?,
    val eventType: EventType,
    val payload: String,
    val createdAt: Instant
)
