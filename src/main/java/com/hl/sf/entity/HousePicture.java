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
public class HousePicture {
    private Long id;

    private Long houseId;

    private String path;

    private String cdnPrefix;

    private int width;

    private int height;

    private String location;
}
