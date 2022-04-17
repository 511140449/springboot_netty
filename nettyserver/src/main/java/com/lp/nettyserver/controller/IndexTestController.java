package com.lp.nettyserver.controller;

import com.lp.nettyserver.annotation.MyLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@MyLog
@RequestMapping("/demo")
public class IndexTestController {

    @GetMapping("/")
    public String index(){
        return "dsfa";
    }
}
