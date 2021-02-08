package com.hl.sf.repository;

import com.hl.sf.entity.Subway;
import com.hl.sf.entity.SubwayStation;
import com.hl.sf.entity.SupportAddress;

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

}
