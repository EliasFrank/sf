package com.hl.sf.repository;

import com.hl.sf.entity.House;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 查询所有的房源信息
     * @return 所有的信息
     */
    @Select("select * from house")
    List<House> findAll();
}
