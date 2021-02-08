package com.hl.sf.entity;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.repository.SupportAddressDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SupportAddressDaoTest extends SfApplicationTests {

    @Autowired
    SupportAddressDao supportAddressDao;
    @Test
    public void findAllByLevel() {
        List<SupportAddress> city = supportAddressDao.findAllByLevel("city");
        city.forEach(System.out::println);
    }
    @Test
    public void findAllRegionByCity() {
        List<SupportAddress> city = supportAddressDao.findAllRegionsByCity("bj");
        city.forEach(System.out::println);
    }

    @Test
    public void findAllSubwayByCity() {
        List<Subway> bj = supportAddressDao.findAllSubwayByCity("bj");
        bj.forEach(System.out::println);
    }
    @Test
    public void findAllSubwayStationBySubwayLine() {
        List<SubwayStation> bj = supportAddressDao.findAllSubwayStationBySubwayLine(4);
        bj.forEach(System.out::println);
    }

}
