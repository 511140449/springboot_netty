package com.lp.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.lp.netty.config.NettyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.context.support.WebApplicationContextUtils;

import io.netty.channel.ChannelFuture;

@Component
public class NettyRunServletContextListener implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(NettyRunServletContextListener.class);
    @Autowired
    private NettyConfig nettyConfig;

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("====== springboot netty destroy ======");
        nettyConfig.destroy();
        System.out.println("---test contextDestroyed method---");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            nettyConfig.run();
        } catch (Exception e) {
            logger.error("---springboot netty server start error : {}", e.getMessage() + "---");
        }
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
