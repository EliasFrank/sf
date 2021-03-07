package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.HouseTag;
import com.hl.sf.repository.HouseTagDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class HouseTagDaoTest extends SfApplicationTests {

    @Autowired
    private HouseTagDao houseTagDao;
    @Test
    public void name() {
        ArrayList<Long> objects = new ArrayList<>();
        objects.add(15L);
        objects.add(16L);
        objects.add(17L);
        List<HouseTag> allByHouseIdIn = houseTagDao.findAllByHouseIdIn(objects);
        allByHouseIdIn.forEach(System.out::println);
    }
}
