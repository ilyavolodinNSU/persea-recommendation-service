package ru.persea.recommendationservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return config;
    }

    @Bean
    public ConsumerFactory<String, JsonNode> jsonNodeConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            baseConfig(),
            new StringDeserializer(),
            new JacksonJsonDeserializer<>(JsonNode.class, new JsonMapper())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JsonNode> jsonNodeKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, JsonNode>();
        factory.setConsumerFactory(jsonNodeConsumerFactory());
        return factory;
    }
}
