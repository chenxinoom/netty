package io.netty.example.executor;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.SingleThreadEventExecutor;

/**
 * 测试一下SingleThreadEventExecutor类是线程执行流程
 */
public class SingleThreadEventExecutorTest {

    public static void main(String[] args) {
        //先试没有工厂方法的测试
        SingleThreadEventExecutor eventExecutors = new DefaultEventExecutor();
        //无返回值
        eventExecutors.execute(() -> {
            System.out.println("执行任务1");
        });

        eventExecutors.execute(() -> {
            System.out.println("执行任务2");
        });

        eventExecutors.execute(() -> {
            System.out.println("执行任务3");
        });

        Future future = eventExecutors.shutdownGracefully();

//                addListener(new GenericFutureListener<Future<? super Void>>() {
//
//                    @Override
//                    public void operationComplete(Future<? super Void> future) throws Exception {
//                        System.err.println(Thread.currentThread());
//                        System.out.println("连接完成");
//                    }
//                });

        future.addListener(new GenericFutureListener<Future<?>>() {

            @Override
            public void operationComplete(Future<?> future) throws Exception {
                System.err.println(Thread.currentThread());
                System.out.println("关闭成功");
            }
        });


    }


}
