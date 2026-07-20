package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface SleepEventRepository: CrudRepository<SleepEvent, Long> {
    fun findByEventId(eventId: Long): SleepEvent?

    fun findByBabyIdAndCreatedOnBetween(babyId: Long, start: Instant, end: Instant): List<SleepEvent>

    fun findTop10ByBabyIdOrderByCreatedOnDesc(
        babyId: Long
    ): List<SleepEvent>
}
