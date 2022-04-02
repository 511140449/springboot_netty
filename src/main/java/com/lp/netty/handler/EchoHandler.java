package com.lp.netty.handler;

import com.lp.annotation.Module;
import com.lp.netty.bean.Message;
import com.lp.netty.bean.Result;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelHandlerContext;

@Module(module = 99)
@Component
public class EchoHandler extends BaseHandler {

    @Override
    public Result process(ChannelHandlerContext ctx, Message message) {
        return new Result(1, "success...", JSON.toJSONString(message));
    }

}
