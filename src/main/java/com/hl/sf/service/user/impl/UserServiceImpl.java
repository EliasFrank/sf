package com.hl.sf.service.user.impl;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.hl.sf.entity.Role;
import com.hl.sf.entity.UserInfo;
import com.hl.sf.repository.RoleDao;
import com.hl.sf.repository.UserDao;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.user.IUserService;
import com.hl.sf.utils.EncodePassword;
import com.hl.sf.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author hl2333
 */
@Service("realUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HttpSession session;

    @Override
    public UserInfo findUserByName(String userName) {
        return null;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        UserInfo user = userDao.findById(userId);
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    @Override
    public UserInfo findUserByTelephone(String telephone) {
        UserInfo userInfo = userDao.findUserByPhone(telephone);
        if (userInfo == null) {
            return null;
        }
        List<Role> roles = roleDao.findRolesByUserId(userInfo.getId());
        if (roles == null || roles.isEmpty()){
            throw new DisabledException("权限非法");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" +
                role.getName())));
        userInfo.setAuthorityList(authorities);
        return userInfo;
    }

    @Override
    public UserInfo addUserByPhone(String telephone) {
        UserInfo user = new UserInfo();
        user.setPhoneNumber(telephone);
        user.setUsername(telephone.substring(0, 3) + "****" + telephone.substring(7, telephone.length()));
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setLastUpdateTime(now);
        userDao.save(user);

        Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleDao.save(role);
        user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }

    @Override
    public ServiceResult modifyUserProfile(String profile, String value) {
        Long userId = getLoginUserId();
        if (profile == null || profile.isEmpty()) {
            return new ServiceResult(false, "属性不可以为空");
        }
        switch (profile) {
            case "name":
                userDao.updateUsername(userId, value);
                break;
            case "email":
                userDao.updateEmail(userId, value);
                break;
            case "password":
                userDao.updatePassword(userId, EncodePassword.encodePassword(value));
                break;
            default:
                return new ServiceResult(false, "不支持的属性");
        }
        return ServiceResult.success();
    }
    /*private static UserInfo load(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal != null && principal instanceof UserInfo){
            return (UserInfo) principal;
        }
        return null;
    }*/

    @Override
    public Long getLoginUserId(){
        Long loginUserId = Longs.tryParse(String.valueOf(session.getAttribute("loginUserId")));
        if (loginUserId == null){
            return -1L;
        }
        return loginUserId;
    }
}
