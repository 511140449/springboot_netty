package com.lp.netty.handler;

import com.lp.netty.bean.Result;
import com.lp.annotation.Module;
import com.lp.netty.bean.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;

@Module(module = 0)
@Component
public class HeartbeatHandler extends BaseHandler {
    
    private static final Logger log = LoggerFactory.getLogger(HeartbeatHandler.class);
    
    @Override
    public Result process(ChannelHandlerContext ctx, Message message) {
        log.info("heartbeat...");
        return new Result(1, "heartbeat success...");
    }

}
