package com.hl.sf.service.search.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 索引结构模板
 * @author hl2333
 */

@JsonIgnoreProperties
@Data
public class HouseIndexTemplate {

    @JSONField(name = "house_id")
    private Long houseId;

    private String title;

    private int price;

    private int area;

    @JSONField(name = "create_time")
    private Date createTime;

    @JSONField(name = "last_update_time")
    private Date lastUpdateTime;

    @JSONField(name = "city_en_name")
    private String cityEnName;

    @JSONField(name = "region_en_name")
    private String regionEnName;

    private int direction;

    @JSONField(name = "distance_to_subway")
    private int distanceToSubway;

    @JSONField(name = "subway_lone_name")
    private String subwayLineName;

    @JSONField(name = "subway_station_name")
    private String subwayStationName;

    private String street;

    private String district;

    private String description;

    @JSONField(name = "layout_desc")
    private String layoutDesc;

    private String traffic;

    @JSONField(name = "round_service")
    private String roundService;

    @JSONField(name = "rent_way")
    private int rentWay;

    private List<String> tags;

    private List<HouseSuggest> suggest;

    private BaiduMapLocation location;

}
