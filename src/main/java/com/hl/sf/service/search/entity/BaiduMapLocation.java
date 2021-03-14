package com.hl.sf.service.search.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 百度位置信息
 * @author hl2333
 */
@Data
public class BaiduMapLocation {
    // 经度
    @JsonProperty("lon")
    @JSONField(name = "lon")
    private double longitude;

    // 纬度
    @JSONField(name = "lat")
    @JsonProperty("lat")
    private double latitude;

}
