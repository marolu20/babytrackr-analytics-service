package com.babytrackr.analytics.infrastructure.config

import com.babytrackr.analytics.infrastructure.config.properties.KafkaProperties
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import com.babytrackr.analytics.infrastructure.model.EventMessage
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
open class KafkaConsumerConfig(
    val kafkaProperties: KafkaProperties
) {

    @Bean
    open fun consumerFactory(): ConsumerFactory<Long, EventMessage> {

        val configProps: MutableMap<String, Any> = HashMap()

        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = "baby-event-consumer"
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java

        configProps[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = LongDeserializer::class.java
        configProps[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java

        configProps[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        configProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = "com.babytrackr.analytics.infrastructure.model.EventMessage"
        configProps[JsonDeserializer.TRUSTED_PACKAGES] = "com.babytrackr.analytics"

        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    open fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Long, EventMessage> {
        val factory = ConcurrentKafkaListenerContainerFactory<Long, EventMessage>()
        factory.setConsumerFactory(consumerFactory())
        factory.setConcurrency(1)
        return factory
    }
}

