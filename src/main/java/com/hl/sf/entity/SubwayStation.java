package com.hl.sf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hl2333
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubwayStation {
    private Long id;
    private Long subwayId;
    private String name;

}
