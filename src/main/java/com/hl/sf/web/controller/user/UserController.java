package com.hl.sf.web.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hl2333
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @GetMapping("/login")
    public String loginPage(){
        return "user/login";
    }

    @GetMapping("/center")
    public String centerPage(){
        return "user/center";
    }
}
