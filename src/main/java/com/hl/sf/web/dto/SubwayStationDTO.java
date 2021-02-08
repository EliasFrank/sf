package com.hl.sf.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hl2333
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubwayStationDTO {
    private Long id;
    private Long subwayId;
    private String name;
}
