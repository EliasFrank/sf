package com.hl.sf.repository;

import com.hl.sf.entity.HouseSubscribe;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author hl2333
 */
public interface HouseSubscribeDao {

    @Select("select * from house_subscribe where house_id = #{houseId} and user_id = #{userId}")
    HouseSubscribe findByHouseIdAndUserId(@Param("houseId") Long houseId, @Param("userId") Long userId);

    void save(HouseSubscribe subscribe);

    @Select("select * from house_subscribe where user_id = #{userId} and `status` = #{status} order by ${createTime} ${order}")
    List<HouseSubscribe> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") int value,
    @Param("createTime")  String createTime, @Param("order") String order);

    void update(HouseSubscribe subscribe);

    @Delete("delete from house_subscribe where id = #{id}")
    void delete(Long id);

    @Select("select * from house_subscribe where admin_id = #{userId} and `status` = #{status} ORDER BY ${orderTime} ${desc}")
    List<HouseSubscribe> findAllByAdminIdAndStatus(@Param("userId") Long userId, @Param("status") int status,
                                                   @Param("orderTime") String orderTime,
                                                   @Param("desc") String desc);

    @Select("select * from house_subscribe where admin_id = #{adminId} and `house_id` = #{houseId}")
    HouseSubscribe findByHouseIdAndAdminId(@Param("houseId") Long houseId, @Param("adminId") Long adminId);


    @Update("UPDATE house_subscribe SET `status` = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id,@Param("status") int status);
}
