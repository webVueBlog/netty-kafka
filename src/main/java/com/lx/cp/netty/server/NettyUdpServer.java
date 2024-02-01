package com.lx.cp.netty.server;

import com.lx.cp.netty.handler.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Slf4j
@Configuration
public class NettyUdpServer implements ApplicationListener<ApplicationStartedEvent> {

    @Value("${gps.netty.udp.port}")
    private int port; // UDP 服务器端口号

    @Resource
    private UdpServerHandler udpServerHandler; // UDP 服务器处理器

    private EventLoopGroup group = null; // 事件循环组，用于处理网络事件和 IO 操作

    /**
     * 启动 Netty UDP 服务器
     * @throws InterruptedException
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) { // 应用程序启动时执行
        try {
            Bootstrap b = new Bootstrap();// 创建了 Netty 的 Bootstrap 实例，用于配置和启动 UDP 服务。
            String osName= SystemPropertyUtil.get("os.name").toLowerCase(); // 获取操作系统的名称
            // 根据操作系统类型选择 EventLoopGroup 和 Channel 类型
            if ("linux".equals(osName)) {
                group = new EpollEventLoopGroup(); // 设置 EventLoopGroup，用于处理连接和 IO 操作。
                b.group(group)// 设置 Channel 类型为 EpollDatagramChannel，用于 UDP 连接
                        .channel(EpollDatagramChannel.class); // 设置 Channel 类型为 NioDatagramChannel，用于 UDP 连接
            } else {
                group = new NioEventLoopGroup(); // 设置 EventLoopGroup，用于处理连接和 IO 操作。
                b.group(group) // 设置 Channel 类型为 EpollDatagramChannel，用于 UDP 连接
                        .channel(NioDatagramChannel.class); // 设置 Channel 类型为 NioDatagramChannel，用于 UDP 连接
            }
            //广播
            // 设置 UDP 服务的配置选项
            b.option(ChannelOption.SO_BROADCAST, true)  // 开启广播
                    .option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 10)  // 设置接收缓存区大小为 10M
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024 * 10)  // 设置发送缓存区大小为 10M
                    .handler(udpServerHandler); // 设置 ChannelHandler，用于处理 UDP 消息和事件。

            ChannelFuture channelFuture = b.bind(port).sync(); // 绑定 UDP 服务端口，并启动服务
            if (channelFuture.isSuccess()) { // 判断服务是否成功启动
                log.info("UDP服务启动完毕,port={}", port); // 打印服务启动成功的信息
            }

        } catch (InterruptedException e) { // 处理异常
            log.info("UDP服务启动失败", e);
        }

    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        if(group!=null) {// 判断 EventLoopGroup 是否为空
            group.shutdownGracefully();// 关闭 EventLoopGroup，释放资源
        }
        log.info("UDP服务关闭成功");
    }
}
