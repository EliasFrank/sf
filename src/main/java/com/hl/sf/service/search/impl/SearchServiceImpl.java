package com.hl.sf.service.search.impl;

import com.hl.sf.repository.HouseDao;
import com.hl.sf.service.search.ISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hl2333
 */
@Service("searchService")
public class SearchServiceImpl implements ISearchService {
    @Autowired
    private HouseDao houseDao;


    @Override
    public void index(Long houseId) {

    }

    @Override
    public void remove(Long houseId) {

    }
}
