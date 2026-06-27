package com.babytrackr.analytics.infrastructure.model

import com.babytrackr.analytics.domain.enums.DiaperType

data class DiaperPayload(
    val diaperType: DiaperType
)
