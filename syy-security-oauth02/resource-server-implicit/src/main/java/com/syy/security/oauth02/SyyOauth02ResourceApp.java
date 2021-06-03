package com.syy.security.oauth02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: 简化模式 资源服务。客户端没有后台，只能通过js访问，注意跨域
 * @Author: cuiweiman
 * @Since: 2021/6/3 下午4:09
 */
@SpringBootApplication
public class SyyOauth02ResourceApp {
    public static void main(String[] args) {
        SpringApplication.run(SyyOauth02ResourceApp.class, args);
    }
}
