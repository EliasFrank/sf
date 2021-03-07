package com.hl.sf.service.user.impl;

import com.hl.sf.entity.UserInfo;
import com.hl.sf.repository.UserDao;
import com.hl.sf.service.ServiceResult;
import com.hl.sf.service.user.IUserService;
import com.hl.sf.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hl2333
 */
@Service("realUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ModelMapper modelMapper;
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
}
