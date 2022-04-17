package com.lp.nettyserver.netty.handler;

import com.alibaba.fastjson.JSON;
import com.lp.nettyserver.annotation.Module;
import com.lp.nettyserver.netty.bean.Message;
import com.lp.nettyserver.netty.bean.Result;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Module(module = 99)
@Component
public class EchoHandler extends BaseHandler {

    @Override
    public Result process(ChannelHandlerContext ctx, Message message) {
        return new Result(1, "success...", JSON.toJSONString(message));
    }

}
