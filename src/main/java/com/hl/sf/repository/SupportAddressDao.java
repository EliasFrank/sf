package com.hl.sf.repository;

import com.hl.sf.entity.Subway;
import com.hl.sf.entity.SubwayStation;
import com.hl.sf.entity.SupportAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hl2333
 */
public interface SupportAddressDao {
    /**
     * 获取所有对应行政级别的信息
     * @param level 级别
     * @return 所有的行政级别信息
     */
    List<SupportAddress> findAllByLevel(String level);

    /**
     * 获取某个城市的所有区
     * @param city 用户选择的城市
     * @return 用户选择城市的所有区
     */
    List<SupportAddress> findAllRegionsByCity(String city);

    /**
     * 获取某个城市所有的地铁
     * @param city 当前城市
     * @return 所有地铁的信息
     */
    List<Subway> findAllSubwayByCity(String city);

    /**
     * 获取某个地铁所经过的所有地铁站
     * @param subwayId
     * @return
     */
    List<SubwayStation> findAllSubwayStationBySubwayLine(Integer subwayId);

    /**
     * 根据缩写名称和城市等级，获取城市的具体信息
     * @param enName 城市缩写名称
     * @param level 等级
     * @return 具体信息
     */
    SupportAddress findByEnNameAndLevel(@Param("enName") String enName, @Param("level") String level);

    /**
     * 根据所属地的缩写查询区域的信息
     * @param regionEnName 区域缩写
     * @param belongTo 属于什么城市
     * @return 区域的详细信息
     */
    SupportAddress findByEnNameAndBelongTo(@Param("enName")String regionEnName, @Param("level") String belongTo);

}
