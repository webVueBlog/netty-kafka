package com.lx.cp.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 生产者配置
 */
@Configuration
@EnableKafka
public class KafkaProducerConfig {

    @Value("${kafka.producer.bootstrap-servers}")
    private String bootstrapServers;//kafka地址

    @Value("${kafka.producer.retries}")
    private Integer retries;//重试次数

    @Value("${kafka.producer.batch-size}")
    private Integer batchSize;//批量大小

    @Value("${kafka.producer.buffer-memory}")
    private Integer bufferMemory;//缓存大小

    @Value("${kafka.producer.linger}")
    private Integer linger;//延迟时间


    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>(16);//定义kafka配置
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);//kafka地址
        props.put(ProducerConfig.RETRIES_CONFIG, retries);//重试次数
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);//批量大小
        props.put(ProducerConfig.LINGER_MS_CONFIG, linger);//延迟时间
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);//缓存大小
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);//序列化
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);//序列化

        return props;
    }

    private ProducerFactory<String, String> producerFactory() {//生产者工厂
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerConfigs());//默认生产者工厂
        return producerFactory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {//kafka模板
        return new KafkaTemplate<>(producerFactory());//默认kafka模板
    }

}
