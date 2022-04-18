package com.lp.nettyserver.netty.handler;

import com.lp.nettyserver.netty.bean.Message;
import com.lp.nettyserver.netty.bean.Result;
import com.lp.nettyserver.netty.config.MyChannelHandlerPool;
import com.lp.nettyserver.util.MyAnnotionUtil;
import com.lp.nettyserver.util.constants.ChannelConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebsoketServerHandler extends ChannelInboundHandlerAdapter {
    //线程安全
    private static final ConcurrentHashMap<ChannelId, Integer> channelIdleTime = new ConcurrentHashMap<ChannelId, Integer>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("通道创建：{}，{}", ctx.channel().remoteAddress(), DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelInactive ======");
        ctx.close();
        log.info("====== Channel close ======");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if( msg instanceof Message ) {
            System.out.println("Message消息");
            Message message = (Message) msg;
            Result result = MyAnnotionUtil.process(ctx, message);
            log.info("result: " + result.toString());
            ctx.writeAndFlush(result);
        }else if ( msg instanceof String ){
            System.out.println("字符串消息");
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
        }else if(msg instanceof FullHttpRequest){
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            log.info("http收到用户{}的{}请求",ctx.channel().id(),fullHttpRequest.method());
            ByteBuf content = fullHttpRequest.content();
            String s = content.toString(CharsetUtil.UTF_8);
            int readIndex = content.readerIndex();
            byte aByte = content.getByte(readIndex);
            readIndex++;
            int length = content.getShort(readIndex);
            // 减3 是因为前面是获取到总长度，前面占用了byte + short = 1+2=3
            byte[] data = new byte[length - 3];//数据大小
            content.getBytes(readIndex, data);
            String str = new String(data, 0, data.length, StandardCharsets.UTF_8);
            System.out.println(str);

        }
        else{
            System.out.printf("收到客户端%s的数据：%s%n",ctx.channel().id(), msg);
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
                log.warn("---READER_IDLE 读空闲---" + dateFormat.format(new Date()));
                ChannelId channelId = ctx.channel().id();
                Integer times = channelIdleTime.get(channelId);
                if (times == null) {
                    channelIdleTime.put(channelId, 1);
                } else {
                    int num = times + 1;
                    if (num >= ChannelConstant.TIME_OUT_NUM) {
                        log.error("---READER_IDLE TIME OUT 读空闲超时，关闭通道 ---");
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
        cause.printStackTrace();
        log.error("exceptionCaught:" + cause.getMessage());
    }



}
