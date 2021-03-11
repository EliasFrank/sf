package com.hl.sf.service.search;

import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.ServiceResult;
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

    ServiceResult<List<String>> suggest(String prefix);

    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);
}
