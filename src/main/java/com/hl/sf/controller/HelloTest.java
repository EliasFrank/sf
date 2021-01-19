package com.hl.sf.controller;

import com.hl.sf.entity.User;
import com.hl.sf.mapper.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
        User userByID = userDao.getUserByID(1L);
        System.out.println(userByID);
        return "hello, hl";

    }
}
