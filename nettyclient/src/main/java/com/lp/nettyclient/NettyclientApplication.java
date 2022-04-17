package com.lp.nettyclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyclientApplication.class, args);
        new TimeClient("127.0.0.1",1111).run();
    }

}
