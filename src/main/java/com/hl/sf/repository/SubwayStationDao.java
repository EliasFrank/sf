package com.hl.sf.repository;

import com.hl.sf.entity.SubwayStation;

/**
 * @author hl2333
 */
public interface SubwayStationDao {
    SubwayStation findById(Long id);
}
