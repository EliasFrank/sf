package com.hl.sf.web.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hl2333
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDetailDTO {
    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private Long adminId;

    private String address;

    private Long subwayLineId;

    private Long subwayStationId;

    private String subwayLineName;

    private String subwayStationName;

}
