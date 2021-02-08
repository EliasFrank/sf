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
public class Subway {
    private Long id;
    private String name;
    private String cityEnName;
}
