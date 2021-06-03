package com.syy.security.oauth01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @Description: 授权服务
 * @Author: cuiweiman
 * @Since: 2021/6/2 下午4:06
 */
@SpringBootApplication
public class SyyOauth01AuthApp {
    public static void main(String[] args) {
        SpringApplication.run(SyyOauth01AuthApp.class,args);
        System.out.println(new BCryptPasswordEncoder().encode("250"));
        System.out.println(new BCryptPasswordEncoder().encode("520"));
    }
}
