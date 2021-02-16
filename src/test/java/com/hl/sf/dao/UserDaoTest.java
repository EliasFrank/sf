package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.UserInfo;
import com.hl.sf.repository.UserDao;
import com.hl.sf.utils.EncodePassword;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDaoTest extends SfApplicationTests {
    @Autowired
    private UserDao userDao;

    @Test
    public void testFindById() {
//        User userByID = userDao.getUserById(2L);
        UserInfo userByID = userDao.getUserByName("admin");
        System.out.println(userByID);
    }

    @Test
    public void testUtilEncode() {
        String s = EncodePassword.encodePassword("123");
        System.out.println(s);
    }
}
