package com.hl.sf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author hl2333
 */
@Controller
public class HomeController {
    @GetMapping("/")
    public String index(){
        return "index";
    }
}
