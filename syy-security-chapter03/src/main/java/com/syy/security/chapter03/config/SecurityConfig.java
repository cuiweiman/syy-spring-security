package com.syy.security.chapter03.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syy.security.chapter03.constants.NormalConstants;
import com.syy.security.chapter03.filters.VerifyCodeFilter;
import com.syy.security.chapter03.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * {@link UsernamePasswordAuthenticationFilter} 在 Spring Security 中，认证与授权的相关校验都是在一系列的过滤器链中完成的，这是和认证相关的过滤器
 * {@link AbstractAuthenticationProcessingFilter} Spring Security 验证过程；
 * {@link AbstractAuthenticationProcessingFilter#doFilter} Spring Security 验证过程；
 * {@link AbstractAuthenticationProcessingFilter#successfulAuthentication} Spring Security 从 session 中获取用户登陆信息；
 *
 * @Description: 配置数据库 验证用户账号以及权限
 * @Author: cuiweiman
 * @Since: 2021/5/26 上午11:55
 */
@Slf4j
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private UserService userService;

    @Resource
    private VerifyCodeFilter verifyCodeFilter;

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return Objects.equals(encodedPassword, rawPassword.toString());
            }
        };
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);

    }


    /**
     * 配置 角色 继承关系: 需要手动添加 ROLE_ 的前缀
     *
     * @return 角色继承关系
     */
    @Bean
    RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_me > ROLE_myGirl");
        return roleHierarchy;
    }

    /**
     * 配置 忽略拦截 的地址，一般为 静态目录地址
     *
     * @param web web
     * @throws Exception 异常
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**", "/css/**", "/images/**", "/verifyCode/**");
    }


    /**
     * 自定义配置 登陆页面等信息
     *
     * @param http http
     * @throws Exception 异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(verifyCodeFilter, UsernamePasswordAuthenticationFilter.class);
        // 关闭 csrf
        http.csrf().disable()
                // 配置 没有登陆时的返回信息
                .exceptionHandling()
                .authenticationEntryPoint((req, resp, authException) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write("尚未登陆，请先登陆：" + authException.getMessage());
                })
                .and().authorizeRequests()

                .antMatchers("/me/**").hasRole("me")
                .antMatchers("/myGirl/**").hasRole("myGirl")

                // 其他请求路径的处理方式
                .anyRequest().authenticated()

                // 配置表单登陆,登陆成功的返回返回，以及登陆失败的返回信息
                .and().formLogin()
                .loginPage("/login.html").loginProcessingUrl(NormalConstants.SECURITY_LOGIN_URL)

                .successHandler((req, resp, authentication) -> {
                    Object principal = authentication.getPrincipal();
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(principal));
                    out.flush();
                    out.close();
                })

                .failureHandler((req, resp, e) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write("登陆失败：" + e.getMessage());
                    out.flush();
                    out.close();
                }).permitAll()

                // 注销登陆后的跳转以及操作
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", HttpMethod.GET.name()))
                .logoutSuccessHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write("注销成功");
                    out.flush();
                    out.close();
                }).permitAll();
    }
}
