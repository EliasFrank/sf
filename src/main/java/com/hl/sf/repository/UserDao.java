package com.hl.sf.repository;

import com.hl.sf.entity.UserInfo;
import org.apache.ibatis.annotations.Select;

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

    @Select("select * from `user` where id = #{id}")
    UserInfo findById(Long userId);

    @Select("select * from `user` where phone_number = #{phone}")
    UserInfo findUserByPhone(String telephone);

    void save(UserInfo user);
}
