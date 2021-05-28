package com.syy.security.chapter04.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.syy.security.chapter04.constants.NormalConstants;
import com.syy.security.chapter04.service.impl.UserService;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.PrintWriter;

/**
 * @Description: security 配置
 * @Author: cuiweiman
 * @Since: 2021/5/28 下午4:42
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private UserService userService;

    @Resource
    private DataSource dataSource;

    @Bean
    protected JdbcTokenRepositoryImpl jdbcTokenRepository() {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return !StringUtils.isBlank(encodedPassword) && encodedPassword.equals(rawPassword.toString());
            }
        };
    }

    /**
     * 配置自定义的 DaoAuthenticationProvider 子类
     *
     * @return MyAuthenticationProvider
     */
    @Bean
    protected MyAuthenticationProvider myAuthenticationProvider() {
        MyAuthenticationProvider myAuthenticationProvider = new MyAuthenticationProvider();
        myAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        myAuthenticationProvider.setUserDetailsService(userService);
        return myAuthenticationProvider;
    }

    /**
     * 引入 自定义的 MyAuthenticationProvider
     *
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(Lists.newArrayList(myAuthenticationProvider()));
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
     * 查看比较完整的配置 {@link com.syy.security.chapter03.config.SecurityConfig#configure(HttpSecurity)}
     *
     * @param http 入参
     * @throws Exception 异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/vc.jpg").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/login.html").loginProcessingUrl(NormalConstants.SECURITY_LOGIN_URL)
                .successHandler((req, resp, auth) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(auth.getPrincipal()));
                    out.flush();
                    out.close();
                })
                .failureHandler((req, resp, e) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(e.getMessage()));
                    out.flush();
                    out.close();
                }).permitAll()

                // 配置 remember me
                .and().rememberMe().rememberMeParameter("rememberMe")
                .tokenRepository(jdbcTokenRepository())

                // 注销登陆后的跳转以及操作
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", HttpMethod.GET.name()))
                .logoutSuccessHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write("注销成功");
                    out.flush();
                    out.close();
                }).permitAll()

                .and().csrf().disable();

    }
}
