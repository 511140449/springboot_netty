package com.lp.nettyserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String home(){
        return "index";
    }

    @RequestMapping("/")
    public String index(){
        return "redirect:index";
    }

}
