package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.HouseSubscribe;
import com.hl.sf.repository.HouseSubscribeDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SubscribeDaoTest extends SfApplicationTests {
    @Autowired
    HouseSubscribeDao subscribeDao;

    @Test
    public void findAllByUserIdAndStatusTest() {
        List<HouseSubscribe> allByUserIdAndStatus = subscribeDao.
                findAllByUserIdAndStatus(1L, 1, "create_time", "desc");
        allByUserIdAndStatus.forEach(System.out::println);
    }
}
