package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface DiaperEventRepository: CrudRepository<DiaperEvent, Long> {
    fun findByEventId(eventId: Long): DiaperEvent?

    fun findByBabyIdAndCreatedOnBetween(babyId: Long, start: Instant, end: Instant): List<DiaperEvent>

    fun findTop10ByBabyIdOrderByCreatedOnDesc(
        babyId: Long
    ): List<DiaperEvent>
}
