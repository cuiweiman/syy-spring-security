package com.syy.security.oauth04;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @Description:
 * @Author: cuiweiman
 * @Since: 2021/6/6 下午11:09
 */
@SpringBootApplication
public class SyyOauth04ClientApp {
    public static void main(String[] args) {
        SpringApplication.run(SyyOauth04ClientApp.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
