package com.lx.cp.netty.server;

import com.lx.cp.netty.handler.TcpServerHandler;
import com.lx.cp.utils.MsgUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * netty服务（TCP）
 */
@Slf4j
@Component
public class NettyTcpServer implements ApplicationListener<ApplicationStartedEvent> {

    @Value("${gps.netty.tcp.port}")
    private int port;
    
    @Value("${gps.netty.tcp.read-timeout}")
    private int readTimeOut;// 读取超时时间

    @Autowired
    @Qualifier("bossGroup")
    private NioEventLoopGroup bossGroup;// 用于接受客户端的连接

    // 自动装配 NioEventLoopGroup，用于处理网络事件的工作线程组
    @Autowired
    @Qualifier("workerGroup")
    private NioEventLoopGroup workerGroup;

    // 自动装配 EventExecutorGroup，用于处理业务逻辑的线程组
    @Autowired
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;

    // 自动装配 TcpServerHandler，处理 TCP 服务端的业务逻辑
    @Autowired
    private TcpServerHandler tcpServerHandler;

    /**
     * 启动Server
     *
     */
    @Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
    	try {
	        ServerBootstrap serverBootstrap = new ServerBootstrap();// 创建ServerBootstrap实例
	        serverBootstrap.group(bossGroup, workerGroup)/* 设置主从Reactor线程组 */
	                .channel(NioServerSocketChannel.class)/* 使用NioServerSocketChannel作为服务器的通道实现 */
	                .childHandler(new ChannelInitializer<SocketChannel>() { /* 设置childHandler，创建通道时，向其pipeLine中添加自定义的handler逻辑 */
						@Override
						public void initChannel(SocketChannel ch) throws Exception {// 创建通道
							ch.pipeline().addLast(new IdleStateHandler(readTimeOut, 0, 0, TimeUnit.MINUTES));// 空闲状态检测
							// 1024表示单条消息的最大长度，解码器在查找分隔符的时候，达到该长度还没找到的话会抛异常
							ch.pipeline().addLast(// 添加自定义的解码器
									new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[] { MsgUtil.DELIMITER }),// 设置分隔符
											Unpooled.copiedBuffer(new byte[] { MsgUtil.DELIMITER, MsgUtil.DELIMITER })));// 设置分隔符
							ch.pipeline().addLast(businessGroup,tcpServerHandler);// 添加业务逻辑处理器
						}
					})
	                .option(ChannelOption.SO_BACKLOG, 1024) //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
	                .childOption(ChannelOption.TCP_NODELAY, true)//立即写出
	                .childOption(ChannelOption.SO_KEEPALIVE, true);//长连接
	        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();// 绑定端口，同步等待成功
	        if (channelFuture.isSuccess()) {// 绑定成功
				log.info("TCP服务启动完毕,port={}", port);// 打印日志
	        }
    	}catch(Exception e) {
			log.info("TCP服务启动失败", e);
    	}
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();// 关闭bossGroup
        workerGroup.shutdownGracefully().syncUninterruptibly();// 关闭workerGroup
        log.info("TCP服务关闭成功");
    }
}
