package com.syy.security.oauth02.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;


/**
 * @Description: 资源服务配置
 * @Author: cuiweiman
 * @Since: 2021/6/3 下午4:11
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    /**
     * 配置了 access_token 的校验地址、client_id、client_secret 这三个信息，
     * 当用户来资源服务器请求资源时，会携带上一个 access_token，通过这里的配置，就能够校验出 token 是否正确等。
     */
    @Bean
    RemoteTokenServices tokenServices() {
        RemoteTokenServices tokenServices = new RemoteTokenServices();
        tokenServices.setCheckTokenEndpointUrl("http://localhost:9201/oauth/check_token");
        tokenServices.setClientId("CSClient");
        tokenServices.setClientSecret("123");
        return tokenServices;
    }

    /**
     * 配置 本资源 的 验证 信息
     *
     * @param resources 资源验证信息
     * @throws Exception 异常
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("res1").tokenServices(tokenServices());
    }

    /**
     * 配置 本资源的 访问 权限 信息。
     *
     * @param http HttpSecurity
     * @throws Exception 异常
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 匹配的 url 需要具备 admin 角色。
                .antMatchers("/admin/**").hasRole("admin")
                // 匹配的 url，不需要 身份验证
                .antMatchers("/hello/**").permitAll()
                // 其他 url 需要 通过 身份验证
                .anyRequest().authenticated()
                // 配置跨域支持
                .and().cors();
    }
}
