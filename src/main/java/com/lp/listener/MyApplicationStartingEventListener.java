package com.lp.listener;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

/**
 * springboot启动监听类
 */
public class MyApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent>{

    @Override
    public void onApplicationEvent(ApplicationStartingEvent applicationStartingEvent) {
        SpringApplication application = applicationStartingEvent.getSpringApplication();
        application.setBannerMode(Banner.Mode.OFF);
        System.out.println("--------- execute MyApplicationStartingEventListener----------");
    }

}