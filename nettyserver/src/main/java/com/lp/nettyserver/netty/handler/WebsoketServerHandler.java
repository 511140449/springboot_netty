package com.lp.nettyserver.netty.handler;

import com.lp.nettyserver.netty.bean.Message;
import com.lp.nettyserver.netty.bean.Result;
import com.lp.nettyserver.netty.config.MyChannelHandlerPool;
import com.lp.nettyserver.util.MyAnnotionUtil;
import com.lp.nettyserver.util.constants.ChannelConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
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
        log.info("通道创建：{}，{}", ctx.channel().remoteAddress(), DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        ctx.writeAndFlush(createRequest("hello"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelInactive ======");
        ctx.close();
        log.info("====== Channel close ======");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ChannelId id = ctx.pipeline().channel().id();
        if( msg instanceof Message ) {
            Message message = (Message) msg;
            Result result = MyAnnotionUtil.process(ctx, message);
            log.info("Message消息: " + result.toString());
            ctx.writeAndFlush(result);
        }else if ( msg instanceof String ){
            String msgStr = (String) msg;
            log.info("Message消息: " + msgStr);

            String heartbeatReply = null;
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
            log.info(String.format("收到客户端%s的TextWebSocketFrame数据：%s" ,ctx.channel().id(), wsMsg.text()));
            ctx.writeAndFlush(createRequest(wsMsg.text()));
        }else if(msg instanceof FullHttpRequest){
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            if( !fullHttpRequest.decoderResult().isSuccess() ){
                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            String rs = "rs = ";
            ByteBuf content = fullHttpRequest.content();
            log.info("http收到用户{}的{}请求内容：{}",ctx.channel().id(),fullHttpRequest.method(),content );
            int readIndex = content.readerIndex();
            //从开始位置读取一个字节
            String first = String.valueOf(content.getByte(readIndex));
            rs += "，"+readIndex+"，"+ first;
            readIndex++;
            int totalLength = content.readableBytes();

            if( totalLength-readIndex>1 ){
                //读两个字节
                int length = content.getShort(readIndex);
                rs += "，"+readIndex+"，"+ length;
                readIndex += 2;
            }else{
                rs += "，"+readIndex+"，"+ String.valueOf(content.getByte(readIndex));
                readIndex++;
            }

            if( totalLength > readIndex ){
                // 减3 是因为前面是获取到总长度，前面占用了byte + short = 1+2=3
                byte[] data = new byte[totalLength - readIndex];//数据大小
                content.getBytes(readIndex, data);
                rs += "，"+readIndex+"，"+ new String(data, 0, data.length, StandardCharsets.UTF_8);
            }
            //http 反馈
            String reply = "{\"code\":\"200\",\"message\":\"pong："+content.toString()+"\"}";
            ByteBuf byteBuf = Unpooled.copiedBuffer(reply.getBytes(StandardCharsets.UTF_8));
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,byteBuf);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,byteBuf.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.info("成功回复通道：{}的心跳包：{}",channelFuture.channel().id(),content.toString());
                }
            });
        }
        else{
            log.info("收到客户端{}的数据：{}",ctx.channel().id(), msg);
        }


        super.channelRead(ctx,msg);
    }

    public static void main(String[] args) {
        ByteBuf content = Unpooled.wrappedBuffer("我是李鹏".getBytes(StandardCharsets.UTF_8));
        String name = content.toString(CharsetUtil.UTF_8);
        System.out.println(name);

        ByteBuffer finalByteBuffer = ByteBuffer.allocate(4);
        //去个字节 8位
        finalByteBuffer.put((byte) 65); //01000001
        finalByteBuffer.put((byte) 66); //0100001001011000
        finalByteBuffer.put((byte) 88);
        finalByteBuffer.put((byte) 89);

        ByteBuf byteBuf = Unpooled.copiedBuffer((ByteBuffer) finalByteBuffer.position(0));

        int readIndex = byteBuf.readerIndex();
        byte aByte = byteBuf.getByte(readIndex);
        readIndex++;
        short aShort = byteBuf.getShort(readIndex);
        System.out.println(String.valueOf(aByte));
        System.out.println(aShort);
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
   private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
       FullHttpResponse response = new DefaultFullHttpResponse(
               HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
       response.headers().set("Content-Type", "text/plain; charset=UTF-8");
       System.out.println(response);
       ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
       super.exceptionCaught(ctx,cause);
        cause.printStackTrace();
        log.error("exceptionCaught:" + cause.getMessage());
    }


    public FullHttpRequest createRequest(String data) {
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
}
