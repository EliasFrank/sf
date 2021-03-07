package com.hl.sf.base;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 排序生成器
 * @author hl2333
 */
public class HouseSort {
    public static final String DEFAULT_SORT_KEY = "last_update_time";

    public static final String DISTANCE_TO_SUBWAY_KEY = "distance_to_subway";


    private static final Set<String> SORT_KEYS = Sets.newHashSet(
        DEFAULT_SORT_KEY,
            "create_time",
            "price",
            "area",
            DISTANCE_TO_SUBWAY_KEY
    );
}
