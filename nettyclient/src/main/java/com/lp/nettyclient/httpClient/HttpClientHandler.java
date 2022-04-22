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

@Slf4j
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
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
        String data = "I am alive";
        while (ctx.channel().isActive()) {
            //模拟空闲状态
            int num = new Random().nextInt(10);
            Thread.sleep(num * 1000);
            log.info("心跳，{}",data);
            ctx.channel().writeAndFlush(heartRequest(data)).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.info("消息发送成功");
                }
            });
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
        boolean active = ctx.channel().isActive();
        log.info("通道状态：{}",active);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Thread.sleep 异常 interruptedException:{}",cause);
        ctx.close();
    }
}