package com.babytrackr.analytics.infrastructure.repositories

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "daily_diaper_summary",
    uniqueConstraints = [UniqueConstraint(name = "uk_diaper_summary_date", columnNames = ["babyId", "date"])]
)
class DailyDiaperSummary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var babyId: Long,
    var date: LocalDate,
    var totalDiaperChanges: Int = 0,
    var totalWetDiapers: Int = 0,
    var totalSolidDiapers: Int = 0,
    var totalMixedDiapers: Int = 0
)
