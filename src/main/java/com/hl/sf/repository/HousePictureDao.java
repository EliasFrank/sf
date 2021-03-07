package com.hl.sf.repository;

import com.hl.sf.entity.HousePicture;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hl2333
 */
public interface HousePictureDao {
    void save(List<HousePicture> list);

    @Select("SELECT * FROM house_picture where house_id = #{id}")
    List<HousePicture> findAllByHouseId(Long id);

    void update(@Param("list") List<HousePicture> list);

    @Select("select * from house_picture where id = #{id}")
    HousePicture findById(Long id);

    @Delete("delete from house_picture where id = #{id}")
    void delete(Long id);
}
