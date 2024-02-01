package com.lx.cp.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 消费者配置
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;// 服务器地址

    @Value("${kafka.consumer.enable-auto-commit}")
    private Boolean autoCommit;// 是否自动提交

    @Value("${kafka.consumer.auto-commit-interval}")
    private Integer autoCommitInterval;// 自动提交时间间隔

    @Value("${kafka.consumer.max-poll-records}")
    private Integer maxPollRecords;// 每次拉取的最大记录数

    @Value("${kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;// 自动重置偏移量

    @Value("${kafka.listener.concurrencys}")
    private Integer concurrency;// 消费者数量

    @Value("${kafka.listener.poll-timeout}")
    private Long pollTimeout;// 拉取超时时间

    @Value("${kafka.consumer.session-timeout}")
    private String sessionTimeout;// 会话超时时间

    @Value("${kafka.listener.batch-listener}")
    private Boolean batchListener;// 是否批量监听

    @Value("${kafka.consumer.max-poll-interval}")
    private Integer maxPollInterval;// 最大拉取间隔

    @Value("${kafka.consumer.max-partition-fetch-bytes}")
    private Integer maxPartitionFetchBytes;// 每个分区的最大获取字节数

    @Value("${kafka.consumer.group-id}")
    private String groupId;// 消费者组ID


    private Map<String, Object> consumerConfigs() {// 配置消费者属性
        Map<String, Object> props = new HashMap<>(20);// 初始化属性
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);// 自动提交时间间隔
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);// 服务器地址
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);// 是否自动提交
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);// 每次拉取的最大记录数
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);// 自动重置偏移量
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);// 会话超时时间
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollInterval);// 最大拉取间隔
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);// 每个分区的最大获取字节数
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);// 键的反序列化类
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);// 值的反序列化类
        props.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);// 消费者组ID

        return props;// 返回属性
    }

    /**
     *  创建批量监听容器工厂
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "kafkaBatchListener")// 如果容器中不存在名为"kafkaBatchListener"的bean，则创建一个新的bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaBatchListener() {// 创建一个新的KafkaListenerContainerFactory
        ConcurrentKafkaListenerContainerFactory<String, String> factory = kafkaListenerContainerFactory();// 调用kafkaListenerContainerFactory方法创建一个容器工厂
        factory.setConcurrency(concurrency);// 设置并发数
        return factory;// 返回容器工厂
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {// 创建一个新的KafkaListenerContainerFactory
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();// 创建一个新的容器工厂
        factory.setConsumerFactory(consumerFactory());// 设置消费者工厂
        //批量消费
        factory.setBatchListener(batchListener);// 设置批量监听
        //如果消息队列中没有消息，等待timeout毫秒后，调用poll()方法。
        // 如果队列中有消息，立即消费消息，每次消费的消息的多少可以通过max.poll.records配置。
        //手动提交无需配置
        factory.getContainerProperties().setPollTimeout(pollTimeout);// 如果消息队列中没有消息，等待timeout毫秒后，调用poll()方法。
        //设置提交偏移量的方式， MANUAL_IMMEDIATE 表示消费一条提交一次；MANUAL表示批量提交一次
        //低版本的spring-kafka，ackMode需要引入AbstractMessageListenerContainer.AckMode.MANUAL
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);// 设置提交偏移量的方式， MANUAL_IMMEDIATE 表示消费一条提交一次；MANUAL表示批量提交一次
        return factory;// 返回容器工厂
    }

    private ConsumerFactory<String, String> consumerFactory() {// 创建一个新的消费者工厂
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());// 返回一个默认的消费者工厂
    }
}
