package com.syy.security.oauth01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

/**
 * @Description: 配置 token 存放位置 ： {@link TokenStore} 为 RedisTokenStore。
 * @Author: cuiweiman
 * @Since: 2021/6/2 下午8:50
 */
@Configuration
public class AccessTokenStore {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public TokenStore tokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

}
