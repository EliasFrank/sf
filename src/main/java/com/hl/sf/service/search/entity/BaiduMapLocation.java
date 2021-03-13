package com.hl.sf.service.search.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 百度位置信息
 * @author hl2333
 */
@Data
public class BaiduMapLocation {
    // 经度
    @JSONField(name = "lon")
    private double longitude;

    // 纬度
    @JSONField(name = "lat")
    private double latitude;

}
