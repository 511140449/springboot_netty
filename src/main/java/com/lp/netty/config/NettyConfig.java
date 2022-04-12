package com.lp.netty.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

@Component
public class NettyConfig {

    private static final Logger log = LoggerFactory.getLogger(NettyConfig.class);

    @Value("${netty.port}")
    private int port;

    @Value("${netty.host}")
    private String host;

    private Channel channel;

    @Autowired
    private ChannelCache channelCache;

    /**
     * 启动服务
     */
    public void run(){

        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        ChannelFuture f = null;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //当设置为true的时候，TCP会实现监控连接是否有效，当连接处于空闲状态的时候，超过了2个小时，本地的TCP实现会发送一个数据包给远程的 socket，如果远程没有发回响应，TCP会持续尝试11分钟，知道响应为止，如果在12分钟的时候还没响应，TCP尝试关闭socket连接。
                    //这个参数其实对应用层的程序而言没有什么用。可以通过应用层实现了解服务端或客户端状态，而决定是否继续维持该Socket
                    .childOption(ChannelOption.SO_KEEPALIVE, false)
                    //禁用nagle算法
                    // Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送.但这不是重点, 关键是这个算法受TCP延迟确认影响, 会导致相继两次向连接发送请求包, 读数据时会有一个最多达500毫秒的延时.
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ServerChannelInitializer());
            //当前主机
            InetSocketAddress address = new InetSocketAddress(host, port);
            channel = b.bind(address).sync().channel();

            log.info("====== springboot netty start ======");
            //结束 和 应用closed 时间做的是相同的事情
            Runtime.getRuntime().addShutdownHook(new Thread()  {
                @Override
                public void run() {
                    log.info("---nettyConfig destroy Hook start---");
                    if(channel != null && channel.isActive() ){
                        log.info("hook 执行");
                        NettyConfig.this.destroy();
                    }
                    log.info("---nettyConfig destroy Hook end---");
                }
            });

            //让其不执行到 finally
            //channel.closeFuture().sync();  sync():等待Future直到其完成，如果这个Future失败，则抛出失败原因; syncUninterruptibly()：不会被中断的sync(),一直等待;
            channel.closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            log.error("Netty start error:", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public void destroy() {
        log.info("Shutdown Netty Server...");
        if (channel == null || !channel.isActive()) {
            log.info("Netty Closed!");
            return;
        }
        ChannelId id = this.channel.id();

        channel.flush();

        Channel channelNow = channelCache.getChannelGroup().find(id);
        if( channelNow != null ){
            log.info("存在缓存chennel");
            channelNow.close();
            channelCache.flushDb();
        }
        log.info("Shutdown Netty Server Success!");
    }
}
