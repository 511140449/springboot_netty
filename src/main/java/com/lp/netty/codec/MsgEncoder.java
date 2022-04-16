package com.lp.netty.codec;

import com.lp.netty.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Slf4j
public class MsgEncoder extends MessageToByteEncoder<Result> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Result msg, ByteBuf out) throws Exception {
        log.info("MsgEncoder");
        MessagePack messagePack = new MessagePack();
        byte[] write = messagePack.write(msg);
        out.writeInt(write.length);
        out.writeBytes(write);
    }

}
