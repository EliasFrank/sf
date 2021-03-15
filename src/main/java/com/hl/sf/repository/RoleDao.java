package com.hl.sf.repository;

import com.hl.sf.entity.Role;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hl2333
 */
public interface RoleDao {

    @Select("select * from `role` where user_id = #{id}")
    List<Role> findRolesByUserId(Long id);

    void save(Role role);
}
