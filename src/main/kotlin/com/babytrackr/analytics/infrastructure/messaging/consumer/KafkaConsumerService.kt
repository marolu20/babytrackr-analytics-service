package com.babytrackr.analytics.infrastructure.messaging.consumer

import com.babytrackr.analytics.application.services.AnalyticsConsumerService
import com.babytrackr.analytics.infrastructure.model.EventMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumerService(
    private val analyticsConsumerService: AnalyticsConsumerService
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)
    @KafkaListener(
        topics = ["baby-events"],
        groupId="babytrackr-analytics",
        containerFactory = "kafkaListenerContainerFactory"
        )

    fun consume(message: EventMessage) {
        try {
            logger.info("Consuming message: $message")
            analyticsConsumerService.processEvent(message)
        } catch (e: Exception) {
            logger.error("Error consuming message ${e.message}")
        }
    }
}
