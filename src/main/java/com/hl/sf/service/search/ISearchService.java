package com.hl.sf.service.search;

import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.web.dto.HouseBucketDTO;
import com.hl.sf.web.form.MapSearch;
import com.hl.sf.web.form.RentSearch;

import java.util.List;

/**
 * @author hl2333
 */
public interface ISearchService {
    /**
     * 查找目标房源
     * @param houseId
     * @return
     */
    void index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     * 获取自动补全
     * @param prefix
     * @return
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合特定小区的房间数
     * @param cityEnName
     * @param regionEnName
     * @param district
     * @return
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);

    /**
     * 聚合城市
     * @param city
     * @return
     */
    ServiceMultiResult<HouseBucketDTO> mapAggregate(String city);

    ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy,
                                      String orderDirection, int start, int size);

    /**
     * 精确数据查询范围
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<Long> mapQuery(MapSearch mapSearch);
}
