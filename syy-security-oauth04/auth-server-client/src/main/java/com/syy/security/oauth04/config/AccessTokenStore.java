package com.syy.security.oauth04.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/6 下午10:55
 */
@Configuration
public class AccessTokenStore {
    @Bean
    public TokenStore tokenStore(){
        return new InMemoryTokenStore();
    }
}
