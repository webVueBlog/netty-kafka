package com.lx.cp.controller;

import com.lx.cp.netty.handler.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class UdpClientController {

    @Value("${gps.netty.udp.port}")
    private int port;

    @PostMapping("sendUdp")
    public String send(String msg){//发送udp消息
        EventLoopGroup group = new NioEventLoopGroup();//创建EventLoopGroup
        Bootstrap b = new Bootstrap();//创建Bootstrap对象
        Channel ch =null;//创建Channel对象
        UdpServerHandler handler=new UdpServerHandler();//创建处理器对象

        try {
            b.group(group)//设置EventLoopGroup
                    .channel(NioDatagramChannel.class)//设置通道类型
                    .option(ChannelOption.SO_BROADCAST, true)//设置选项
                    .handler(new ChannelInitializer<NioDatagramChannel>() {//设置处理器

                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {//初始化通道
                            ch.pipeline().addLast(handler.getClass().getSimpleName(),handler);//添加处理器
                        }});
            ch =b.bind(0).sync().channel();//绑定端口
        } catch (Exception e) {
            e.printStackTrace();//打印异常信息
        }

        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes(StandardCharsets.UTF_8));//创建ByteBuf对象

        try{
            log.info("UDP客户端发送消息：{}", msg);
            ch.writeAndFlush(new DatagramPacket(//创建DatagramPacket对象
                    Unpooled.copiedBuffer(buf.array()),//发送的数据
                    SocketUtils.socketAddress("localhost", port))).sync();//发送数据
        }catch (Exception e){
            log.info("UDP客户端发送消息失败：{}", e);
        }

        //关闭链接
        group.shutdownGracefully();
        return "success";
    }
}
