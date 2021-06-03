package com.syy.security.oauth01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @Description: 第三方平台客户端
 * @Author: cuiweiman
 * @Since: 2021/6/3 上午11:44
 */
@SpringBootApplication
public class SyyOauth01ClientApp {
    public static void main(String[] args) {
        SpringApplication.run(SyyOauth01ClientApp.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
