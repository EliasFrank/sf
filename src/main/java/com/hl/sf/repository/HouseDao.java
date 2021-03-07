package com.hl.sf.repository;

import com.hl.sf.entity.House;
import com.hl.sf.web.form.DatatableSearch;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Select("SELECT * FROM house where id = #{id}")
    House findById(Long id);


    void update(House house);

    @Update("update `house` set cover = #{path} where id = #{id}")
    void updateCover(@Param("id") Long targetId, @Param("path") String path);

    @Update("update `house` set status = #{status} where id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") int status);

    @Select("SELECT * FROM `house` where `status` = #{status}  and city_en_name = #{cityEnName} ORDER BY ${order} ${direction}")
    List<House> findAllByCondition(@Param("status") int status, @Param("cityEnName") String cityEnName, @Param("order")String order, @Param("direction")String direction);

    @Select("select * from house order by ${param1} ${param2}")
    List<House> testOrderBy(String order, String direction);
}
