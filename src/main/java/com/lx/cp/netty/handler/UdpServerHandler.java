package com.lx.cp.netty.handler;

import com.lx.cp.kafka.KafkaSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
@Component
@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Autowired
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;// 业务线程池

   @Autowired
   private KafkaSender kafkaSender;// kafka发送消息

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {// 读取数据
        String content = packet.content().toString(StandardCharsets.UTF_8);// 读取数据

        log.info("UDP服务端接收到消息：{}",  content);


        ByteBuf buf = Unpooled.copiedBuffer("UDP已经接收到消息：".getBytes(StandardCharsets.UTF_8));// 返回数据


        businessGroup.execute(()->{// 业务线程池处理
            try {
                kafkaSender.sendMessage("hello", content);// kafka发送消息
                ctx.writeAndFlush(new DatagramPacket(buf, packet.sender()));// 返回数据
            }catch(Throwable e) {
                log.info("UDP数据接收处理出错{}",  e);
            }
        });
    }

}
