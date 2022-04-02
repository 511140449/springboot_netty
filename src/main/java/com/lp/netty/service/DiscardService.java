package com.lp.netty.service;

import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yangguang
 * @DateTime 2022/4/2 11:00
 *
 * 业务层
 */
@Slf4j
@Component
public class DiscardService {
    private Logger logger = LoggerFactory.getLogger(DiscardService.class);
    public void discard(String msg){
        log.info("丢弃：{}",msg);
        logger.info("丢弃：{}",msg);
        ReferenceCountUtil.release(msg);
    }
}
