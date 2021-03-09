package com.hl.sf.service.search;

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

}
