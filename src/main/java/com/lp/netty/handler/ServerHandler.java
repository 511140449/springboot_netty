package com.lp.netty.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.lp.netty.bean.Message;
import com.lp.netty.bean.Result;
import com.lp.netty.config.ChannelCache;
import com.lp.util.Const;
import com.lp.util.MyAnnotionUtil;
import com.lp.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
    
    private ChannelCache channelCache = SpringUtil.getBean(ChannelCache.class);
    
    private static ConcurrentHashMap<ChannelId, Integer> channelIdleTime = new ConcurrentHashMap<ChannelId, Integer>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if( msg instanceof  Message ) {
            Message message = (Message) msg;
            Result result = new Result();
            // 非登录接口，验证是否已登录过
            if (message.getModule() != 1) {
                if (channelCache.getChannel(ctx.channel()) == null) {
                    result = new Result(0, "need auth");
                    ctx.writeAndFlush(result);
                    return;
                }
            }
            channelCache.addChannel(ctx.channel(), message.getUid());
            result = MyAnnotionUtil.process(ctx, message);
            log.info("result: " + result.toString());
            ctx.writeAndFlush(result);
        }else{
            ctx.writeAndFlush("pong");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                log.warn("---READER_IDLE---" + dateFormat.format(new Date()));
                ChannelId channelId = ctx.channel().id();
                Integer times = channelIdleTime.get(channelId);
                if (times == null) {
                    channelIdleTime.put(channelId, 1);
                } else {
                    int num = times.intValue() + 1;
                    if (num >= Const.TIME_OUT_NUM) {
                        log.error("--- TIME OUT ---");
                        channelIdleTime.remove(channelId);
                        channelCache.removeChannel(ctx.channel());
                        ctx.close();
                    } else {
                        channelIdleTime.put(channelId, num);
                    }
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught:" + cause.getMessage());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("通道创建：{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelInactive ======");
        channelCache.removeChannel(ctx.channel());
        ctx.close();
        log.info("====== Channel close ======");
    }

}
