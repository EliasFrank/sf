package com.hl.sf.entity;

import lombok.Data;

/**
 * @author hl2333
 */
@Data
public class User {
    private Long id;

    private String name;

    private String password;

    private String email;

    private String phoneNumber;

    private String createTime;

    private String lastLoginTime;

    private String lastUpdateTime;

    private String avatar;

}
