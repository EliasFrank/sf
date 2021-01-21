package com.hl.sf.repository;

import com.hl.sf.entity.UserInfo;

/**
 * @author hl2333
 */
public interface UserDao {
    /**
     * 根据用户id查询用户的所有信息
     * @param id 用户id
     * @return 用户信息
     */
    UserInfo getUserById(Long id);

    /**
     * 根据用户名查询用户的所有信息
     * @param s 用户名
     * @return 用户信息
     */
    UserInfo getUserByName(String s);
}
