package com.hl.sf.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hl2333
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("center")
    public String adminCenterPage(){
        return "admin/center";
    }

    @GetMapping("welcome")
    public String welcomePage(){
        return "admin/welcome";
    }

    @GetMapping("login")
    public String adminLoginPage(){
        return "admin/login";
    }
}
