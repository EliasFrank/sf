package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.Role;
import com.hl.sf.repository.RoleDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleDaoTest extends SfApplicationTests {

    @Autowired
    private RoleDao roleDao;
    @Test
    public void testSvae() {
        Role role = new Role();
        role.setUserId(1L);
        role.setName("ADMIN");
        roleDao.save(role);
    }
}
