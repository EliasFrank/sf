package com.hl.sf.config;

import com.hl.sf.security.LoginAuthFailHandler;
import com.hl.sf.security.LoginUrlEntryPoint;
import com.hl.sf.service.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author hl2333
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("userService")
    MyUserDetailService userDetailService;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.formLogin();
//        http.csrf().ignoringAntMatchers("/druid/*");
        http.headers().frameOptions().sameOrigin();
        http.csrf().disable();

        http.authorizeRequests()
                //管理员登录入口
                .antMatchers("/admin/login").permitAll()
                //静态资源
                .antMatchers("/static/**").permitAll()
                //用户登录
                .antMatchers("/user/login").permitAll()
                .antMatchers("/hello").hasRole("USER")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("api/user/**").hasAnyRole("ADMIN", "USER")
                .and()
                .formLogin()
                //配置角色登录处理入口
                .loginProcessingUrl("/login")
                .failureHandler(authFailHandler())
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(urlEntryPoint())
                .accessDeniedPage("/403")
                .and();

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(bCryptPasswordEncoder());
    }
    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(new PasswordEncoder() {

            对传入的密码进行加密
            @Override
            public String encode(CharSequence charSequence) {
                return EncodePassword.encodePassword(charSequence.toString());
            }

            比较加密后得传入的密码和数据库中查询出来的密码
            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return passwordEncoder.matches(EncodePassword.encodePassword(charSequence.toString()),s);
            }
        });
    }*/
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LoginUrlEntryPoint urlEntryPoint(){
        return new LoginUrlEntryPoint("/user/login");
    }

    @Bean
    public LoginAuthFailHandler authFailHandler(){
        return new LoginAuthFailHandler(urlEntryPoint());
    }
}
