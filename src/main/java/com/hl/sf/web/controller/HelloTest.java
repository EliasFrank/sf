package com.hl.sf.web.controller;

import com.hl.sf.base.ApiResponse;
import com.hl.sf.entity.UserInfo;
import com.hl.sf.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hl2333
 */
@RestController
public class HelloTest {
    @Autowired
    UserDao userDao;

    @GetMapping("/hello")
    public String hello(){
        UserInfo userByID = userDao.getUserById(1L);
        System.out.println(userByID);
        return "hello, hl";
    }

    @GetMapping("/get")
    @ResponseBody
    public ApiResponse get(){
        return ApiResponse.ofMessage(200, "OK");
    }
}
