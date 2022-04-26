package com.lp.nettyclient.httpClient;

/**
 * czh_member_card
 *
 * @author yangguang
 * @DateTime 2022/3/16 11:39
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
    private final HttpClient httpClient;

    public HttpClientHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        log.info("收到:msg -> {}",msg);
        if(msg instanceof FullHttpResponse){
            FullHttpResponse response = (FullHttpResponse)msg;
            ByteBuf buf = response.content();
            String result = buf.toString(CharsetUtil.UTF_8);
            log.info("收到：msg -> {}",result);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("通道激活");
    }



    /*@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        URI uri = new URI("/user/get");
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri.toASCIIString());
        request.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        request.headers().add(HttpHeaderNames.CONTENT_LENGTH,request.content().readableBytes());
        ctx.writeAndFlush(request);
    }*/

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        log.info("通道关闭,等待5秒后重连");
        boolean isReconnet = true;
        while ( isReconnet ){
            try {
                Thread.sleep(5000);
                httpClient.run();
            }catch (Exception e){
                log.info("重连失败,等待5秒后再次重连");
                continue;
            }
            isReconnet = false;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Thread.sleep 异常 interruptedException:{}",cause);
        ChannelFuture disconnect = ctx.channel().disconnect();
        ctx.close();
    }
}