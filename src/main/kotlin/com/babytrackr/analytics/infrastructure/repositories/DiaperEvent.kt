package com.babytrackr.analytics.infrastructure.repositories

import com.babytrackr.analytics.domain.enums.DiaperType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "diaper_events")
class DiaperEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var babyId: Long,
    @Enumerated(EnumType.STRING)
    var diaperType: DiaperType,
    var createdOn: Instant
)
