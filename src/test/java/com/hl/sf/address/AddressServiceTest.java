package com.hl.sf.address;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.house.IAddressService;
import com.hl.sf.service.search.entity.BaiduMapLocation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AddressServiceTest extends SfApplicationTests {
    @Autowired
    private IAddressService addressService;

    @Test
    public void testGetMapLocation() {
        String city = "北京";
        String address = "北京市昌平区巩华家园1号楼2单元";

        ServiceResult<BaiduMapLocation> baiduMapLocation = addressService.getBaiduMapLocation(city, address);

        System.out.println(baiduMapLocation.getResult().getLongitude());
        System.out.println(baiduMapLocation.getResult().getLatitude());
    }
}
