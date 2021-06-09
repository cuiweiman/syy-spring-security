package com.syy.security.oauth05.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.Map;

/**
 * 生成 JWT 时， 除了用户的主要信息外，添加的额外附加信息。
 *
 * @Description: JWT 额外信息添加
 * @Author: cuiweiman
 * @Since: 2021/6/9 下午4:28
 */
@Configuration
public class CustomAdditionalInformation implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> info = accessToken.getAdditionalInformation();
        info.put("author", "cwm");
        info.put("lover", "syy");
        info.put("expired", "forever");
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
        return accessToken;
    }
}
