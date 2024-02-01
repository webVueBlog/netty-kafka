package com.lx.cp.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息接收
 */
@Component
@Slf4j
public class KafkaConsumer {

    /**
     * containerFactory:定义批处理器，批处理消费的线程数由kafka.listener.concurrencys控制
     * topics：消费的消息队列的topic
     * @param records
     * @param ack
     */
    /**
     * 批量消费
     */
    @KafkaListener(containerFactory = "kafkaBatchListener",topics = { "hello" })
    public void batchListener1(List<ConsumerRecord<?,?>> records, Acknowledgment ack){//批量消费
        try {
            records.forEach(record -> {//循环处理每条消息
                //TODO - 处理消息
                log.info("receive {} msg:{}",record.topic(),record.value().toString());//打印消息

            });
        } catch (Exception e) {//异常处理
            //TODO - 消息处理异常操作
            log.error("kafka listen error:{}",e.getMessage());//打印异常信息
        } finally {
            //手动提交偏移量
            ack.acknowledge();//提交偏移量
        }
    }

    /**
     * containerFactory:定义批处理器，批处理消费的线程数由kafka.listener.concurrencys控制
     * topics：消费的消息队列的topic
     * @param records
     * @param ack
     */
    @KafkaListener(containerFactory = "kafkaBatchListener",topics = {"hello"})
    public void batchListener2(List<ConsumerRecord<?,?>> records, Acknowledgment ack){
        try {
            records.forEach(record -> {
                //TODO - 处理消息
                log.info("receive {} msg:{}",record.topic(),record.value().toString());
            });
        } catch (Exception e) {
            //TODO - 消息处理异常操作
            log.error("kafka listen error:{}",e.getMessage());
        } finally {
            //手动提交偏移量
            ack.acknowledge();
        }
    }
}
