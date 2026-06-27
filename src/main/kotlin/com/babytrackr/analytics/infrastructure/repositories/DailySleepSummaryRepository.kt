package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface DailySleepSummaryRepository: CrudRepository<DailySleepSummary, Long> {
    fun findByBabyIdAndDate(babyId: Long, date: LocalDate): DailySleepSummary?
}
