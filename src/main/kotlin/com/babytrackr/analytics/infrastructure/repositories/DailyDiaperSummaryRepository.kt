package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface DailyDiaperSummaryRepository: CrudRepository<DailyDiaperSummary, Long> {
    fun findByBabyIdAndDate(babyId: Long, date: LocalDate): DailyDiaperSummary?

    fun findByBabyIdAndDateBetween(
        babyId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<DailyDiaperSummary>
}
