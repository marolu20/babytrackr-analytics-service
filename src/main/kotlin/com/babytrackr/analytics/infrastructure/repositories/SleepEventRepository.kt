package com.babytrackr.analytics.infrastructure.repositories

import org.springframework.data.repository.CrudRepository

interface SleepEventRepository: CrudRepository<SleepEvent, Long> {}
