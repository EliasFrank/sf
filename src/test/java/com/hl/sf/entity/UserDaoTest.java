package com.hl.sf.entity;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.mapper.UserDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDaoTest extends SfApplicationTests {
    @Autowired
    private UserDao userDao;

    @Test
    public void testFindById() {
        User userByID = userDao.getUserByID(1L);
        System.out.println(userByID);
    }
}
