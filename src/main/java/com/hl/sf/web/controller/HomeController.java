package com.hl.sf.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author hl2333
 */
@Controller
public class HomeController {
    @GetMapping({"/", "/index"})
    public String index(Model model){
        model.addAttribute("msg", "hello");
        return "index";
    }

    @GetMapping("/403")
    public String accessError(){
        return "403";
    }

    @GetMapping("/404")
    public String notFoundPage(){
        return "404";
    }

    @GetMapping("/500")
    public String internalError(){
        return "500";
    }

    @GetMapping("/logout")
    public String logoutPage(){
        return "logout";
    }

}
