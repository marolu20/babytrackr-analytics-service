package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository

interface DiaperEventRepository: CrudRepository<DiaperEvent, Long> {
    fun findByEventId(eventId: Long): DiaperEvent?
}
