package com.hl.sf.mapper;

import com.hl.sf.entity.User;

/**
 * @author hl2333
 */
public interface UserDao {
    User getUserByID(Long id);
}
