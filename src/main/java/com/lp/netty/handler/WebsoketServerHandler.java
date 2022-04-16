package com.lp.netty.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.lp.netty.bean.Message;
import com.lp.netty.bean.Result;
import com.lp.netty.config.ChannelCache;
import com.lp.netty.config.MyChannelHandlerPool;
import com.lp.util.Const;
import com.lp.util.MyAnnotionUtil;
import com.lp.util.SpringUtil;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
public class WebsoketServerHandler extends ChannelInboundHandlerAdapter {
    
    private static final ConcurrentHashMap<ChannelId, Integer> channelIdleTime = new ConcurrentHashMap<ChannelId, Integer>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("通道创建：{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelInactive ======");
        ctx.close();
        log.info("====== Channel close ======");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if( msg instanceof  Message ) {
            Message message = (Message) msg;
            Result result = MyAnnotionUtil.process(ctx, message);
            log.info("result: " + result.toString());
            ctx.writeAndFlush(result);
        }else if ( msg instanceof String ){
            String heartbeatReply = null;
            String msgStr = (String) msg;
            switch (msgStr){
                case "ping":
                    heartbeatReply = "pong";
                    break;
                default:
                    heartbeatReply = "干什么呀？";
                    break;
            }
            ctx.writeAndFlush(heartbeatReply).sync();
        }else if (msg instanceof TextWebSocketFrame){
            TextWebSocketFrame wsMsg = (TextWebSocketFrame) msg;
            //接收的消息
            System.out.println(String.format("收到客户端%s的数据：%s" ,ctx.channel().id(), wsMsg.text()));
            sendMessage(ctx);
        }
    }

   /* @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //接收的消息
        System.out.println(String.format("收到客户端%s的数据：%s" ,ctx.channel().id(), msg.text()));

        // 单独发消息
        // sendMessage(ctx);
        // 群发消息
        sendAllMessage();
    }*/


    private void sendMessage(ChannelHandlerContext ctx){
        String message = "消息";
        ctx.writeAndFlush(new TextWebSocketFrame(message));
    }
    //广播
    private void sendAllMessage(){
        String message = "我是服务器，这是群发消息";
        MyChannelHandlerPool.channelGroup.writeAndFlush(new TextWebSocketFrame(message));
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



}
