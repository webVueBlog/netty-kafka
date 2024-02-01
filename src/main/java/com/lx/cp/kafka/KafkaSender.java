package com.lx.cp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * 消息发送
 */
@Component
@Slf4j
public class KafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;// KafkaTemplate<K, V>

    @Autowired
    public KafkaSender(KafkaTemplate<String, String> kafkaTemplate) {// 构造注入
        this.kafkaTemplate = kafkaTemplate;// 注入KafkaTemplate
    }

    public void sendMessage(String topic, String message) {// 发送消息
        log.info("Send msg:{}", message);

        ListenableFuture<SendResult<String, String>> sender = kafkaTemplate.send(new ProducerRecord<>(topic, message));// 发送消息


        sender.addCallback(// 发送成功的回调
                result -> log.info("Send success:offset({}),partition({}),topic({})",// 打印成功消息 发送成功：偏移量({}), 分区({}), 主题({})
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().topic()),
                ex -> log.error("Send fail:{}", ex.getMessage()));// 发送失败的回调
    }
}
