package com.syy.security.oauth04.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/6 下午11:05
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Bean
    protected RemoteTokenServices tokenServices(){
        RemoteTokenServices tokenServices = new RemoteTokenServices();
        tokenServices.setCheckTokenEndpointUrl("http://localhost:9401/oauth/check_token");
        tokenServices.setClientId("CSClient");
        tokenServices.setClientSecret("123");
        return tokenServices;
    }


    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("res1").tokenServices(tokenServices());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("admin")
                .anyRequest().authenticated();
    }

}
