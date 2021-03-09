package com.hl.sf.search;

import com.hl.sf.SfApplicationTests;
import com.hl.sf.service.search.ISearchService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceImplTest extends SfApplicationTests {

    @Autowired
    private ISearchService searchService;
    @Test
    public void testIndex() {
        Long targetHouseId = 15L;
        boolean success = searchService.index(targetHouseId);
        System.out.println(success);
    }

    @Test
    public void testRemove() {
        Long targerHouseId = 15L;
        boolean remove = searchService.remove(targerHouseId);
        System.out.println(remove);

    }
}
