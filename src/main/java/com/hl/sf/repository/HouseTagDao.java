package com.hl.sf.repository;

import com.hl.sf.entity.HouseTag;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hl2333
 */
public interface HouseTagDao {
    void save(List<HouseTag> list);

    @Select("SELECT * FROM house_tag where house_id = #{id}")
    List<HouseTag> findAllByHouseId(Long id);

    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);
}
