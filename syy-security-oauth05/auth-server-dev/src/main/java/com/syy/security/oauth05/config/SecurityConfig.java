package com.syy.security.oauth05.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Description: 认证服务
 * @Author: cuiweiman
 * @Since: 2021/6/8 下午5:13
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("cwm").password(new BCryptPasswordEncoder().encode("250")).roles("admin")
                .and()
                .withUser("syy").password(new BCryptPasswordEncoder().encode("520")).roles("user");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 关闭 csrf， 并开启默认 form-login 登录配置
        http.csrf().disable().formLogin();
    }

}
