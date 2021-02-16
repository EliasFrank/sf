package com.hl.sf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hl2333
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetail {
    private Long id;

    private Long houseId;

    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private String address;

    private Long subwayLineId;

    private Long subwayStationId;

    private String subwayLineName;

    private String subwayStationName;
}
