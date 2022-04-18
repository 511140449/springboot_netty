package com.lp.nettyclient;

/**
 * czh_member_card
 *
 * @author yangguang
 * @DateTime 2022/3/16 11:39
 */
import io.netty.buffer.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg; // (1)
        try {
            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            System.out.println(new Date(currentTimeMillis));
            ctx.close();
        } finally {
            m.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("通道激活");
        ByteBuffer finalByteBuffer = ByteBuffer.allocate(2);
        // put的时候，ByteBuffer的position指针会移动
        // 导致Unpooled.copiedBuffer(ByteBuffer buffer)返回了EMPTY_BUFFER
        finalByteBuffer.put((byte) 66);
        finalByteBuffer.put((byte) 88);
        // 用下面这个发不出去
//        ByteBuf byteBuf = Unpooled.copiedBuffer(finalByteBuffer);
        // 用下面这2个可以发出去
        ByteBuf byteBuf = Unpooled.copiedBuffer((ByteBuffer) finalByteBuffer.position(0));
//        ByteBuf byteBuf = Unpooled.copiedBuffer(finalByteBuffer.array());
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/", byteBuf );

        request.headers().set("Host", "127.0.0.1");
        request.headers().set("Connection", "keep-alive");
        request.headers().set("Content-Length", request.content().readableBytes());
        request.headers().set("Content-Type", "application/json");


        ctx.channel().writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("消息发送成功");
            }
        });
//        ctx.writeAndFlush(new TextWebSocketFrame(message));
//        ctx.close();
    }


    /*@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        URI uri = new URI("/user/get");
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri.toASCIIString());
        request.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        request.headers().add(HttpHeaderNames.CONTENT_LENGTH,request.content().readableBytes());
        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("msg -> "+msg);
        if(msg instanceof FullHttpResponse){
            FullHttpResponse response = (FullHttpResponse)msg;
            ByteBuf buf = response.content();
            String result = buf.toString(CharsetUtil.UTF_8);
            System.out.println("response -> "+result);
        }
    }*/

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}