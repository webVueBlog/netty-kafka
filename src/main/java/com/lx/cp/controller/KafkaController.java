package com.lx.cp.controller;


import com.lx.cp.kafka.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class KafkaController {// 生产者
    @Autowired
    private KafkaSender kafkaSender;// 消费者
    @PostMapping("send")
    public String send(String msg,String topic){// 发送消息

        Assert.notNull(msg,"消息内容不能为空");// 校验消息内容不能为空

        kafkaSender.sendMessage(topic,msg);// 发送消息

        return "success";// 返回发送成功
    }
}
