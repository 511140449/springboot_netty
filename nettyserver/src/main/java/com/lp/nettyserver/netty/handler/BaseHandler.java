package com.lp.nettyserver.netty.handler;

import com.lp.nettyserver.netty.bean.Message;
import com.lp.nettyserver.netty.bean.Result;
import com.lp.nettyserver.netty.service.LoginRedisService;
import com.lp.nettyserver.netty.service.MyRedisService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseHandler {
    
    @Autowired
    protected MyRedisService jxRedisService;
    
    @Autowired
    protected LoginRedisService loginRedisService;
    
    public abstract Result process(ChannelHandlerContext ctx, Message message);
}
