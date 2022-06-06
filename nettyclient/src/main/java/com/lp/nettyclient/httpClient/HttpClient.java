package com.lp.nettyclient.httpClient;

/**
 * czh_member_card
 *
 * @author yangguang
 * @DateTime 2022/3/16 11:33
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 编写时间客户端
 * 与服务器不同DISCARD，ECHO我们需要一个TIME协议客户端，因为人类无法将 32 位二进制数据转换为日历上的日期。在本节中，我们将讨论如何确保服务器正常工作并学习如何使用 Netty 编写客户端。
 *
 * Netty 中服务器和客户端之间最大也是唯一的区别是使用了不同的Bootstrap实现Channel。请看下面的代码：
 * */
@Slf4j
public class HttpClient {
    private final int port;
    private final String host;

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)

            //该参数的作用就是禁止使用Nagle算法，使用于小数据即时传输
            b.option(ChannelOption.TCP_NODELAY, true);
            //（4）当设置为true的时候，TCP会实现监控连接是否有效，当连接处于空闲状态的时候，超过了2个小时，本地的TCP实现会发送一个数据包给远程的 socket，如果远程没有发回响应，TCP会持续尝试11分钟，知道响应为止，如果在12分钟的时候还没响应，TCP尝试关闭socket连接。
            //这个参数其实对应用层的程序而言没有什么用。可以通过应用层实现了解服务端或客户端状态，而决定是否继续维持该Socket，默认true
            b.option(ChannelOption.SO_KEEPALIVE, false); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast( new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new HttpClientCodec());
                    ch.pipeline().addLast(new HttpObjectAggregator(65536));
                    ch.pipeline().addLast(new HttpContentDecompressor());
                    ch.pipeline().addLast(new HttpClientHandler(HttpClient.this));
                }
            });

            // Start the client.
            ChannelFuture connect = b.connect(host, port).sync();
            ChannelFuture f = connect.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        //重连交给后端线程执行
                        channelFuture.channel().eventLoop().schedule(() -> {
                            log.info("重连服务端...");
                            try {
                                HttpClient.this.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }, 3000, TimeUnit.MILLISECONDS);
                    }else {
                        System.out.println("开始心跳...");
                        String data = "I am alive";
                        while (channelFuture.channel().isActive()) {
                            //模拟空闲状态
                            int num = new Random().nextInt(10);
                            Thread.sleep(num * 1000);
                            channelFuture.channel().writeAndFlush(HttpClient.this.heartRequest(data)).sync().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                    log.info("心跳发送成功");
                                }
                            });
                        }
                    }
                }
            });

            // Wait until the connection is closed.
            connect.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("客户端异常",e);
            throw e;
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    public FullHttpRequest heartRequest(String data) {
        int length = data.getBytes(StandardCharsets.UTF_8).length;
        ByteBuffer finalByteBuffer = ByteBuffer.allocate(length+2);
        // put的时候，ByteBuffer的position指针会移动
        // 导致Unpooled.copiedBuffer(ByteBuffer buffer)返回了EMPTY_BUFFER
        finalByteBuffer.put((byte) 66);
        finalByteBuffer.put((byte) 88);
        finalByteBuffer.put(data.getBytes(StandardCharsets.UTF_8),0,length);
        // 用下面这个发不出去
//        ByteBuf byteBuf = Unpooled.copiedBuffer(finalByteBuffer);
        // 用下面这2个可以发出去
        ByteBuf byteBuf = Unpooled.copiedBuffer((ByteBuffer) finalByteBuffer.position(0));
//        ByteBuf byteBuf = Unpooled.copiedBuffer(finalByteBuffer.array());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/", byteBuf );

        request.headers().set("Host", "127.0.0.1");
        request.headers().set("Connection", "keep-alive");
        request.headers().set("Content-Length", request.content().readableBytes());
        request.headers().set("Content-Length",byteBuf.readableBytes());
//        ByteBuf content = request.content();
        request.headers().set("Content-Type", "application/json");
        return request;
    }

    public static void main(String[] args) throws Exception {
        int port = 1111;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        boolean isReconnet = true;
        while ( isReconnet ){
            try {
                new HttpClient("127.0.0.1",port).run();
            }catch (Exception e){
                log.info("连接失败，等待5秒后重连");
                Thread.sleep(5000);
                continue;
            }
            isReconnet = false;
        }


    }
}