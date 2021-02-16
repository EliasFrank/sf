package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.House;
import com.hl.sf.repository.HouseDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class HouseDaoTest extends SfApplicationTests {
    @Autowired
    HouseDao houseDao;

    @Test
    public void save() {
        House house = new House();
        house.setAdminId(1L);
        house.setLastUpdateTime(new Date());
        house.setCreateTime(new Date());
        house.setWatchTimes(1);
        house.setTotalFloor(3);
        house.setTitle("hello");
        house.setStreet("jfsdfj");
        house.setStatus(1);
        house.setRoom(32);
        house.setRegionEnName("fds");
        house.setPrice(7878);
        house.setParlour(12);
        house.setFloor(15);
        house.setDistrict("fasdfdsa");
        house.setDistanceToSubway(1);
        house.setDirection(12);
        house.setCover("ds");
        house.setCityEnName("bj");
        house.setBuildYear(3223);
        house.setBathroom(12);
        house.setArea(321);

        System.out.println(house.getId());
        houseDao.save(house);
        System.out.println(house.getId());

    }
}
