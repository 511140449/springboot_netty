package com.lp.nettyserver.netty.handler;

import com.lp.nettyserver.annotation.Module;
import com.lp.nettyserver.netty.bean.Message;
import com.lp.nettyserver.netty.bean.Result;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
