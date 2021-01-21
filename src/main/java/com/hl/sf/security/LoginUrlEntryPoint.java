package com.hl.sf.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private PathMatcher pathMatcher = new AntPathMatcher();
    private final Map<String, String> authEntityPointMap;
    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
        authEntityPointMap = new HashMap<>();

        authEntityPointMap.put("/admin/**", "/admin/login");
        authEntityPointMap.put("/user/**", "/user/login");
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        String uri = request.getRequestURI().replace(request.getContextPath(), "");
        for (Map.Entry<String, String> authEntry :
                this.authEntityPointMap.entrySet()) {
            if (this.pathMatcher.match(authEntry.getKey(), uri)){
                return authEntry.getValue();
            }
        }
        return super.determineUrlToUseForThisRequest(request, response, exception);
    }
}
