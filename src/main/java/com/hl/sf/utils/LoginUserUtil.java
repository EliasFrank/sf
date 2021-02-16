package com.hl.sf.utils;

import com.hl.sf.entity.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author hl2333
 */
public class LoginUserUtil {
    private static UserInfo load(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null && principal instanceof UserInfo){
            return (UserInfo) principal;
        }
        return null;
    }

    public static Long getLoginUserId(){
        UserInfo userInfo = load();
        if (userInfo == null) {
            return -1L;
        }
        return userInfo.getId();
    }
}
