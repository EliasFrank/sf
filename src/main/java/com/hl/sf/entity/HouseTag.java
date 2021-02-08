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
public class HouseTag {
    private Long id;

    private Long houseId;

    private String name;
}
