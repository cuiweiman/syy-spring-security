package com.syy.security.oauth05;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @Description: 第三方平台访问
 * @Author: cuiweiman
 * @Since: 2021/6/8 下午5:24
 */
@SpringBootApplication
public class SyyOauth05ClientApp {

    public static void main(String[] args) {
        SpringApplication.run(SyyOauth05ClientApp.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
