package com.lp.controller;

import com.lp.annotation.MyLog;
import lombok.extern.slf4j.Slf4j;
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
