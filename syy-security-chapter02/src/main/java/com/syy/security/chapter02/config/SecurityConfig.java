package com.syy.security.chapter02.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.PrintWriter;

/**
 * @Description: security 配置
 * @Author: cuiweiman
 * @Since: 2021/5/25 下午8:07
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 必须配置
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * 配置 验证的 用户名和密码
     * 有两种方式，重写 {@link WebSecurityConfigurerAdapter#configure(AuthenticationManagerBuilder)}
     * 或者 重写：{@link WebSecurityConfigurerAdapter#userDetailsService()}
     *
     * @param auth AuthenticationManagerBuilder身份校验器
     * @throws Exception 异常
     */
    /*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("syy").password("520").roles("myGirl")
                .and().withUser("cwm").password("250").roles("me");
    }*/
    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        // 普通用户
        userDetailsManager.createUser(User.withUsername("syy").password("520").roles("myGirl").build());
        // 管理员
        userDetailsManager.createUser(User.withUsername("cwm").password("250").roles("me").build());
        return userDetailsManager;
    }

    /**
     * 配置 角色 继承关系
     * 需要手动添加 ROLE_ 的前缀
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
        web.ignoring().antMatchers("/js/**", "/css/**", "/images/**");
    }


    /**
     * 自定义配置 登录页面等信息
     * <p>登录成功后的跳转：
     * {@link AbstractAuthenticationFilterConfigurer#successHandler}
     * {@link AbstractAuthenticationFilterConfigurer#failureHandler}
     *
     * @param http http
     * @throws Exception 异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 关闭 csrf
        http.csrf().disable()
                // 配置 没有登录时的返回信息
                .exceptionHandling()
                .authenticationEntryPoint((req, resp, authException) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write("尚未登录，请先登录：" + authException.getMessage());
                })
                .and().authorizeRequests()

                .antMatchers("/me/**").hasRole("me")
                .antMatchers("/myGirl/**").hasRole("myGirl")

                // 其他请求路径的处理方式
                .anyRequest().authenticated()

                // 配置表单登录,登录成功的返回返回，以及登录失败的返回信息
                .and().formLogin()
                .loginPage("/login.html").loginProcessingUrl("/doLogin")

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
                    out.write("登录失败：" + e.getMessage());
                    out.flush();
                    out.close();
                }).permitAll()

                // 注销登录后的跳转以及操作
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
