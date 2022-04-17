package com.lp.nettyserver.netty.codec;

import com.lp.nettyserver.netty.bean.Result;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.MessagePack;

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
