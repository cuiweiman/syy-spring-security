package com.syy.security.oauth05.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * 必须与 auth-server 的 token 配置一致，共用一个
 *
 * @Description: 配置 token 令牌存储方案
 * @Author: cuiweiman
 * @Since: 2021/6/8 下午5:37
 */
@Configuration
public class AccessTokenConfig {

    /**
     * 在 JWT 和 用户信息 进行相互转换的时候，使用的 签名
     */
    private static final String SIGNING_KEY = "SYY520";

    /**
     * TokenStore 使用 JwtTokenStore 这个实例。
     * 之前将 access_token 无论是存储在内存中，还是存储在 Redis 中，都是要存下来的，客户端将 access_token 发来之后，
     * 还要校验看对不对。但是如果使用了 JWT，access_token 实际上就不用存储了（无状态登录，服务端不需要保存信息），
     * 因为用户的所有信息都在 jwt 里边，所以这里配置的 JwtTokenStore 本质上并不是做存储
     *
     * @return JwtTokenStore
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    /**
     * 用于实现 将用户信息和 JWT 进行转换
     * （将用户信息转为 jwt 字符串，或者从 jwt 字符串提取出用户信息）
     *
     * @return JwtAccessTokenConverter
     * @see JwtAccessTokenConverter
     * @see org.springframework.security.oauth2.provider.token.TokenEnhancer
     */
    @Bean
    protected JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(SIGNING_KEY);
        return jwtAccessTokenConverter;
    }

}
