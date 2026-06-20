package com.babytrackr.analytics.infrastructure.repositories

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant

@Entity
@Table(name = "feed_events")
class FeedEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var babyId: Long,
    var feedingAmountOz: Int = 0,
    var createdOn: Instant
)
