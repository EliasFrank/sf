package com.hl.sf.repository;

import com.hl.sf.entity.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author hl2333
 */
@Repository
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

    @Update("update `user` SET username = #{name} where id = #{id}")
    void updateUsername(@Param("id") Long id, @Param("name") String name);

    @Update("update `user` SET email = #{email} where id = #{id}")
    void updateEmail(@Param("id") Long id, @Param("email") String email);

    @Update("update `user` SET password = #{password} where id = #{id}")
    void updatePassword(@Param("id") Long id, @Param("password") String password);

}
