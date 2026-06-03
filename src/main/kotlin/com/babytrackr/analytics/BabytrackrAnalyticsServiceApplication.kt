package com.babytrackr.analytics

import com.babytrackr.analytics.infrastructure.config.properties.KafkaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties(KafkaProperties::class)
class BabytrackrAnalyticsServiceApplication

fun main(args: Array<String>) {
	runApplication<BabytrackrAnalyticsServiceApplication>(*args)
}
