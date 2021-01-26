package com.hl.sf.service.house.impl;

import com.hl.sf.service.house.IQiNiuService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * @author hl2333
 */
@Service("qiNiuService")
public class QiNiuServiceImpl implements IQiNiuService, InitializingBean {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private BucketManager bucketManager;

    @Autowired
    private Auth auth;

    @Value("${qiniu.Bucket}")
    private String bucket;

    private StringMap putPolicy;
    @Override
    public Response uploadFile(File file) throws QiniuException {
        Response put = this.uploadManager.put(file, null, getUploadToken());

        int retry = 0;
        while(put.needRetry() && retry < 3){
            this.uploadManager.put(file, null, getUploadToken());
            retry++;
        }
        return put;
    }

    @Override
    public Response uploadFile(InputStream inputStream) throws QiniuException {
        Response put = this.uploadManager.put(inputStream, null, getUploadToken(), null, null);

        int retry = 0;
        while(put.needRetry() && retry < 3){
            this.uploadManager.put(inputStream, null, getUploadToken(), null, null);
            retry++;
        }
        return put;
    }

    @Override
    public Response deleteFile(String key) throws QiniuException {
        Response hlsf = bucketManager.delete("hlsf", key);
        int retry = 0;
        while(hlsf.needRetry() && retry < 3){
            bucketManager.delete("hlsf", key);
            retry++;
        }
        return hlsf;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.putPolicy = new StringMap();

        putPolicy.put("returnBody",
                "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":\"$(imageInfo.width)\",\"height\":\"$(imageInfo.height)\"}");
    }
    /**
     * 获取上传凭证
     */
    private String getUploadToken(){
        return this.auth.uploadToken(bucket, null, 3600, putPolicy);
    }
}
