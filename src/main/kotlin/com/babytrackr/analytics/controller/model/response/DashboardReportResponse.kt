package com.babytrackr.analytics.controller.model.response

import com.babytrackr.analytics.controller.model.DiaperData
import com.babytrackr.analytics.controller.model.FeedData
import com.babytrackr.analytics.controller.model.SleepData
import java.time.LocalDate

class DashboardReportResponse(
    val date: LocalDate,
    val feed: FeedData,
    val sleep: SleepData,
    val diaper: DiaperData,
)
