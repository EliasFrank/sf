package com.hl.sf.repository;

import com.hl.sf.entity.House;

/**
 * @author hl2333
 */
public interface HouseDao {
    /**
     * 保存房源信息
     * @param house 房源信息
     * @return 房源信息
     */
    Integer save(House house);
}