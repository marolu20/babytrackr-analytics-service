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
    name = "daily_sleep_summary",
    uniqueConstraints = [UniqueConstraint(name = "uk_sleep_summary_date", columnNames = ["babyId", "date"])]
    )
class DailySleepSummary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var babyId: Long,
    var date: LocalDate,
    var totalSleepSessions: Int = 0,
    var totalSleepMinutes: Int = 0
)
