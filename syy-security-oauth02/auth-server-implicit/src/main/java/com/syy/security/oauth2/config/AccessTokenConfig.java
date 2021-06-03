package com.syy.security.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @Description: token 存储方式
 * @Author: cuiweiman
 * @Since: 2021/6/3 下午3:52
 */
@Configuration
public class AccessTokenConfig {
    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }
}
