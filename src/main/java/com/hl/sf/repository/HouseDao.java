package com.hl.sf.repository;

import com.hl.sf.entity.House;
import com.hl.sf.web.form.DatatableSearch;

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
     * @param searchBody 需要拼接的条件
     * @return 所有的信息
     */
    List<House> findAll(DatatableSearch searchBody);
}
