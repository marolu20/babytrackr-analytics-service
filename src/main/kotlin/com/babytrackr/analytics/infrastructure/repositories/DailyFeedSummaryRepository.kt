package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface DailyFeedSummaryRepository: CrudRepository<DailyFeedSummary, Long> {
    fun findByBabyIdAndDate(babyId: Long, date: LocalDate): DailyFeedSummary?
}
