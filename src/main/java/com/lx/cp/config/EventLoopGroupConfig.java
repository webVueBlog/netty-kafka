package com.lx.cp.config;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * netty线程组
 */
@Configuration
public class EventLoopGroupConfig {

    @Value("${gps.netty.threads.boss}")
    private int bossNum;// 主线程

    @Value("${gps.netty.threads.worker}")
    private int workerNum;// 工作线程

    @Value("${gps.netty.threads.business.num}")
    private int businessNum;// 业务线程

	@Value("${gps.netty.threads.business.max-pending}")
    private int maxPending;// 业务线程最大挂起任务数


    /**
     * TCP连接处理
     * @return
     */
    @Bean(name = "bossGroup")
    public NioEventLoopGroup bossGroup() {// 主线程
        return new NioEventLoopGroup(bossNum);// 主线程
    }

    /**
     * Socket数据读写
     * @return
     */
    @Bean(name = "workerGroup")// 工作线程
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerNum);// 工作线程
    }

    /**
     * Handler业务处理
     * @return
     */
    @Bean(name = "businessGroup")
    public EventExecutorGroup businessGroup() {// 业务线程
        return new DefaultEventExecutorGroup(businessNum,new BusinessThreadFactory(),maxPending, RejectedExecutionHandlers.reject());// 业务线程
    }
    
    static class BusinessThreadFactory implements ThreadFactory {
        private final ThreadGroup group;// 线程组
        private final AtomicInteger threadNumber = new AtomicInteger(1);// 线程编号
        private final String namePrefix;// 线程名称前缀

        BusinessThreadFactory() {// 构造函数
            SecurityManager s = System.getSecurityManager();// 安全管理器
            group = (s != null) ? s.getThreadGroup() :// 父线程组
                                  Thread.currentThread().getThreadGroup();// 当前线程组
            namePrefix = "business-thread-";// 线程名称前缀
        }

        @Override
        public Thread newThread(Runnable r) {// 创建线程
            Thread t = new Thread(group, r,// 线程名称
                                  namePrefix + threadNumber.getAndIncrement(),// 线程优先级
                                  0);
            if (t.isDaemon()){// 是否为守护线程
                t.setDaemon(false);// 设置为非守护线程
            }

            if (t.getPriority() != Thread.NORM_PRIORITY){// 线程优先级
                t.setPriority(Thread.NORM_PRIORITY);// 设置为默认优先级
            }
            return t;// 返回线程
        }
    }
}
