package com.lx.cp.controller;

import com.lx.cp.netty.handler.TcpServerHandler;
import com.lx.cp.utils.MsgUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class TcpClientController {

    @Value("${gps.netty.tcp.port}")
    private int port;

    @PostMapping("sendTcp")
    public String send(String msg){//发送tcp消息
        EventLoopGroup group = new NioEventLoopGroup();//创建EventLoopGroup
        Bootstrap b = new Bootstrap();//创建Bootstrap对象
        Channel ch =null;//创建Channel对象
        TcpServerHandler handler=new TcpServerHandler();//创建处理类对象

        try {
            b.group(group)//设置EventLoopGroup
                    .channel(NioSocketChannel.class)//使用NioSocketChannel来作为连接用的channel类
                    .handler(new ChannelInitializer<NioSocketChannel>() {//创建一个通道初始化对象
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {//添加我们自己的接收数据实现方法
                            ch.pipeline().addLast(//基于分隔符的解码器
                                    new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[] { 0x7e }),//以0x7e为分隔符
                                            Unpooled.copiedBuffer(new byte[] { 0x7e, 0x7e })));//以0x7e,0x7e为分隔符
                            ch.pipeline().addLast(handler);//添加我们的业务处理类
                        }});//设置channel的类型
            ch =b.connect("localhost", port).sync().channel();//连接到服务器
        } catch (Exception e) {//异常处理
            e.printStackTrace();
        }

        ByteBuf tcpMsg = Unpooled.copiedBuffer(msg.getBytes(StandardCharsets.UTF_8));//将字符串转换为字节数组

        ByteBuf buf= Unpooled.buffer(msg.length() + 1);//创建ByteBuf对象
        try{
            log.info("TCP客户端发送消息：{}", msg);//打印消息
            buf.writeBytes(tcpMsg);//消息体
            buf.writeByte(MsgUtil.DELIMITER);//消息分割符
            ch.writeAndFlush(buf).sync();//发送消息
        }catch (Exception e){
            log.info("TCP客户端发送消息失败：{}", e);
        }

        //关闭链接
        group.shutdownGracefully();
        return "success";
    }
}
