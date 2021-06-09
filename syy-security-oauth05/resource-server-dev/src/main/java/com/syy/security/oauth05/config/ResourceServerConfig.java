package com.syy.security.oauth05.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;

/**
 * @Description: 资源服务器配置
 * @Author: cuiweiman
 * @Since: 2021/6/8 下午5:55
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Resource
    private TokenStore tokenStore;


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("res1")
                // 但是需要配置 TokenStore，会自动调用 JwtAccessTokenConverter 将 jwt 解析出来，
                // jwt 里边就包含了用户的基本信息，不再需要向 auth-server 进行 token 远程校验。
                .tokenStore(tokenStore);
    }

    /**
     * 配置资源服务器 接口路径 的 权限要求
     *
     * @param http http
     * @throws Exception 异常
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("admin")
                .anyRequest().authenticated();
    }

}
