package com.babytrackr.analytics.controller.model.response

data class AggregatedReport<SummaryType, BucketType> (
    val overall: SummaryType,
    val buckets: List<BucketType>
)
