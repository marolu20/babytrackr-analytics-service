package com.babytrackr.analytics.controller

import com.babytrackr.analytics.controller.error.ErrorCode
import com.babytrackr.analytics.controller.model.response.ApiError
import com.babytrackr.analytics.domain.exception.UnsupportedReportParametersException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedReportParametersException::class)
    fun handleUnsupportedParamsException(
        ex: UnsupportedReportParametersException,
    ): ResponseEntity<ApiError> {
        println("GLOBAL EXCEPTION HANDLER WAS CALLED")
        val response = buildError(
            code = ErrorCode.UNSUPPORTED_REPORT_PARAMS,
            message = ex.message ?: "Unsupported report parameters",
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response)
    }


        private fun buildError(
            code: ErrorCode,
            message: String?,
        ): ApiError {
            return ApiError(
                code = code,
                message = message,
            )
        }
}
