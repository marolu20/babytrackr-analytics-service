package com.babytrackr.analytics.infrastructure.repositories

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "sleep_events")
class SleepEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var babyId: Long,
    var sleepDurationMinutes: Int = 0,
    var createdOn: Instant
)
