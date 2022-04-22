package com.lp.nettyclient.httpClient;

/**
 * czh_member_card
 *
 * @author yangguang
 * @DateTime 2022/3/16 11:33
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

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

    public void run(){
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    //心跳- 必须放在前面
                    ch.pipeline().addLast(new IdleStateHandler(20, 0, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new HttpClientCodec());
                    ch.pipeline().addLast(new HttpObjectAggregator(65536));
                    ch.pipeline().addLast(new HttpContentDecompressor());
                    ch.pipeline().addLast(new HttpClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
            log.error("客户端异常",e);
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 1111;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new HttpClient("127.0.0.1",port).run();


    }
}