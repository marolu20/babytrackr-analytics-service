package com.babytrackr.analytics.controller.model.response

import com.babytrackr.analytics.controller.error.ErrorCode

data class ApiError(
    val code: ErrorCode,
    val message: String?,
)
