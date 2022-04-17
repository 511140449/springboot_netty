package com.lp.nettyserver;

import com.lp.nettyserver.listener.MyApplicationStartingEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyserverApplication {

    public static void main(String[] args) {
//        SpringApplication.run(NettyserverApplication.class, args);
        //		SpringApplication.run(SpringbootNettyApplication.class, args);
        SpringApplication app = new SpringApplication(NettyserverApplication.class);
        app.addListeners(new MyApplicationStartingEventListener());
        app.run(args);
    }

}
