package com.lp.netty.config;


import com.lp.netty.codec.MsgDecoder;
import com.lp.netty.codec.MsgEncoder;
import com.lp.netty.handler.ServerHandler;
import com.lp.util.Const;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        // 解码编码
        // socketChannel.pipeline().addLast(new
        // LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
        socketChannel.pipeline().addLast(new MsgDecoder());
        // socketChannel.pipeline().addLast(new LengthFieldPrepender(2));
        socketChannel.pipeline().addLast(new MsgEncoder());
        socketChannel.pipeline().addLast(new IdleStateHandler(Const.READER_IDLE_TIME_SECONDS, 0, 0));
        socketChannel.pipeline().addLast(new ServerHandler());
    }

}
