package com.hl.sf.entity;

import lombok.Data;

/**
 * @author hl2333
 */
@Data
public class SupportAddress {
    private Long id;
    private String belongTo;
    private String enName;
    private String cnName;
    private String level;
    private double baiduMapLng;
    private double baiduMapLat;

    /**
     * 定义行政级别
     */
    public enum Level{

        /**
         * 行政级别是城市还是区
         */
        CITY("city"),
        REGION("region");
        private String value;

        Level(String value){
            this.value = value;
        }
        public String getValue(){
            return value;
        }

        public static Level of(String value){
            for (Level level : Level.values()){
                if (level.getValue().equals(value)){
                    return level;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
