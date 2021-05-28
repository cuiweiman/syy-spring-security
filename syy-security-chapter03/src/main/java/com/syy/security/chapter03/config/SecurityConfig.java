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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * {@link UsernamePasswordAuthenticationFilter} 在 Spring Security 中，认证与授权的相关校验都是在一系列的过滤器链中完成的，这是和认证相关的过滤器
 * {@link AbstractAuthenticationProcessingFilter} Spring Security 验证过程；
 * {@link AbstractAuthenticationProcessingFilter#doFilter} Spring Security 验证过程；
 * {@link AbstractAuthenticationProcessingFilter#successfulAuthentication} Spring Security 从 session 中获取用户登陆信息；
 * <p>
 * {@link AbstractRememberMeServices} Remember Me 功能,配置在{@link #configure(HttpSecurity)}，
 * 但是存在登陆隐患，若令牌被盗用，可能被，因此要采用一些措施保证安全行：
 * 1. 持久化令牌：
 * 1.1 在持久化令牌中，新增了两个经过 MD5 散列函数计算的校验参数，一个是 series，另一个是 token。
 * 其中，series 只有当用户在使用用户名/密码登录时，才会生成或者更新，
 * 而 token 只要有新的会话，就会重新生成，这样就可以避免一个用户同时在多端登录，
 * 就像手机 QQ ，一个手机上登录了，就会踢掉另外一个手机的登录，这样用户就会很容易发现账户是否泄漏。
 * 1.2 {@link PersistentRememberMeToken} 令牌保存处理类。
 * 1.3 首先需要一张表来记录令牌信息，这张表可以完全自定义，也可以使用系统默认提供的 JDBC 来操作，
 * 如果使用默认的 JDBC，即 {@link JdbcTokenRepositoryImpl}。
 * <pre class="code">
 * CREATE TABLE `persistent_logins` (
 *   `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
 *   `series` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
 *   `token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
 *   `last_used` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   PRIMARY KEY (`series`)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
 * </pre>
 * <p>
 * 2. 二次校验：如果用户使用了自动登录功能，我们可以只让他做一些常规的不敏感操作，例如数据浏览、查看，
 * 但是不允许他做任何修改、删除操作，如果用户点击了修改、删除按钮，我们可以跳转回登录页面，
 * 让用户重新输入密码确认身份，然后再允许他执行敏感操作。
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

                // 配置 url 的 角色 权限
                .antMatchers("/me/**").hasRole("me")
                .antMatchers("/myGirl/**").hasRole("myGirl")

                // rememberMe()：需要 rememberMe 才能访问；
                // fullyAuthenticated 不包含自动登录；
                // authenticated 包含自动登录
                .antMatchers("/remember").rememberMe()
                .antMatchers("/me/**").fullyAuthenticated()
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
                }).permitAll();
    }
}
