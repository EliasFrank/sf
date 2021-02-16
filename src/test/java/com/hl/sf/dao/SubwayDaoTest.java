package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.Subway;
import com.hl.sf.repository.SubwayDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SubwayDaoTest extends SfApplicationTests {

    @Autowired
    private SubwayDao subwayDao;

    @Test
    public void findById() {
        Subway byId = subwayDao.findById(1L);
        System.out.println(byId);
    }
}
