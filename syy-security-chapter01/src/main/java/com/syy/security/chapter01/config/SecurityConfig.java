package com.syy.security.chapter01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @Description: 配置密码 加密方案
 * @Author: cuiweiman
 * @Since: 2021/5/25 下午3:18
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * {@link NoOpPasswordEncoder} 不加密
     *
     * @return 密码加密方式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * 配置 验证的 用户名和密码
     * <p>
     * {@link AuthenticationManagerBuilder#inMemoryAuthentication}: 来开启在内存中定义用户;
     * {@link UserDetailsManagerConfigurer#withUser}：配置用户名;
     * {@link UserDetailsManagerConfigurer.UserDetailsBuilder#password}：配置密码;
     * {@link  UserDetailsManagerConfigurer.UserDetailsBuilder#roles}：配置角色;
     * <p>
     * 若有多个用户，用 and 连接配置
     *
     * @param auth AuthenticationManagerBuilder身份校验器
     * @throws Exception 异常
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("syy").password("520").roles("myGirl")
                .and().withUser("cwm").password("250").roles("me");
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
     * 自定义配置 登陆页面等信息
     * 当我们定义了登录页面为 /login.html 的时候，Spring Security 也会帮我们自动注册一个 /login.html
     * 的接口，这个接口是 POST 请求，用来处理登录逻辑。
     * <p>登陆成功后的跳转：
     * {@link AbstractAuthenticationFilterConfigurer#defaultSuccessUrl}：登陆成功后跳转到的页面或接口，跳转后还可以继续重定向;
     * {@link FormLoginConfigurer#successForwardUrl}：登陆成功后，必须跳转到 指定的页面;和 defaultSuccessUrl 只配置一个即可；
     * {@link AbstractAuthenticationFilterConfigurer#successHandler}：包含了以上两个方法的功能，并且 与之对应的有 failureHandler。
     * <p>类似的，登陆失败后的跳转：
     * {@link AbstractAuthenticationFilterConfigurer#failureUrl}：登陆失败后 重定向到 指定的页面；
     * {@link FormLoginConfigurer#failureForwardUrl(java.lang.String)}：登陆失败后，服务端跳转；二者设置一个即可。
     * <p>配置 注销 登陆后的 页面跳转
     * {@link LogoutConfigurer#logoutRequestMatcher}： 设置 注销登陆的 接口路径 以及 请求方式;
     * {@link LogoutConfigurer#logoutUrl}：修改默认的 /logout 注销登陆请求接口，默认是 GET 请求;二者设置一个即可。
     *
     * @param http http
     * @throws Exception 异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 关闭 csrf
        http.csrf().disable()
                .authorizeRequests().anyRequest().authenticated()

                // 默认 设置了 登陆页面为 login.html，并且将 security 的 登陆接口也设置成了 login.html
                // .and().formLogin().loginPage("/login.html")
                // 分开设置 前端登陆页面 和 security 的后端登陆 接口
                .and().formLogin().loginPage("/login.html").loginProcessingUrl("/doLogin")

                // 默认的 用户名和密码 是 username、password，可以配置修改
                .usernameParameter("name").passwordParameter("pass")

                // 登陆成功后的跳转
                // .successForwardUrl("/index.html")
                .defaultSuccessUrl("/index.html")
                // 登陆失败后的跳转
                .failureUrl("/loginfail.html")
                .permitAll()

                // 注销登陆后的跳转以及操作
                .and().logout()
                // 设置 注销登陆的 接口路径 以及 请求方式,咋一直报错 重定向次数过多 呢？每个阶段一个 permitAll() 方法。
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", HttpMethod.GET.name()))
                .deleteCookies().clearAuthentication(true).invalidateHttpSession(true)
                .permitAll();
    }


}
