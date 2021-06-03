package com.syy.security.oauth01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * EnableAuthorizationServer: 开启授权服务器的自动化配置
 *
 * @Description: 配置 授权服务
 * @Author: cuiweiman
 * @Since: 2021/6/2 下午8:55
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Resource
    private DataSource dataSource;

    @Resource
    private TokenStore tokenStore;

    @Resource
    private ClientDetailsService clientDetailsService;

    @Bean
    protected AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        // 客户端详情服务
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenStore(tokenStore);
        // 令牌有效时间 2h
        tokenServices.setAccessTokenValiditySeconds(60 * 60 * 2);
        // 刷新令牌有效期 3天
        tokenServices.setRefreshTokenValiditySeconds(60 * 60 * 24 * 3);
        return tokenServices;
    }

    /**
     * 配置 授权码  的存储
     * <p>
     * 授权码是用来获取令牌的，使用一次就失效，令牌则是用来获取资源的
     *
     * @return 授权码的存储
     */
    @Bean
    AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    /**
     * 配置令牌的访问端点和令牌服务
     *
     * @param endpoints 端点
     * @throws Exception 异常
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authorizationCodeServices(authorizationCodeServices())
                .tokenServices(tokenServices());
    }

    /**
     * 配置令牌端点的安全约束，这个端点谁能访问，谁不能访问。
     *
     * @param security security
     * @throws Exception 异常
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

    /**
     * 配置客户端的详细信息，授权服务器要做两方面的检验，一方面是校验客户端，
     * 另一方面则是校验用户，校验用户在{@link SecurityConfig}配置了，这里就是配置校验客户端。
     * <p>
     * 这里将 客户端信息 存在内存中，
     * 这里我们分别配置了客户端的 id，secret、资源 id、授权类型、授权范围以及重定向 uri。
     *
     * @param clients 客户端信息
     * @throws Exception 异常
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("CSClient")
                .secret(new BCryptPasswordEncoder().encode("123"))
                .resourceIds("res1")
                // 授权模式只有四种，但实际上 refresh_token 也是一种授权模式
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("all")
                // 跳转回 第三方 应用 配置的 地址
                .redirectUris("http://localhost:9103/index.html");
    }

}
