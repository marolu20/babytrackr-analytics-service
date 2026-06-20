package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository

interface DailySleepSummaryRepository: CrudRepository<DailyFeedSummary, Long> {}
