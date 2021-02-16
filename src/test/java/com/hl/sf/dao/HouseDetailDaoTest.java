package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.HouseDetail;
import com.hl.sf.repository.HouseDetailDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HouseDetailDaoTest extends SfApplicationTests {

    @Autowired
    HouseDetailDao houseDetailDao;

    @Test
    public void save() {
        HouseDetail houseDetail = new HouseDetail();
        houseDetail.setHouseId(12L);
        houseDetail.setAddress("addsa");
        houseDetail.setRentWay(3920);
        houseDetailDao.save(houseDetail);
        System.out.println(houseDetail.getId());
    }
}
