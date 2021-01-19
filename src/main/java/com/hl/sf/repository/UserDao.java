package com.hl.sf.repository;

import com.hl.sf.entity.User;

/**
 * @author hl2333
 */
public interface UserDao {
    User getUserByID(Long id);
}
