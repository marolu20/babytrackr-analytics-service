package com.babytrackr.analytics.infrastructure.model

import com.babytrackr.analytics.domain.enums.EventType
import com.babytrackr.analytics.domain.enums.OperationType
import java.time.Instant

data class EventMessage(
    val eventId: Long,
    val babyId: Long,
    val userId: Long?,
    val eventType: EventType,
    val operationType: OperationType,
    val payload: String,
    val createdAt: Instant
)
