package com.babytrackr.analytics.infrastructure.model

import java.time.LocalDate

data class EventContext (
    val babyId: Long,
    val date: LocalDate
)
