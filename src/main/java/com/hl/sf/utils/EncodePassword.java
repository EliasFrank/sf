package com.hl.sf.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author hl2333
 */
public class EncodePassword {
    public static BCryptPasswordEncoder passwordEncoder
            = new BCryptPasswordEncoder();

    public static String encodePassword(String password){
        return passwordEncoder.encode(password);
    }
}
