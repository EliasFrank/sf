package com.hl.sf.repository;

import com.hl.sf.entity.Subway;

/**
 * @author hl2333
 */
public interface SubwayDao {
    Subway findById(Long id);
}
