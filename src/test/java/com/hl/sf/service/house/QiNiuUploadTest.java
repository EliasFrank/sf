package com.hl.sf.service.house;

import com.hl.sf.SfApplicationTests;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;

public class QiNiuUploadTest extends SfApplicationTests {

    @Autowired
    @Qualifier("qiNiuService")
    private IQiNiuService qiNiuService;

    @Test
    public void upload() {
        String fileName = "tmp/img-5820f8ff46202ce4b632aadb173be39a.jpg";
        File file = new File(fileName);

        Assert.assertTrue(file.exists());

        try {
            Response response = qiNiuService.uploadFile(file);
            Assert.assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void delete() {
        try {
            Response response = qiNiuService.deleteFile("FketGEIapy-AB9OsDHpkdaeFkpLv");
            Assert.assertTrue(response.isOK());
            System.out.println(response.getInfo());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
