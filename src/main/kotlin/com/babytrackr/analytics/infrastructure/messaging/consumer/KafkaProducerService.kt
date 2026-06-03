package com.babytrackr.analytics.infrastructure.messaging.consumer

import com.babytrackr.analytics.infrastructure.model.EventMessage
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumerService {
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)
    @KafkaListener(topics = ["baby-events"], groupId="babytrackr-analytics")

    fun consume(message: EventMessage) {
        try {
            logger.info("Consuming message: $message")
        } catch (e: Exception) {
            logger.error("Error consuming message ${e.message}")
        }
    }
}
