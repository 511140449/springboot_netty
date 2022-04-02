package com.lp.netty.codec;

import com.lp.netty.bean.Result;
import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgEncoder extends MessageToByteEncoder<Result> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Result msg, ByteBuf out) throws Exception {
        
        MessagePack messagePack = new MessagePack();
        byte[] write = messagePack.write(msg);
        out.writeInt(write.length);
        out.writeBytes(write);
    }

}
