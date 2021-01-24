package com.hl.sf.web.controller.admin;

import com.hl.sf.base.ApiResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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

    @GetMapping("house/subscribe")
    public String subscribe(){
        return "admin/subscribe";
    }

    @GetMapping("add/house")
    public String addHouse(){
        return "admin/house-add";
    }

    @PostMapping(value = "upload/photo")
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        String fileName = file.getOriginalFilename();
        System.out.println(fileName);
        File target = new File("C:\\Users\\hl2333\\IdeaProjects\\sf\\tmp\\" + fileName);
        try {
            file.transferTo(target);
            System.out.println("ok");
        } catch (IOException e) {
            System.out.println("error");
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }

        return ApiResponse.ofSuccess(null);
    }
}
