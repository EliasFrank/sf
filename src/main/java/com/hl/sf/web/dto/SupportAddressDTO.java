package com.hl.sf.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author hl2333
 */
@Data
public class SupportAddressDTO {
    private Long id;

    @JsonProperty(value = "belong_to")
    private String belongTo;

    @JsonProperty(value = "en_name")
    private String enName;

    @JsonProperty(value = "cn_name")
    private String cnName;

    private String level;

    @JsonProperty(value = "baidu_map_lng")
    private Double baiduMapLng;

    @JsonProperty(value = "baidu_map_lat")
    private Double baiduMapLat;
}
