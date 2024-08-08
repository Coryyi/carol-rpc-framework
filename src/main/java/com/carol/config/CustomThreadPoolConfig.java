package com.carol.config;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
public class CustomThreadPoolConfig {
    /**
     * 线程池默认参数
     */
    //线程池核心线程数，线程池中线程数小于此值时，即使有空闲线程也会创建新线程
    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    //定义线程池的最大线程数，线程池允许创建的最大线程数
    private static final int DEFAULT_MAXIMUM_POOL_SIZE_SIZE = 100;
    //线程池中非核心线程的空闲事件，当空闲事件超过该值就会被终止
    private static final int DEFAULT_KEEP_ALIVE_TIME  = 1;

    // 定义线程池中非核心线程的空闲时间的单位，这里是分钟
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    // 定义阻塞队列的默认容量，即队列中最多可以存储的等待执行的任务数量
    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 100;

    // 定义一个常量，表示阻塞队列的容量，这里和默认容量相同，可以看作是冗余的
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    /**
     * 可配置参数
     */
    // 可配置的核心线程数，默认值为DEFAULT_CORE_POOL_SIZE
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;

    // 可配置的最大线程数，默认值为DEFAULT_MAXIMUM_POOL_SIZE_SIZE
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE_SIZE;

    // 可配置的非核心线程的空闲时间，默认值为DEFAULT_KEEP_ALIVE_TIME
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;

    // 可配置的空闲时间单位，默认值为DEFAULT_TIME_UNIT
    private TimeUnit unit = DEFAULT_TIME_UNIT;

    /*
    Runnable task = () -> {
    // 要执行的任务代码
    };
    Thread thread = new Thread(task);
    thread.start();
     */
    // 使用有界队列作为工作队列，默认容量为BLOCKING_QUEUE_CAPACITY
    // ArrayBlockingQueue是一个基于数组结构的有界阻塞队列，用于存储等待执行的任务
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
    //使用take方法取出时，若没有元素则会阻塞直到有元素添加
}
