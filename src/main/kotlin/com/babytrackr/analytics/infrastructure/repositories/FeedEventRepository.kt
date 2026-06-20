package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedEventRepository: CrudRepository<FeedEvent, Long> {}
