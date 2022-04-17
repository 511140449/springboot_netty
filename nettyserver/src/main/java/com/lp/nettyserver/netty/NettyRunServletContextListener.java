package com.lp.nettyserver.netty;

import com.lp.nettyserver.netty.config.ChannelCache;
import com.lp.nettyserver.netty.handler.WebsoketServerHandler;
import com.lp.nettyserver.util.constants.ChannelConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyRunServletContextListener implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    @Value("${netty.port}")
    private int port;

    @Value("${netty.host}")
    private String host;

    private Channel channel;

    @Autowired
    private ChannelCache channelCache;


    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("====== springboot netty destroy ======");
        neetyDestroy();
        System.out.println("---test contextDestroyed method---");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        ChannelFuture f = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.localAddress(new InetSocketAddress(host, this.port));

            bootstrap.group(bossGroup, workerGroup)
                    // 指定使用的channel
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //当设置为true的时候，TCP会实现监控连接是否有效，当连接处于空闲状态的时候，超过了2个小时，本地的TCP实现会发送一个数据包给远程的 socket，如果远程没有发回响应，TCP会持续尝试11分钟，知道响应为止，如果在12分钟的时候还没响应，TCP尝试关闭socket连接。
                    //这个参数其实对应用层的程序而言没有什么用。可以通过应用层实现了解服务端或客户端状态，而决定是否继续维持该Socket
                    .childOption(ChannelOption.SO_KEEPALIVE, false)
                    //禁用nagle算法
                    // Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送.但这不是重点, 关键是这个算法受TCP延迟确认影响, 会导致相继两次向连接发送请求包, 读数据时会有一个最多达500毫秒的延时.
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //心跳- 必须放在前面
                            socketChannel.pipeline().addLast(new IdleStateHandler(ChannelConstant.READER_IDLE_TIME_SECONDS, 0, 0, TimeUnit.SECONDS));
                            //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                            socketChannel.pipeline().addLast(new HttpServerCodec());
                            socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast(new HttpObjectAggregator(65536));
                            socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", "WebSocket", true, 65536 * 10));
                            socketChannel.pipeline().addLast(new WebsoketServerHandler());

                            // 解码编码 尝试
                            // socketChannel.pipeline().addLast(new
                            // LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
//                            socketChannel.pipeline().addLast(new MsgDecoder());
                            // socketChannel.pipeline().addLast(new LengthFieldPrepender(2));
//                            socketChannel.pipeline().addLast(new MsgEncoder());
//
                        }
                    });
            //当前主机
            channel = bootstrap.bind().sync().channel();

//            channelCache.addChannel(channel,);
            log.info(NettyRunServletContextListener.class + "已启动，正在监听： " + channel.localAddress());
            //结束 和 应用closed 时间做的是相同的事情
            Runtime.getRuntime().addShutdownHook(new Thread()  {
                @Override
                public void run() {
                    log.info("---nettyConfig destroy Hook start---");
                    if(channel != null && channel.isActive() ){
                        log.info("hook 执行");
                        neetyDestroy();
                    }
                    log.info("---nettyConfig destroy Hook end---");
                }
            });

            //让其不执行到 finally
            //channel.closeFuture().sync();  sync():等待Future直到其完成，如果这个Future失败，则抛出失败原因; syncUninterruptibly()：不会被中断的sync(),一直等待;
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("Netty start error:", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    public void neetyDestroy() {
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



    /*@Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("====== springboot netty destroy ======");
        nettyConfig.destroy();
        System.out.println("---test contextDestroyed method---");
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(servletContextEvent.getServletContext())
                .getAutowireCapableBeanFactory().autowireBean(this);
        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), port);
            ChannelFuture future = nettyConfig.run(address);
            logger.info("====== springboot netty start ======");
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    logger.info("---nettyConfig destroy---");
                    nettyConfig.destroy();
                }

            });
            future.channel().closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            logger.error("---springboot netty server start error : ", e.getMessage() + "---");
        }
    }*/

}
