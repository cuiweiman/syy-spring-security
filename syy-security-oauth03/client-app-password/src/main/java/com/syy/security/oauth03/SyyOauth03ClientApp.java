package com.syy.security.oauth03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/6 下午8:26
 */
@SpringBootApplication
public class SyyOauth03ClientApp {

    public static void main(String[] args) {
        SpringApplication.run(SyyOauth03ClientApp.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
