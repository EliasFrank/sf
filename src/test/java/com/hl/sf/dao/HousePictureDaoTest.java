package com.hl.sf.dao;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.entity.HousePicture;
import com.hl.sf.repository.HousePictureDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public class HousePictureDaoTest extends SfApplicationTests {

    @Autowired
    HousePictureDao housePictureDao;

    @Test
    public void save() {
        HousePicture housePicture = new HousePicture();
        housePicture.setCdnPrefix("aa");
        housePicture.setWidth(21);
        housePicture.setPath("dfas");
        housePicture.setLocation("sda");
        housePicture.setHouseId(1L);
        housePicture.setHeight(45);

        HousePicture housePicture2 = new HousePicture();
        housePicture2.setCdnPrefix("aa");
        housePicture2.setWidth(21);
        housePicture2.setPath("dfas");
        housePicture2.setLocation("sda");
        housePicture2.setHouseId(1L);
        housePicture2.setHeight(45);
        ArrayList<HousePicture> housePictures = new ArrayList<>();
        housePictures.add(housePicture);
        housePictures.add(housePicture2);
        housePictureDao.save(housePictures);

        System.out.println(housePictures.get(0).getId());
        System.out.println(housePictures.get(1).getId());
    }
}
