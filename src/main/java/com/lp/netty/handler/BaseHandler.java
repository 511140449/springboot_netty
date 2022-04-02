package com.lp.netty.handler;

import com.lp.netty.bean.Message;
import com.lp.netty.bean.Result;
import com.lp.netty.service.LoginRedisService;
import com.lp.netty.service.MyRedisService;
import org.springframework.beans.factory.annotation.Autowired;

import io.netty.channel.ChannelHandlerContext;

public abstract class BaseHandler {
    
    @Autowired
    protected MyRedisService jxRedisService;
    
    @Autowired
    protected LoginRedisService loginRedisService;
    
    public abstract Result process(ChannelHandlerContext ctx, Message message);
}
