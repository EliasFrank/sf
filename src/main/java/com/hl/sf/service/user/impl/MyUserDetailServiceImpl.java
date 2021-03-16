package com.hl.sf.service.user.impl;

import com.hl.sf.entity.Role;
import com.hl.sf.entity.UserInfo;
import com.hl.sf.repository.UserDao;
import com.hl.sf.service.user.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hl2333
 */
@Service("userService")
public class MyUserDetailServiceImpl implements MyUserDetailService {
    @Autowired
    UserDao userDao;

    @Autowired
    HttpSession session;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        UserInfo userInfo = userDao.getUserByName(s);
        session.setAttribute("loginUserId", userInfo.getId());

        List<Role> roles = userInfo.getRoles();
        if(roles == null || roles.isEmpty()){
            throw new DisabledException("权限非法");
        }
        List<GrantedAuthority> authorities = getAuthorities(roles);

//        System.out.println(userInfo.getPassword());
        User user = new User(userInfo.getUsername(),
//                EncodePassword.encodePassword(userInfo.getPassword()),
                userInfo.getPassword(),
                userInfo.getStatus()==0?false:true,
                true, true, true,
                authorities);
//        System.out.println(user);
//        System.out.println(user.getPassword());
        return user;
    }

    /**
     * 将查询出来的角色权限转换为SpringSecurity可识别的权限
     * @param roles 查询出来的用户角色
     * @return 用户具有的所有权限
     */
    private List<GrantedAuthority> getAuthorities(List<Role> roles){
        List<GrantedAuthority> list = new ArrayList<>();

        for (Role role :
                roles) {
            list.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }
        return list;
    }
}
