package com.hl.sf.security;

import com.hl.sf.entity.UserInfo;
import com.hl.sf.service.ISmsService;
import com.hl.sf.service.user.IUserService;
import com.hl.sf.utils.LoginUserUtil;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author hl2333
 */
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    @Qualifier("realUserService")
    private IUserService userService;

    @Autowired
    @Qualifier("smsService")
    private ISmsService smsService;
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String name = obtainUsername(request);
        if (!Strings.isNullOrEmpty(name)) {
            request.setAttribute("username", name);
            return super.attemptAuthentication(request, response);
        }

        String telephone = request.getParameter("telephone");
        if (Strings.isNullOrEmpty(telephone) || !LoginUserUtil.checkTelephone(telephone)) {
            throw  new BadCredentialsException("wrong telephone number");
        }

        UserInfo user = userService.findUserByTelephone(telephone);
        String inputCode = request.getParameter("smsCode");
        String sessionCode = smsService.getSmsCode(telephone);
        if (Objects.equals(inputCode, sessionCode)) {
            //如果用户第一次用手机登录，则自动注册该用户
            if (user == null) {
                user = userService.addUserByPhone(telephone);
            }
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorityList());
        } else {
            throw new BadCredentialsException("smsCodeError");
        }
    }
}
