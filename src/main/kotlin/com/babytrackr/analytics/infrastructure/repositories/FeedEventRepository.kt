package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface FeedEventRepository: CrudRepository<FeedEvent, Long> {
    fun findByEventId(eventId: Long): FeedEvent?

    fun findByBabyIdAndCreatedOnBetween(babyId: Long, start: Instant, end: Instant): List<FeedEvent>
}
