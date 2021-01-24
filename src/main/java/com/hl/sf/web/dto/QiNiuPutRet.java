package com.hl.sf.web.dto;

import lombok.Data;

/**
 * @author hl2333
 */
@Data
public class QiNiuPutRet {
    private String key;
    private String hash;
    private String bucket;
    private String fname;
    private String fsize;
    private Integer width;
    private Integer height;
}
