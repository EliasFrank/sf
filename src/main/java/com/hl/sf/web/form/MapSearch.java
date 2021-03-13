package com.hl.sf.web.form;

import lombok.Data;

/**
 * @author hl2333
 */
@Data
public class MapSearch {
    private String cityEnName;

    /**
     * 地图缩放级别
     */
    private int level = 12;
    private String orderBy = "last_update_time";
    private String orderDirection = "desc";
    /**
     * 左上角
     */
    private Double leftLongitude;
    private Double leftLatitude;

    /**
     * 右下角
     */
    private Double rightLongitude;
    private Double rightLatitude;

    private int start = 0;
    private int size = 5;

}
