package com.syy.security.chapter04.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.syy.security.chapter04.constants.NormalConstants;
import com.syy.security.chapter04.service.impl.UserService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

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

    @Resource
    private MyWebAuthenticationDetailsSource myWebAuthenticationDetailsSource;

    @Resource
    private FindByIndexNameSessionRepository sessionRepository;

    /**
     * session 整合 redis，集群 session 管理
     * <p>
     * 分布式 共享 session 解决方案：redis
     * <p>
     * 需要 去掉 配置的 HttpSessionEventPublisher，因为它是 session 内存管理策略，
     * 会使用 security 提供的 内存 session 注册表。
     *
     * @return SpringSessionBackedSessionRegistry
     */
    @Bean
    protected SpringSessionBackedSessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }

    @Bean
    protected JdbcTokenRepositoryImpl jdbcTokenRepository() {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        // 配置 spring security 的 密码加密方式。
        return new BCryptPasswordEncoder(10);
        /*return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return !StringUtils.isBlank(encodedPassword) && encodedPassword.equals(rawPassword.toString());
            }
        };*/
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
     * 可以将 session 创建以及销毁的事件及时感知到，并且调用 Spring 中的事件机制将相关的创建和销毁事件发布出去，
     * 进而被 Spring Security 感知到，从而可以 在 {@link #configure(HttpSecurity)} 控制 客户端 sesssion 的登录数量
     *
     * @return HttpSessionEventPublisher
     */
    /*@Bean
    protected HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }*/

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
     * 查看 权限控制 比较完整的配置 {@link com.syy.security.chapter03.config.SecurityConfig#configure(HttpSecurity)}
     *
     * @param http 入参
     * @throws Exception 异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 关闭 csrf 功能
        // http.csrf().disable()

        // 配置 csrf 防御功能
        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                // 设置 客户端最大登录次数 为 1，超过次数后 默认会直接 踢掉 最先登录的客户端
                .sessionManagement()
                .maximumSessions(1)
                // 客户端登录 达到最大次数后，禁止新的客户端登录
                .maxSessionsPreventsLogin(true);

        http.authorizeRequests()
                // 指定 任何人 都允许使用URL，不需要登陆。
                .antMatchers("/vc.jpg").permitAll()
                // 指定 其它URL，只允许被经过身份验证的用户访问
                .anyRequest().authenticated();

        http.formLogin()
                .authenticationDetailsSource(myWebAuthenticationDetailsSource)
                .loginPage("/login.html")
                .loginProcessingUrl(NormalConstants.SECURITY_LOGIN_URL)

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
                }).permitAll();

        // 配置 remember me
        http.rememberMe()
                .tokenRepository(jdbcTokenRepository());

        // 注销登录后的跳转以及操作
        http.logout()
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

