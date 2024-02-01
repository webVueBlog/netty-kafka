package com.lx.cp.netty.handler;

import com.lx.cp.kafka.KafkaSender;
import com.lx.cp.utils.MsgUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

/**
 * TCP业务处理handler
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class TcpServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

	@Autowired
	private KafkaSender kafkaSender;//kafka发送消息

	@Autowired
	@Qualifier("businessGroup")
	private EventExecutorGroup businessGroup;//业务线程池

    /**
     * 使用
     * @param ctx
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
		// 获取客户端发送的消息
		// 注意：ByteBuf 是 Netty 提供的处理字节数据的类，它是对 ByteBuffer 的封装，使用方便，自动管理内存，使用完毕会释放内存。
		// 读取消息内容
		// 注意：byteBuf.readableBytes() 方法用于获取当前 ByteBuf 中可读的字节数。
		// 注意：byteBuf.readBytes(byte[] dst) 方法用于将 ByteBuf 中的数据读取到指定的字节数组中。
		// 注意：byteBuf.toString(Charset) 方法用于将 ByteBuf 中的数据转换为字符串，
		// 其中 Charset 参数用于指定字符编码。
		String content = byteBuf.toString(StandardCharsets.UTF_8);

		log.info("TCP服务端接收到消息：{}",  content);

		// 处理业务逻辑
		// 注意：这里使用了业务线程池来处理业务逻辑，这样可以避免阻塞 Netty 的 IO 线程。
		// 注意：使用业务线程池需要先创建一个线程池，并将业务逻辑处理方法注册到线程池中。
		// 注意：这里使用的是 Netty 提供的 EventExecutorGroup 类，它是一个线程池的抽象，可以方便
		// 的创建和管理线程池。
		ByteBuf buf = Unpooled.copiedBuffer("TCP已经接收到消息：".getBytes(StandardCharsets.UTF_8));// 构造响应消息

		businessGroup.execute(()->{// 执行业务逻辑
			// 注意：这里使用了 Kafka 发送消息，可以将消息发送到指定的 Kafka 主题中。
			// 注意：KafkaSender 是一个用于发送消息到 Kafka 的工具类，需要先进行配置和初始
			// 化。
			// 注意：这里使用了 try-catch 块来捕获可能出现的异常，并在异常发生时返回一个错误响应给客户端
			// 注意：这里使用了 sendMessage 方法来发送消息到 Kafka，该方法需要传入消息的主题和消息的内容。
			// 注意：sendMessage 方法会返回一个 Future 对象，表示消息发送的结果。
			// 注意：可以使用 Future 对象来获取消息发送的状态和结果，并进行相应的处理。
			// 注意：如果消息发送失败，可以返回一个错误响应给客户端。
			try {
				kafkaSender.sendMessage("hello", content);// 发送消息到 Kafka
				send2client(ctx,buf.array());// 发送响应消息给客户端
			}catch(Throwable e) {
				log.error("TCP数据接收处理出错",e);
				ByteBuf err = Unpooled.copiedBuffer("系统错误：".getBytes(StandardCharsets.UTF_8));// 构造错误响应消息
				send2client(ctx,err.array());// 发送错误响应消息给客户端
			}
		});

    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {// 读取完成
        ctx.flush();// 将数据冲刷到远程节点，一般结合使用 @Sharable 注解
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {// 异常处理
    	log.error("TCP数据接收处理出错：",cause);// 记录日志
    }

    /**
     * 返回消息给客户端
     * @param ctx
     * @param msg
     */
    void send2client(ChannelHandlerContext ctx, byte[] msg) {// 发送消息给客户端
    	ByteBuf buf= Unpooled.buffer(msg.length+1);// 创建一个缓冲区
    	buf.writeBytes(msg);// 将消息写入缓冲区
    	buf.writeByte(MsgUtil.DELIMITER);// 在消息后面添加分隔符
    	ctx.writeAndFlush(buf).addListener(future->{// 异步发送消息，并添加监听器
    		if(!future.isSuccess()) {// 如果发送失败
				log.error("TCP发送给客户端消息失败");// 记录日志
    		}
    	});
    }
}
