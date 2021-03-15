package com.hl.sf.service.user;

import com.hl.sf.entity.UserInfo;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.web.dto.UserDTO;

/**
 * @author hl2333
 */
public interface IUserService {
    UserInfo findUserByName(String userName);

    ServiceResult<UserDTO> findById(Long userId);

    UserInfo findUserByTelephone(String telephone);

    UserInfo addUserByPhone(String telephone);
}
