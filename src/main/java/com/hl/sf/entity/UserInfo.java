package com.hl.sf.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;
import java.util.List;

/**
 * @author hl2333
 */
@Data
public class UserInfo {
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    private Date createTime;

    private Date lastLoginTime;

    private Date lastUpdateTime;

    private String avatar;

    private int status;

    private List<Role> roles;

    private List<GrantedAuthority> authorityList;
}
