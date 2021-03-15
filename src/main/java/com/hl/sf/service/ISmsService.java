package com.hl.sf.service;

/**
 * @author hl2333
 */
public interface ISmsService {
    /**
     * 发送验证码到指定手机，并缓存验证码10分钟及请求间隔时间一分钟
     * @param telephone
     * @return
     */
    ServiceResult<String> sendSms(String telephone);

    /**
     * 获取缓存中的验证码
     * @param telephone
     * @return
     */
    String getSmsCode(String telephone);

    /**
     * 移除指定手机号的验证码缓存
     * @param telephone
     */
    void remove(String telephone);

}
