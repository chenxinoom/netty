package io.netty.example.chen;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

public class EchoServer {

    private static final String DELIMITER = "$_";
    private AtomicInteger num = new AtomicInteger();

    public void bind(Integer port) {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        DefaultEventLoopGroup business = new DefaultEventLoopGroup(1,(r) -> {
            Thread thread = new Thread(r);
            thread.setName("business" + num.incrementAndGet());
            return thread;
        });
        NioEventLoopGroup work = new NioEventLoopGroup(10,(r) -> {
            Thread thread = new Thread(r);
            thread.setName("work" + num.incrementAndGet());
            return thread;
        });


        try {
            b.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ByteBuf buf = Unpooled.copiedBuffer(DELIMITER.getBytes());
                            ChannelPipeline pipeline = socketChannel.pipeline();
//                            pipeline.addLast(new DelimiterBasedFrameDecoder(1024, buf));
                            pipeline.addLast(new FixedLengthFrameDecoder(20));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(business,new EchoServerHandle());
                        }
                    });



            //sync 阻塞
            ChannelFuture future = b.bind(port).sync();
            System.out.println("zhixing bind");
            future.channel().closeFuture().sync();
            System.out.println("zhixing close");
        } catch (InterruptedException e) {
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }

    }

    class EchoServerHandle extends ChannelInboundHandlerAdapter {

        String body;
        private AtomicInteger count = new AtomicInteger();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            Channel channel = ctx.channel();
            Channel.Unsafe unsafe = channel.unsafe();
//            unsafe.beginRead();
            body = (String) msg;
            System.out.println("客户端发送的信息为:" + body + count.incrementAndGet());
            ByteBuf buf = Unpooled.copiedBuffer((body ).getBytes());
            //这是异步完成
            ChannelFuture future = ctx.writeAndFlush(getButeBuf(body));
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    System.out.println("完成");
//                    Thread.sleep(100);
                    System.err.println("Future中的线程"+ count.get()  + Thread.currentThread());
                }
            });
            System.out.println("yibu");
        }

        public ByteBuf getButeBuf(String body) {
            try {
                System.err.println("Future外的线程" + count.get() + Thread.currentThread());

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Unpooled.copiedBuffer((body ).getBytes());
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            System.out.println(count.incrementAndGet());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            System.out.println("发生异常");
        }

//        PrintStream out = System.err;
//
//
//
//
//        @Override
//        public void channelRegistered(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("channelRegistered: channel注册到NioEventLoop");
//            super.channelRegistered(ctx);
//        }

//        @Override
//        public void channelUnregistered(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("channelUnregistered: channel取消和NioEventLoop的绑定");
//            super.channelUnregistered(ctx);
//        }

//        @Override
//        public void channelActive(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("channelActive: channel准备就绪");
//            super.channelActive(ctx);
//        }

//        @Override
//        public void channelInactive(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("channelInactive: channel被关闭");
//            super.channelInactive(ctx);
//        }

//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg)
//                throws Exception {
//            out.println("channelRead: channel中有可读的数据" );
//            super.channelRead(ctx, msg);
//        }

//        @Override
//        public void channelReadComplete(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("channelReadComplete: channel读数据完成");
//            super.channelReadComplete(ctx);
//        }

//        @Override
//        public void handlerAdded(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("handlerAdded: handler被添加到channel的pipeline");
//            super.handlerAdded(ctx);
//        }
//
//        @Override
//        public void handlerRemoved(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("handlerRemoved: handler从channel的pipeline中移除");
//            super.handlerRemoved(ctx);
//        }


    }

    public static void main(String[] args) throws InterruptedException {
        EchoServer echoServer = new EchoServer();
        echoServer.bind(8080);
    }
}
