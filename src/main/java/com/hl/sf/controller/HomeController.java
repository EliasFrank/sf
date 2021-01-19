package com.hl.sf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author hl2333
 */
@Controller
public class HomeController {
    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("msg", "hello");
        return "index";
    }

    @GetMapping("ut")
    public String ut(){
        return "user/login";
    }
}
