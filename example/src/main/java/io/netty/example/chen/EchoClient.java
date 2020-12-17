package io.netty.example.chen;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.PrintStream;

public class EchoClient {
    private static final String DELIMITER = "$_";

    public void connect(String ip, Integer port) throws InterruptedException {

        Bootstrap b = new Bootstrap();
        NioEventLoopGroup work = new NioEventLoopGroup();

        b.group(work)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,100)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ByteBuf buf = Unpooled.copiedBuffer(DELIMITER.getBytes());
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024,buf));
                        pipeline.addLast(new StringDecoder());


                        pipeline.addLast(new EchoClienHandle());
                    }
                });

        //这个可以看出不是一个线程
        System.out.println(Thread.currentThread());
        ChannelFuture future = b.connect(ip, port);
        future.addListener(new GenericFutureListener<Future<? super Void>>() {

            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.err.println(Thread.currentThread());
                System.out.println("连接完成");
            }
        });
        System.out.println("看sync");
        future.channel().closeFuture().sync();
    }

    class EchoClienHandle extends ChannelInboundHandlerAdapter {
        String body;

        @Override
        public void channelActive(ChannelHandlerContext ctx){
            for (int i = 0; i < 100; i++) {
                ByteBuf buf = Unpooled.copiedBuffer(("Echo Server MSG" + DELIMITER).getBytes());
//                System.out.println(buf);
                ctx.writeAndFlush(buf);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){
            body = (String)msg;
            System.out.println("服务器发来的数据:" + body);
        }

//        PrintStream out = System.out;
//
//        @Override
//        public void channelRegistered(ChannelHandlerContext ctx)
//                throws Exception {
//            out.println("channelRegistered: channel注册到NioEventLoop");
//            super.channelRegistered(ctx);
//        }
//
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
//
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
        EchoClient echoClient = new EchoClient();
        echoClient.connect("127.0.0.1",8080);
    }
}
