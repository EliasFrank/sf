package com.hl.sf.service.house;

import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.web.dto.SubwayDTO;
import com.hl.sf.web.dto.SubwayStationDTO;
import com.hl.sf.web.dto.SupportAddressDTO;

/**
 * @author hl2333
 */
public interface IAddressService {
    /**
     * 查询所有城市
     * @return 返回所有城市的信息
     */
    ServiceMultiResult<SupportAddressDTO> findAllCities();

    /**
     * 查询某个城市的所有区
     * @param city 要查询的城市
     * @return 所有区域
     */
    ServiceMultiResult<SupportAddressDTO> findAllRegions(String city);

    /**
     * 获取该城市的所有地铁线路
     * @param city 城市名称缩写
     * @return 所有地铁路线
     */
    ServiceMultiResult<SubwayDTO> findAllSubways(String city);

    /**
     * 查询某个地铁经过的所有地铁站
     * @param subwayId 地铁id
     * @return 经过的所有地铁站
     */
    ServiceMultiResult<SubwayStationDTO> findAllSubwayStations(Integer subwayId);
}
