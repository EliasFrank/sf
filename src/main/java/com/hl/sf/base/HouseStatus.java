package com.hl.sf.base;


/**
 * 房源状态
 * @author hl2333
 */
public enum HouseStatus {
    /**
     * 房源状态
     */
    NOT_AUDITED(0), // 未审核
    PASSES(1), // 审核通过
    RENTED(2), // 已出租
    DELETED(3); // 逻辑删除
    private int value;

    HouseStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
