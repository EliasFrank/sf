package com.hl.sf.search;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.service.ServiceMultiResult;
import com.hl.sf.service.search.ISearchService;
import com.hl.sf.web.form.RentSearch;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceImplTest extends SfApplicationTests {

    @Autowired
    private ISearchService searchService;
    @Test
    public void testIndex() {
        Long targetHouseId = 15L;
        searchService.index(targetHouseId);
    }

    @Test
    public void testRemove() {
        Long targerHouseId = 15L;
        searchService.remove(targerHouseId);

    }

    @Test
    public void testQuery() {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(3);
        ServiceMultiResult<Long> query = searchService.query(rentSearch);
        query.getResult().forEach(System.out::println);
    }
}
