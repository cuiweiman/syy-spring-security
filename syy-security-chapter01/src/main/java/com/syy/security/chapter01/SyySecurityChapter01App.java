package com.syy.security.chapter01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 简单的引入 spring-boot-starter-security 依赖后，就保护了所以有 接口服务；
 * 访问端口时，会自动重定向到 security 的默认登陆页面。
 *
 * @Description: spring security chapter01
 * @Author: cuiweiman
 * @Since: 2021/5/25 下午2:51
 */
@SpringBootApplication
public class SyySecurityChapter01App {
    public static void main(String[] args) {
        SpringApplication.run(SyySecurityChapter01App.class, args);
    }
}
