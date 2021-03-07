package com.hl.sf.repository;

import com.hl.sf.entity.HouseDetail;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hl2333
 */
public interface HouseDetailDao {

    Integer save(HouseDetail houseDetail);

    @Select("SELECT * FROM house_detail where house_id = #{id}")
    HouseDetail findByHouseId(Long id);

    void update(HouseDetail detail);

    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
